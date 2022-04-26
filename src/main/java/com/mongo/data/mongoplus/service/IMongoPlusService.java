package com.mongo.data.mongoplus.service;



import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.Collection;
import java.util.List;

/**
 * @author liaoyi
 * @version V1.0
 * @className IMongoPlusService
 * @description
 * @date 2022/3/30 15:54
 * @since [产品/模块版本]
 **/

public interface IMongoPlusService <T>{

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







}
