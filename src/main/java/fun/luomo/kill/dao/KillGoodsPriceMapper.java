package fun.luomo.kill.dao;

import fun.luomo.kill.bean.model.KillGoodsPrice;
import fun.luomo.kill.bean.vo.KillGoodsPriceVo;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author luomo
 * @date 2021/3/26 18:06
 */
@Mapper
public interface KillGoodsPriceMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(KillGoodsPrice record);

    int insertSelective(KillGoodsPrice record);

    KillGoodsPrice selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(KillGoodsPrice record);

    int updateByPrimaryKey(KillGoodsPrice record);

    KillGoodsPriceVo detail(Integer killId);


    int updateSecKill(KillGoodsPrice killGoodsPrice);
}
