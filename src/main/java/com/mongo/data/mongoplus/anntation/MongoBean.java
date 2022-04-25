package com.mongo.data.mongoplus.anntation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author liaoyi
 * @version V1.0
 * @className MongoBean
 * @description
 * @date 2022/4/24 16:08
 * @since [产品/模块版本]
 **/

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface MongoBean {
    /** 功能描述：服务对应的mongo collection
     * @return java.lang.Class<?>
     * @author liaoyi
     * @date 2022/4/24 16:11
     */
    Class beanClass();

}
