package fun.luomo.kill.service;

import fun.luomo.kill.bean.model.OrderKill;

/**
 * @author luomo
 * @date 2021/3/27 15:19
 */
public interface OrderService {

    OrderKill selectByPrimaryKey(Integer orderKillId);

    Integer queryCountByUserId(String userId);
}
