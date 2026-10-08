package com.itheima.java_fight.common;

public class Result<T> {
    private int code;
    private String msg;
    private T data;

    public Result(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public T getData() {
        return data;
    }

    public static <T> Result<T> success(T data){
        return new Result<>(1,"success",data);
    }

    public static <T> Result<T> error(String msg){
        return new Result<>(0,msg,null);
    }


}
