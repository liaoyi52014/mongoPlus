package com.mongo.data.mongoplus.proxy;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

/**
 * @author liaoyi
 * @version V1.0
 * @className MongoFactoryBean
 * @description
 * @date 2022/3/10 8:16 PM
 * @since [产品/模块版本]
 **/

public class MongoFactoryBean<T> implements FactoryBean<T> {

    private final Class<T> clazz;


    public MongoFactoryBean(Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public T getObject() throws Exception {
       InvocationHandler handler =new MongoPlusMapperProxy();
        Object o = Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, handler);
      return (T)o;
    }

    @Override
    public Class<T> getObjectType() {
        return this.clazz;
    }
}