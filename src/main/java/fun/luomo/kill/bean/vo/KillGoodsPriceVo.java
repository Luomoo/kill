package fun.luomo.kill.bean.vo;

import fun.luomo.kill.bean.model.KillGoodsPrice;
import lombok.Data;

import java.io.Serializable;

/**
 * @author luomo
 * @date 2021/3/27 10:01
 */
@Data
public class KillGoodsPriceVo extends KillGoodsPrice implements Serializable {
    private static final long serialVersionUID = -3354246766267329205L;

    private String goodsName;
    private String originalImg;
    private String key;
    private String keyName;
}
