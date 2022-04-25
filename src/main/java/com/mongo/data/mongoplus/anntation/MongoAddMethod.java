package com.mongo.data.mongoplus.anntation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author liaoyi
 * @version V1.0
 * @className MongoAdd
 * @description mongo新增的注解，表明此方法为新增
 * @date 2022/3/10 3:56 PM
 * @since [产品/模块版本]
 **/
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface MongoAddMethod {

    /** 功能描述：对应的mongo的bean对象，此对象应该包含mongo的${@link org.springframework.data.mongodb.core.mapping.Document 注解}
     * @return java.lang.Class<?>
     * @author liaoyi
     * @date 2022/3/10 3:05 PM
     */
    Class<?> bean() ;

}
