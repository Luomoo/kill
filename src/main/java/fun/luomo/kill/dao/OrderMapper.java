package fun.luomo.kill.dao;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import fun.luomo.kill.bean.model.OrderKill;

/**
 * @author luomo
 * @date 2021/3/27 15:18
 */
@Mapper
public interface OrderMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(OrderKill record);

    int insertSelective(OrderKill record);

    OrderKill selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(OrderKill record);

    int updateByPrimaryKey(OrderKill record);

    Integer queryCountByUserId(String userId);
}