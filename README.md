# mongoPlus

#### 介绍
mongo框架，旨在减少mongoTemplate查询时各种Query的编写，较少各种重复劳动，本框架使用jdk动态代理，类似mybatis框架，用户只要使用接口+注解就可以实现简单的增删改查，对于复杂的查询等操作，也可使用实现接口的方式，
默认的实现类中，已实现大部分的增删改查方法


#### 使用说明

##### 1.  使用代理模式

```
// 首先在启动类上加上注解@MongoPlusMapperScan，会扫描指定的包下的接口，使用jdk动态代理，实现接口里面的所有方法

@SpringBootApplication
@MongoPlusMapperScan(packages = "com.mongo.你的服务接口包.**Service")
public class MongoTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(MongoTestApplication.class, args);
    }

}

/**
 * @author liaoyi
 * @version V1.0
 * @className MongoProxyService
 * @description  @MongoPlusMapperScan 扫描包下的接口
 * @date 2022/4/26 11:42
 * @since [产品/模块版本]
 **/

public interface MongoProxyService {
   /** 功能描述：@MongoSelectMethod 表明是一个查询方法，bean对应的是这个方法要查询的mongo的bean的class对象
        @Select 表明这个是查询参数，这个注解对应的bean必须与@MongoSelectMethod的bean的值对应，
        只能查询Mongo的bean对象作为条件
     * @param testMongo
     * @return java.util.List<com.mongo.mongotest.bean.TestMongo>
     * @author liaoyi
     * @date 2022/4/26 15:14
     */
    @MongoSelectMethod(bean = MongoBean.class)
    List<TestMongo> getList(@Select TestMongo testMongo);

}

//使用对应的注解，用类似的方式，支持所有的增删改查
```
##### 2.  实现指定接口的方式
```
// 不需要在启动类上添加其他注解
/**
 * @author liaoyi
 * @version V1.0
 * @className MongoTestService
 * @description 你的业务接口需要继承IMongoPlusService<T>，T为这个业务服务对应的mongo 的bean的class对象
 * @date 2022/4/26 11:27
 * @since [产品/模块版本]
 **/

public interface MongoTestService extends IMongoPlusService<TestMongo> {

}

//实现类

/**
 * @author liaoyi
 * @version V1.0
 * @className MongoTestServiceImpl
 * @description 实现类需要继承MongoPlusServiceImpl<T> T为这个业务服务对应的mongo 的bean的class对象，
 同时实现他的对应的接口 MongoTestService，MongoPlusServiceImpl中已实现了大部分的增删改查方法
 * @date 2022/4/26 11:27
 * @since [产品/模块版本]
 **/
@Service
@MongoBean(beanClass=TestMongo.class)
public class MongoTestServiceImpl extends MongoPlusServiceImpl<TestMongo> implements MongoTestService {


}


```
##### 3. 项目中1和2混用
两个可以混用，只是使用代理模式，则不要自己实现，如果使用接口模式，则要继承IMongoPlusService，MongoPlusServiceImpl，
一个接口只能使用其中一种方式，一个项目可以混用两种方式
