package com.mongo.data.mongoplus.anntation;

import com.mongo.data.mongoplus.proxy.MongoPlusImportBeanDefinitionRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author liaoyi
 * @version V1.0
 * @className MongoMapperScan
 * @description 扫描mongo的mapper接口注解,注解对应的mapper接口上
 * @date 2022/3/10 2:02 PM
 * @since [产品/模块版本]
 **/
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import(MongoPlusImportBeanDefinitionRegistrar.class)
public @interface MongoPlusMapperScan {

    /** 功能描述：要扫描的mapper路径
     * @return java.lang.String
     * @author liaoyi
     * @date 2022/3/11 3:01 PM
     */
    String packages();



}
