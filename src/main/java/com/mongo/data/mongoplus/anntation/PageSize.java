package com.mongo.data.mongoplus.anntation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author liaoyi
 * @version V1.0
 * @className PageSize
 * @description
 * @date 2022/3/14 11:08 AM
 * @since [产品/模块版本]
 **/
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface PageSize {

}
