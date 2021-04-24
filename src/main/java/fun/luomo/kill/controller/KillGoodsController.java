package fun.luomo.kill.controller;

import fun.luomo.kill.bean.model.KillGoodsPrice;
import fun.luomo.kill.bean.vo.KillGoodsPriceVo;
import fun.luomo.kill.service.KillGoodsPriceService;
import fun.luomo.kill.util.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author luomo
 * @date 2021/3/26 18:07
 */
@RestController
@RequestMapping("kill")
@Slf4j
public class KillGoodsController {
    @Autowired
    KillGoodsPriceService killGoodsPriceService;

    @PostMapping("killByDB/{id}")
    public R<KillGoodsPriceVo> killByDB(@PathVariable("id") Integer killId) {

        KillGoodsPriceVo killGoodsPriceVo = killGoodsPriceService.detail(killId);
        if (System.currentTimeMillis() < killGoodsPriceVo.getBegainTime().getTime()) {
            return R.failResponse("秒杀还未开始");
        }
        if (System.currentTimeMillis() > killGoodsPriceVo.getEndTime().getTime()) {
            return R.failResponse("秒杀已经结束");
        }
        if (!killGoodsPriceService.secKillByDb(killId, getSessionUserId())) {
            return R.failResponse("秒杀失败");
        }

        return R.successResponse("ok", killGoodsPriceVo);
    }

    @PostMapping("killOnlyIncr/{id}")
    public R<KillGoodsPrice> killOnlyIncr(@PathVariable("id") int killId) {
        KillGoodsPriceVo killGoods = killGoodsPriceService.detail(killId);
        if (killGoods.getBegainTime().getTime() > System.currentTimeMillis()) {
            return R.failResponse("抢购还未开始");
        }
        if (killGoods.getEndTime().getTime() < System.currentTimeMillis()) {
            return R.failResponse("抢购已结束");
        }
        if (!killGoodsPriceService.secKill(killId, getSessionUserId())) {
            log.info("----抢购失败----");
            return R.failResponse("抢购失败");
        }

        return R.successResponse("ok", killGoods);
    }

    @PostMapping("killBylua/{id}")
    public R<KillGoodsPrice> killByLua(@PathVariable("id") int killId) {
        KillGoodsPriceVo killGoods = killGoodsPriceService.detail(killId);
        if (killGoods.getBegainTime().getTime() > System.currentTimeMillis()) {
            return R.failResponse("抢购还未开始");
        }
        if (killGoods.getEndTime().getTime() < System.currentTimeMillis()) {
            return R.failResponse("抢购已结束");
        }
        if (!killGoodsPriceService.secKillByLock(killId, getSessionUserId())) {
            log.info("----抢购失败----");
            return R.failResponse("抢购失败");
        }

        return R.successResponse("ok", killGoods);
    }

    @PostMapping("secKillByRedissonLock/{id}")
    public R secKillByRedissonLock(@PathVariable("id") int killId) {
        KillGoodsPriceVo killGoods = killGoodsPriceService.detail(killId);
        if (killGoods.getBegainTime().getTime() > System.currentTimeMillis()) {
            return R.failResponse("抢购还未开始");
        }
        if (killGoods.getEndTime().getTime() < System.currentTimeMillis()) {
            return R.failResponse("抢购已结束");
        }
        if (!killGoodsPriceService.secKillByRedissonLock(killId, getSessionUserId())) {
            log.info("----抢购失败----");
            return R.failResponse("抢购失败");
        }

        return R.successResponse("ok", killGoods);
    }

    private String getSessionUserId() {
        return "AAA";
    }
}
