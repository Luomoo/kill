package fun.luomo.kill.service.impl;

import fun.luomo.kill.bean.model.OrderKill;
import fun.luomo.kill.dao.OrderMapper;
import fun.luomo.kill.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author luomo
 * @date 2021/3/27 15:19
 */
@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    OrderMapper orderMapper;


    @Override
    public OrderKill selectByPrimaryKey(Integer orderKillId) {
        return orderMapper.selectByPrimaryKey(orderKillId);
    }

    @Override
    public Integer queryCountByUserId(String userId) {
        return orderMapper.queryCountByUserId(userId);
    }

}
