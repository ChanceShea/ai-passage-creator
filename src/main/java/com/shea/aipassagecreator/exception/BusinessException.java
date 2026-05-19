package com.shea.aipassagecreator.exception;

/**
 * @author : Shea.
 * @since : 2026/5/18 09:36
 */
public class BusinessException extends RuntimeException{

    private String message;
    private int code;

    public BusinessException(String message,int code){
        this.message = message;
        this.code = code;
    }

    public BusinessException(ErrorCode errorCode){
        this.message = errorCode.getMessage();
        this.code = errorCode.getCode();
    }

    public BusinessException(ErrorCode errorCode,String message){
        this.message = message;
        this.code = errorCode.getCode();
    }


}
