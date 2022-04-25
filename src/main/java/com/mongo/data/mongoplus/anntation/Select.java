package com.mongo.data.mongoplus.anntation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author liaoyi
 * @version V1.0
 * @className MongoSelectParamAnnotation
 * @description 用于标记查询参数
 *   与注解${@link MongoSelectMethod} 联合使用
 * T  getBean(@MongoSelectParamAnnotation MongoBean mongoBean)
 *
 * @date 2022/3/10 3:13 PM
 * @since [产品/模块版本]
 **/
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Select {


    /** 功能描述：根据id查询时，此处放入mongo的主键的值
     * @return java.lang.String
     * @author liaoyi
     * @date 2022/3/10 3:09 PM
     */
    boolean  selectById() default false;

    /** 功能描述：是否分页，默认不分页
     * @return boolean
     * @author liaoyi
     * @date 2022/3/12 11:41 PM
     */
    boolean ifPage() default false;


    /** 功能描述：排序字段，目前只支持单个字段排序
     * @return java.lang.String
     * @author liaoyi
     * @date 2022/3/10 3:21 PM
     */
    String orderMongoParameter() default "";

}