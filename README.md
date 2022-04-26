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

    //目前已实现方法
    /** 功能描述：新增一个
     * @param t bean对象
     * @author liaoyi
     * @date 2022/3/30 15:59
     */
    void addOne(T t);

    /** 功能描述：批量新增
     * @param collection 对象集合
     * @author liaoyi
     * @date 2022/3/30 16:02
     */
    void addBatch(Collection<T> collection);

    /** 功能描述：根据主键id查询
     * @param id 主键id
     * @return java.lang.Object
     * @author liaoyi
     * @date 2022/3/30 15:54
     */
    T getById(String id);

    /** 功能描述：根据bean对象查询
     * @param t bean对象
     * @return java.util.List<T>
     * @author liaoyi
     * @date 2022/3/30 15:55
     */
    List<T>  getByCondition(T t);

    /** 功能描述：根据id进行修改（非空的参数全替换）
     * @param t bean对象
     * @return long
     * @author liaoyi
     * @date 2022/3/30 15:57
     */
    long updateById(T t);

    /** 功能描述：根据条件修改（不支持更改子对象字段，只支持全部替换）
     * @param queryBean 条件
     * @param updateBean 设置的值
     * @return long 数量
     * @author liaoyi
     * @date 2022/3/30 16:03
     */
    long updateByCondition(T queryBean,T updateBean);

    /** 功能描述：根据id集合进行删除
     * @param ids 主键id集合
     * @return long
     * @author liaoyi
     * @date 2022/3/30 15:58
     */
    long removeByIds(List<String> ids);

    /** 功能描述：根据主键id删除
     * @param id 主键id
     * @return long 删除数量
     * @author liaoyi
     * @date 2022/3/30 16:04
     */
    long removeById(String id);

    /** 功能描述：根据Criteria 进行查询
     * @param criteria 条件
     * @return java.util.List<T>
     * @author liaoyi
     * @date 2022/4/25 09:50
     */
    List<T> getByCriteria(Criteria criteria);

    /** 功能描述：根据Query查询
     * @param query 查询query条件
     * @return java.util.List<T>
     * @author liaoyi
     * @date 2022/4/25 09:50
     */
    List<T> getByQuery(Query query);

    /** 功能描述：根据Criteria 进行查询一个结果
     * @param criteria 条件
     * @return T
     * @author liaoyi@qding.me
     * @date 2022/4/26 16:54
     */
    T getOneByCriteria(Criteria criteria);

    /** 功能描述：根据query 查询一个结果
     * @param query 查询query条件
     * @return T
     * @author liaoyi@qding.me
     * @date 2022/4/26 16:54
     */
    T getOneByQuery(Query query);

    /** 功能描述：根据query作为条件来更改update
     * @param query 查询query条件
     * @param update 更新条件
     * @return long 更新数量
     * @author liaoyi
     * @date 2022/4/25 09:52
     */
    long updateByQuery(Query query, Update update);

    /** 功能描述：根据query 作为条件，根据updateBean不为空的参数 来设置值
     * @param query 查询query条件
     * @param updateBean 修改对象
     * @return long
     * @author liaoyi@qding.me
     * @date 2022/4/26 16:51
     */
    long updateByQuery(Query query, T updateBean);


    /** 功能描述：根据criteria 作为条件，根据updateBean不为空的参数 来设置值
     * @param criteria 查询条件
     * @param updateBean 修改对象
     * @return long
     * @author liaoyi@qding.me
     * @date 2022/4/26 16:50
     */
    long updateByCriteria(Criteria criteria,T updateBean);

    /** 功能描述：根据query进行删除
     * @param query 查询条件
     * @return long 删除数量
     * @author liaoyi
     * @date 2022/4/25 09:54
     */
    long deleteByQuery(Query query);

    /** 功能描述：根据 criteria 作为条件删除
     * @param criteria 查询条件
     * @return long
     * @author liaoyi@qding.me
     * @date 2022/4/26 16:52
     */
    long deleteByCriteria(Criteria criteria);

```
##### 3. 项目中1和2混用
两个可以混用，只是使用代理模式，则不要自己实现，如果使用接口模式，则要继承IMongoPlusService，MongoPlusServiceImpl，
一个接口只能使用其中一种方式，一个项目可以混用两种方式
