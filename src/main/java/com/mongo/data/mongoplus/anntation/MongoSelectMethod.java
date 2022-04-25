package com.mongo.data.mongoplus.anntation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author liaoyi
 * @version V1.0
 * @className MongoSelect
 * @description 用于注解 ${@link MongoPlusMapperScan 注释的方法，表明此方法用于查询}
 * @date 2022/3/10 3:00 PM
 * @since [产品/模块版本]
 **/
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface MongoSelectMethod {

    /** 功能描述：对应的mongo的bean对象，此对象应该包含mongo的${@link org.springframework.data.mongodb.core.mapping.Document 注解}
     * @return java.lang.Class<?>
     * @author liaoyi
     * @date 2022/3/10 3:05 PM
     */
    Class<?> bean() ;

}
