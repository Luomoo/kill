package fun.luomo.kill.util;

import lombok.Data;

import java.io.Serializable;

/**
 * @author luomo
 * @date 2021/3/26 18:09
 */
@Data
public class R<E> implements Serializable {
    private static final long serialVersionUID = -4188037498887997739L;
    private String code;

    private String msg;

    private E data;

    public R() {
    }

    public R(String code, String msg, E data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public R(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public static <T> R<T> successResponse(String message) {
        return new R<>(ResponseCodeConstant.SUCCESS, message);
    }

    public static <T> R<T> successResponse(String message, T singleData) {
        return new R<>(ResponseCodeConstant.SUCCESS, message, singleData);
    }
    public static <T> R<T> failResponse(String message) {
        return new R<>(ResponseCodeConstant.SUCCESS, message);
    }


}
