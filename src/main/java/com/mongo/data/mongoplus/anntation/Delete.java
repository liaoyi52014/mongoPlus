package com.mongo.data.mongoplus.anntation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author liaoyi
 * @version V1.0
 * @className MongoDeleteParam
 * @description  修改时的参数注解 与 ${@link MongoDeleteMethod 注解联合使用}，如果没有参数，
 * 使用${@link Delete} 标记的对象所有不为null的参数作为条件来删除，否则按id删除
 * long deleteBean(@MongoDeleteParam MongoBean deleteBean)
 *  如果要按id删除则
 *   long deleteBean(@MongoDeleteParam(deleteById=true) MongoBean deleteBean)
 * @date 2022/3/10 3:46 PM
 * @since [产品/模块版本]
 **/
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Delete {

  /** 功能描述：是否根据id来删除
   * @return boolean
   * @author liaoyi
   * @date 2022/3/10 3:50 PM
   */
    boolean  deleteById() default false;

    /** 功能描述：根据拼装好的query进行删除
     * @return java.lang.Class<?>[]
     * @author liaoyi
     * @date 2022/3/13 12:20 AM
     */
    Class<?>[] deleteByMongoQueryBean() default {};



}
