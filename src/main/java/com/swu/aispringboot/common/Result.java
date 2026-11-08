package com.swu.aispringboot.common;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

@Data//lombok注解，自动生成getter/setter/toString/equals/hashCode方法
@JsonPropertyOrder({"code", "msg", "data"})
public class Result<T> {
    private String code;
    private String msg;
    private T data;

    public static <T> Result<T> ok() {
        Result<T> result = new Result<>();
        result.setCode(ResultCode.SUCCESS.getCode());
        result.setMsg(ResultCode.SUCCESS.getMsg());
        return result;
    }
    public static <T> Result<T> ok(T data) {
        Result<T> result = ok();
        result.setData(data);
        return result;
    }

    public static <T> Result<T> error() {
        Result<T> result = new Result<>();
        result.setCode(ResultCode.ERROR.getCode());
        result.setMsg(ResultCode.ERROR.getMsg());
        return result;
    }
    public static <T> Result<T> error(String code,String msg,T data) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMsg(msg);
        result.setData(data);
        return result;
    }
}
