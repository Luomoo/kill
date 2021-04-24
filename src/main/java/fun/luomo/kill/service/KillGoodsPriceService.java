package fun.luomo.kill.service;

import fun.luomo.kill.bean.vo.KillGoodsPriceVo;

/**
 * @author luomo
 * @date 2021/3/26 18:06
 */

public interface KillGoodsPriceService {
    KillGoodsPriceVo detail(Integer killId);

    boolean secKillByDb(Integer killId, String sessionUserId);

    boolean secKill(int killId, String userId);

    boolean secKillByLock(int killId, String userId);

    boolean secKillByRedissonLock(int killId, String sessionUserId);

}
