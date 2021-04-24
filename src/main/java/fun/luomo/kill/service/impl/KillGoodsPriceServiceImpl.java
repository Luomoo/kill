package fun.luomo.kill.service.impl;

import com.alibaba.fastjson.JSONObject;
import fun.luomo.kill.bean.model.KillGoodsPrice;
import fun.luomo.kill.bean.model.OrderKill;
import fun.luomo.kill.bean.vo.KillGoodsPriceVo;
import fun.luomo.kill.constant.KillConstants;
import fun.luomo.kill.dao.KillGoodsPriceMapper;
import fun.luomo.kill.lock.RedisLock;
import fun.luomo.kill.service.KillGoodsPriceService;
import fun.luomo.kill.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author luomo
 * @date 2021/3/26 18:07
 */
@Slf4j
@Service
public class KillGoodsPriceServiceImpl implements KillGoodsPriceService {
    @Autowired
    private KillGoodsPriceMapper killGoodsPriceMapper;
    @Autowired
    CacheManager cacheManager;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private OrderService orderService;

    @Autowired
    @Qualifier("redissonClient")
    private RedissonClient redissonClient;

    /**
     * 执行扣库存的脚本
     */
    public static final String STOCK_LUA;

    /**
     * 库存没有初始化，库存key在redis里面不存在
     */
    public static final long UNINITIALIZED_STOCK = -3L;
    /**
     * setNX
     */
    public static String REDIS_LOCK = "stock:lock";

    static {
        StringBuilder sb = new StringBuilder();
        sb.append("if (redis.call('exists', KEYS[1]) == 1) then");
        sb.append("    local stock = tonumber(redis.call('get', KEYS[1]));");
        sb.append("    local num = tonumber(ARGV[1]);");
        sb.append("    if (stock == -1) then");
        sb.append("        return -1;");
        sb.append("    end;");
        sb.append("    if (stock >= 1) then");
        sb.append("        return redis.call('incrby', KEYS[1], 0 - num);");
        sb.append("    end;");
        sb.append("    return -2;");
        sb.append("end;");
        sb.append("return -3;");
        STOCK_LUA = sb.toString();

    }

    @Override
    public KillGoodsPriceVo detail(Integer killId) {
        KillGoodsPriceVo detail = null;
        String killGoodsDetailName = KillConstants.KILLGOOD_DETAIL + killId;
        // 1. 从本地cache中拿
        Cache killCache = cacheManager.getCache("killgoodDetail");
        if (null != killCache.get(killGoodsDetailName)) {
            log.info(Thread.currentThread().getName() + "--------ehcache缓存里面的到数据1-------");
            return (KillGoodsPriceVo) killCache.get(killGoodsDetailName).getObjectValue();
        }
        // 2. 从redis中拿
        Object KillGoodsPrice = redisTemplate.opsForValue().get(killGoodsDetailName);
        if (KillGoodsPrice != null) {
            log.info(Thread.currentThread().getName() + "----------redis中的数据1-----------");
            return JSONObject.parseObject(KillGoodsPrice.toString(), KillGoodsPriceVo.class);
        }
        synchronized (killGoodsPriceMapper) {
            // 1. 从本地cache中拿
            killCache = cacheManager.getCache("killgoodDetail");
            if (null != killCache.get(killGoodsDetailName)) {
                log.info(Thread.currentThread().getName() + "--------ehcache缓存里面的到数据2-------");
                return (KillGoodsPriceVo) killCache.get(killGoodsDetailName).getObjectValue();
            }
            // 2. 从redis中拿
            KillGoodsPrice = redisTemplate.opsForValue().get(killGoodsDetailName);
            if (KillGoodsPrice != null) {
                log.info(Thread.currentThread().getName() + "----------redis中的数据2-----------");
                return JSONObject.parseObject(KillGoodsPrice.toString(), KillGoodsPriceVo.class);
            }

            detail = killGoodsPriceMapper.detail(killId);
            log.info(Thread.currentThread().getName() + "----------MySQL中的数据-----------");
            if (detail != null) {
                killCache.putIfAbsent(new Element(killGoodsDetailName, detail));
                redisTemplate.opsForValue().set(killGoodsDetailName, JSONObject.toJSONString(detail));
            }
        }
        return detail;
    }

    @Override
    public boolean secKillByDb(Integer killId, String userId) {
        KillGoodsPrice kgp = killGoodsPriceMapper.selectByPrimaryKey(killId);
        if (kgp.getKillCount() <= 0) {
            log.info("--------商品不足--------");
            return false;
        }
        Integer count = orderService.queryCountByUserId(userId);
        if (/*count!=null && count>0*/false) {
            return false;
        }
        KillGoodsPrice killGoodsPrice = new KillGoodsPrice();
        killGoodsPrice.setKillCount(1);
        killGoodsPrice.setId(killId);

        int i = killGoodsPriceMapper.updateSecKill(killGoodsPrice);
        if (i == 0) {
            log.info("-----------已经秒杀完了----------");
            return false;
        }
        redisTemplate.opsForSet().add(KillConstants.KILLGOOD_USER + killId, userId);
        return true;
    }

    @Override
    public boolean secKill(int killId, String userId) {
        //判断用户是否已经秒杀过
        Boolean member = redisTemplate.opsForSet().isMember(KillConstants.KILLED_GOOD_USER + killId, userId);
        /*if (member) {
            log.info("---------userId:" + userId + "----已经秒杀过了");
            return false;
        }*/

        String killGoodCount = KillConstants.KILL_GOOD_COUNT + killId;
        //1000   只有一个1
        //库存没有办法进行补偿
        if (redisTemplate.opsForValue().increment(killGoodCount, -1) < 0) {
            log.info("---------stock 余量不足--------");
            return false;
        }
        //把用户设置到redis，代表已经秒杀过
        redisTemplate.opsForSet().add(KillConstants.KILLED_GOOD_USER + killId, userId);
        return true;
    }

    @Override
    public boolean secKillByLock(int killId, String userId) {
        String killGoodCount = KillConstants.KILL_GOOD_COUNT + killId;
        //返回的数值,执行了lua脚本
        Long stock = stock(killGoodCount, 1, STOCK_LUA);

        if (stock == UNINITIALIZED_STOCK) {
            Timer timer = null;
            RedisLock redisLock = new RedisLock(redisTemplate, REDIS_LOCK);
            try {
                //如果竞争锁成功  如果其他线程没竞争锁成功，这里是阻塞的
                if (redisLock.tryLock()) {
                    //锁续命
                    timer = continueLock(REDIS_LOCK);

                    stock = stock(killGoodCount, 1, STOCK_LUA);
                    if (stock == UNINITIALIZED_STOCK) {
                        KillGoodsPrice killGoodsPrice = killGoodsPriceMapper.selectByPrimaryKey(killId);
                        redisTemplate.opsForValue().set(killGoodCount, String.valueOf(killGoodsPrice.getKillCount()), 60 * 60, TimeUnit.SECONDS);
                        //再次去执行lua脚本，扣减库存
                        stock = stock(killGoodCount, 1, STOCK_LUA);
                    }
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            } finally {
                if (timer != null) {
                    timer.cancel();
                }
                //释放锁 。自己加的锁不能让别人释放，自己只能释放自己的锁
                //这里要进行一个value值的比较，只要自己的value值相等才能释放
                redisLock.unlock();
            }
        }
        //如果是这种情况，秒杀成功
        boolean flag = stock >= 0;
//        if (flag) {
//            redisTemplate.opsForSet().add(KillConstants.KILLED_GOOD_USER + killId, userId);
//        }
        return flag;
    }

    @Override
    public boolean secKillByRedissonLock(int killId, String userId) {
        Boolean member = redisTemplate.opsForSet().isMember(KillConstants.KILLED_GOOD_USER + killId, userId);
        if (member) {
            log.info("--------userId:" + userId + "--has secKilled");
            return false;
        }
        final String killGoodCount = KillConstants.KILL_GOOD_COUNT + killId;

        long stock = stock(killGoodCount, 1, STOCK_LUA);
        // 初始化库存
        if (stock == UNINITIALIZED_STOCK) {
            RLock lock = redissonClient.getLock("store_lock_cn_order");
            try {
                // 获取锁,支持过期解锁功能 2秒钟以后自动解锁
                lock.lock(2, TimeUnit.SECONDS);
                // 双重验证，避免并发时重复回源到数据库
                stock = stock(killGoodCount, 1, STOCK_LUA);
                if (stock == UNINITIALIZED_STOCK) {
                    // 获取初始化库存
                    KillGoodsPrice killGoodsPrice = killGoodsPriceMapper.selectByPrimaryKey(killId);
                    // 将库存设置到redis
                    redisTemplate.opsForValue().set(killGoodCount, String.valueOf(killGoodsPrice.getKillCount().intValue()), 60 * 60, TimeUnit.SECONDS);
                    // 调一次扣库存的操作
                    stock = stock(killGoodCount, 1, STOCK_LUA);
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            } finally {
                lock.unlock();
            }

        }
        boolean flag = stock >= 0;
        if (flag) {
            //秒杀成功，缓存秒杀用户和商品
            redisTemplate.opsForSet().add(KillConstants.KILLGOOD_USER, killId + userId);
        }
        return flag;
    }

    private Timer continueLock(String lockKey) {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
//                redisTemplate.opsForValue().set(lockKey, "", 60, TimeUnit.SECONDS);
                redisTemplate.expire(lockKey, 60, TimeUnit.SECONDS);
            }
        }, 0, 1);
        return timer;
    }

    /**
     * 扣库存
     *
     * @param key 库存key
     * @param num 扣减库存数量
     * @return 扣减之后剩余的库存【-3:库存未初始化; -2:库存不足; -1:不限库存; 大于等于0:扣减库存之后的剩余库存】
     */
    public Long stock(String key, int num, String script) {
        // 脚本里的KEYS参数
        List<String> keys = new ArrayList<>();
        keys.add(key);
        // 脚本里的ARGV参数
        List<String> args = new ArrayList<>();
        args.add(Integer.toString(num));
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(script, Long.class);
        Long result = redisTemplate.execute(redisScript, keys, Integer.toString(num));
        return result;
    }
}
