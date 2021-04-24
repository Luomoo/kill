package fun.luomo.kill.bean.model;

/**
 * @author luomo
 * @date 2021/3/27 15:18
 */
public class OrderKill {
    private Integer id;

    private Integer userid;

    private Integer killgoosid;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserid() {
        return userid;
    }

    public void setUserid(Integer userid) {
        this.userid = userid;
    }

    public Integer getKillgoosid() {
        return killgoosid;
    }

    public void setKillgoosid(Integer killgoosid) {
        this.killgoosid = killgoosid;
    }
}