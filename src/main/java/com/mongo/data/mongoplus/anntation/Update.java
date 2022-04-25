package com.mongo.data.mongoplus.anntation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author liaoyi
 * @version V1.0
 * @className MongoUpdateParam
 * @description 修改时的参数注解 与 ${@link MongoUpdateMethod 注解联合使用}，如果没有参数，则默认使用${@link Select }注解注释的对象作为条件，
 * 使用${@link Update} 来se'值
 *
 * long updateBean(@MongoSelectParam MongoBean query ,@MongoUpdateParam MongoBean update)
 * MongoBeanQuery 所有不为null（字符串不为空字符串）的参数为条件，MongoBeanUpdate 所有不为null（字符串不为空字符串）的字段设置值
 * 如果根据id来修改
 *
 * long updateBean(@MongoUpdateParam(updateById=true)MongoBean update)
 *
 * @date 2022/3/10 3:25 PM
 * @since [产品/模块版本]
 **/
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Update {

    /** 功能描述：是否根据id来删除
     * @return boolean
     * @author liaoyi
     * @date 2022/3/10 3:50 PM
     */
    boolean  updateById() default false;


}
