package com.mongo.data.mongoplus.exception;

/**
 * @author liaoyi
 * @version V1.0
 * @className MongoPlusException
 * @description
 * @date 2022/3/10 4:12 PM
 * @since [产品/模块版本]
 **/

public class MongoPlusException extends RuntimeException{

    public MongoPlusException() {
    }

    public MongoPlusException(String message) {
        super(message);
    }

    public MongoPlusException(String message, Throwable cause) {
        super(message, cause);
    }
}