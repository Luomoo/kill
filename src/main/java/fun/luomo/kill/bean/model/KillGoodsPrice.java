package fun.luomo.kill.bean.model;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author luomo
 * @date 2021/3/26 18:19
 */
/**
    * 商品秒杀
    */
public class KillGoodsPrice implements Serializable {
    private static final long serialVersionUID = -1498353491003159693L;
    private Integer id;

    /**
    * 商品规格id
    */
    private Integer specGoodsId;

    /**
    * 状态：1，有效;2，无效
    */
    private Integer status;

    /**
    * 价格
    */
    private BigDecimal price;

    /**
    * 库存数量
    */
    private Integer killCount;

    /**
    * 开始时间
    */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date begainTime;

    /**
    * 结束时间
    */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getSpecGoodsId() {
        return specGoodsId;
    }

    public void setSpecGoodsId(Integer specGoodsId) {
        this.specGoodsId = specGoodsId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getKillCount() {
        return killCount;
    }

    public void setKillCount(Integer killCount) {
        this.killCount = killCount;
    }

    public Date getBegainTime() {
        return begainTime;
    }

    public void setBegainTime(Date begainTime) {
        this.begainTime = begainTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }
}