package com.shea.aipassagecreator.common;

import lombok.Data;

/**
 * 统一返回结果类
 * @author : Shea.
 * @since : 2026/5/18 09:34
 */
@Data
public class Result<T> {
    private int code;
    private String message;
    private T data;

    public Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(200, "Success", data);
    }

    public static <T> Result<T> success(String message) {
        return new Result<>(200, message, null);
    }

    public static <T> Result<T> fail(int code, String message) {
        return new Result<>(code, message, null);
    }
}
