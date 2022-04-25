package com.mongo.data.mongoplus.proxy;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.mongo.data.mongoplus.anntation.*;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.maven.surefire.shade.org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author liaoyi
 * @version V1.0
 * @className MongoPlusMapperProxy
 * @description 动态代理mongo的mapper类
 * @date 2022/3/10 6:34 PM
 * @since [产品/模块版本]
 **/
@Slf4j
public class MongoPlusMapperProxy implements InvocationHandler {


    private MongoTemplate mongoTemplate;

    private static final String ORDER_ASC = "asc";

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (Object.class.equals(method.getDeclaringClass())) {
            return method.invoke(proxy, args);
        }
        mongoTemplate = MongoPlusImportBeanDefinitionRegistrar.applicationContext.getBean(MongoTemplate.class);
        //获取方法注解
        return resolverMethod(method, args);
    }

    /**
     * 功能描述：解析方法
     *
     * @param method 对应的方法
     * @param args   参数
     * @return java.lang.Object
     * @author liaoyi
     * @date 2022/3/11 3:45 PM
     */
    public Object resolverMethod(Method method, Object[] args) throws NoSuchFieldException, IllegalAccessException {

        MongoAddMethod mongoAdd = method.getDeclaredAnnotation(MongoAddMethod.class);
        if (null != mongoAdd) {
            return this.add(method, args[0]);

        }
        MongoDeleteMethod mongoDelete = method.getDeclaredAnnotation(MongoDeleteMethod.class);
        if (null != mongoDelete) {
            return this.delete(method, args[0]);
        }
        MongoUpdateMethod mongoUpdateMethod = method.getDeclaredAnnotation(MongoUpdateMethod.class);
        if (args.length == 2 && null != mongoUpdateMethod) {
            return this.update(method, args[0], args[1]);
        }
        MongoSelectMethod mongoSelectMethod = method.getDeclaredAnnotation(MongoSelectMethod.class);
        if (null != mongoSelectMethod) {
            return this.select(method, args);
        }

        return null;
    }

    private Object select(Method method, Object[] arg) throws IllegalAccessException {
        //获取执行的方法上的注解
        MongoSelectMethod mongoSelectMethod = method.getDeclaredAnnotation(MongoSelectMethod.class);
        if (mongoSelectMethod == null) {
            return null;
        }
        //mongo 的bean对象
        Class<?> bean = mongoSelectMethod.bean();
        //方法参数的类对象
        Class<?> paramClass = arg[0].getClass();
        //获取方法参数的注解
        Parameter[] parameters = method.getParameters();
        //获取查询方法参数的注解
        Select select = parameters[0].getDeclaredAnnotation(Select.class);
        boolean b = select.selectById();
        Criteria criteria = new Criteria();
        if (b) {
            String id = JSONUtil.parseObj(arg[0]).getStr("id");
            criteria = Criteria.where("_id").is(new ObjectId(id));
        } else {
            Field[] declaredFields = paramClass.getDeclaredFields();
            StringBuilder prefix = new StringBuilder();
            for (Field declaredField : declaredFields) {
                this.appendCriteria(criteria, declaredField, arg[0], prefix, new AtomicInteger(0));
            }
        }
        Query query = new Query(criteria);
        String orderParameter = select.orderMongoParameter();
        if (StringUtils.isNotBlank(orderParameter)) {
            String[] split = orderParameter.split(",");
            Sort orders = ORDER_ASC.equals(split[1]) ? Sort.by(Sort.Direction.ASC, split[0]) : Sort.by(Sort.Direction.DESC, split[0]);
            query.with(orders);
        }
        long pageNo=-1L;
        if(arg.length>=2&&arg[1] instanceof Long){
            pageNo=(Long)arg[1];
        }
        long pageSize=-1L;
        if(arg.length>=2&&arg[2] instanceof Long){
            pageSize=(Long)arg[2];
        }
        if (-1 != pageNo && -1 != pageSize) {
            query.skip(pageSize * (pageNo - 1)).limit(Integer.parseInt(String.valueOf(pageSize)));
        }
        if (b) {
            return this.mongoTemplate.findOne(query, bean);
        }

        return this.mongoTemplate.find(query, bean);

    }

    private String getMongoDocument(Class<?> bean) {
        Document mongoDocument = bean.getDeclaredAnnotation(Document.class);
        if (null == mongoDocument) {
            log.error("current bean {} is not mongo bean ", bean.getName());
            return null;
        }
        return mongoDocument.collection();
    }

    /**
     * 功能描述：执行新增
     *
     * @param method 方法
     * @param object 参数
     * @author liaoyi
     * @date 2022/3/10 7:25 PM
     */
    private int add(Method method, Object object) {
        MongoAddMethod mongoAdd = method.getDeclaredAnnotation(MongoAddMethod.class);
        if (null != mongoAdd) {
            Class<?> bean = mongoAdd.bean();
            String collation = this.getMongoDocument(bean);
            if (StringUtils.isNotBlank(collation)) {
                mongoTemplate.save(object, collation);
                return 1;
            }
        }
        return -1;

    }

    /**
     * 功能描述：执行删除方法
     *
     * @param method 方法
     * @param object 参数
     * @return boolean true为就是删除，否则不是
     * @author liaoyi
     * @date 2022/3/10 7:25 PM
     */
    private Object delete(Method method, Object object) throws NoSuchFieldException, IllegalAccessException {
        MongoDeleteMethod mongoDelete = method.getDeclaredAnnotation(MongoDeleteMethod.class);
        if (null != mongoDelete) {
            Class<?> bean = mongoDelete.bean();
            String mongoDocument = getMongoDocument(bean);
            Parameter[] parameters = method.getParameters();
            if (null != parameters) {
                Parameter parameter = parameters[0];
                Delete delete = parameter.getDeclaredAnnotation(Delete.class);
                boolean deleteById = delete.deleteById();
                Query query;
                Class<?> paramClass = object.getClass();
                if (deleteById) {
                    Field idField = paramClass.getDeclaredField("id");
                    idField.setAccessible(true);
                    Object id = idField.get(object);
                    if (null == id) {
                        log.error("method {} delete by id ,but id is null", method.getName());
                        return true;
                    }
                    Criteria criteria = Criteria.where("_id").is(id);
                    query = new Query(criteria);
                } else {
                    Field[] declaredFields = paramClass.getDeclaredFields();
                    Field[] superClassFields = paramClass.getSuperclass().getDeclaredFields();
                    List<Field> fields = Arrays.asList(declaredFields);
                    fields.addAll(Arrays.asList(superClassFields));
                    Criteria criteria = new Criteria();
                    StringBuilder stringBuilder = new StringBuilder();
                    fields.forEach(field -> {
                        try {
                            appendCriteria(criteria, field, object, stringBuilder, new AtomicInteger(0));
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    });
                    query = new Query(criteria);
                }
                if (StringUtils.isNotBlank(mongoDocument)) {
                    DeleteResult remove = mongoTemplate.remove(query, mongoDocument);
                    return remove.getDeletedCount();
                }

            }


        }
        return -1;
    }

    /**
     * 功能描述：修改
     *
     * @param method 执行的方法
     * @param arg    第一个参数
     * @param arg1   第二个参数
     * @return java.lang.Object
     * @author liaoyi
     * @date 2022/3/12 10:22 PM
     */
    private Object update(Method method, Object arg, Object arg1) throws IllegalAccessException {
        MongoUpdateMethod mongoUpdateMethod = method.getDeclaredAnnotation(MongoUpdateMethod.class);
        if (null != mongoUpdateMethod) {
            Class<?> bean = mongoUpdateMethod.bean();
            if (null != arg && null != arg1) {
                //默认第一个为查询参数
                Object selectObj = arg;
                //默认第二个为修改参数
                Object updateObj = arg1;

                //获取方法的参数
                Parameter[] parameters = method.getParameters();
                //默认第一个为查询参数
                Parameter selectParameter = parameters[0];
                //默认第二个为修改参数
                Parameter updateParameter = parameters[1];

                //查询参数
                Select selectParam = selectParameter.getDeclaredAnnotation(Select.class);
                if (null == selectParam) {
                    //修改第二个为查询参数
                    selectParameter = parameters[1];
                    selectParam = selectParameter.getDeclaredAnnotation(Select.class);
                    selectObj = arg1;
                }
                Query updateQuery = this.getUpdateQuery(selectParam, selectObj);
                //修改参数
                Update updateParam = updateParameter.getDeclaredAnnotation(Update.class);
                if (null == updateParam) {
                    updateObj = arg;
                }
                org.springframework.data.mongodb.core.query.Update update = this.getUpdate(updateObj);
                UpdateResult updateResult = this.mongoTemplate.updateMulti(updateQuery, update, bean);
                return updateResult.getModifiedCount();
            }
        }
        return -1;
    }

    /**
     * 功能描述：拼接修改要set的值
     *
     * @param updateParamObj 修改的入参对象
     * @return org.springframework.data.mongodb.core.query.Update
     * @author liaoyi
     * @date 2022/3/12 10:18 PM
     */
    private org.springframework.data.mongodb.core.query.Update getUpdate(Object updateParamObj) throws IllegalAccessException {
        org.springframework.data.mongodb.core.query.Update update = new org.springframework.data.mongodb.core.query.Update();
        Field[] declaredFields = updateParamObj.getClass().getDeclaredFields();
        StringBuilder prefix = new StringBuilder();
        for (Field declaredField : declaredFields) {
            this.appendUpdate(update, declaredField, updateParamObj, prefix, new AtomicInteger(0));
        }
        return update;
    }

    /**
     * 功能描述：拼接查询条件
     *
     * @param selectParam 查询的参数注解
     * @param selectParamObj 查询的参数值
     * @return org.springframework.data.mongodb.core.query.Query
     * @author liaoyi
     * @date 2022/3/12 10:18 PM
     */
    private Query getUpdateQuery(Select selectParam, Object selectParamObj) throws IllegalAccessException {
        //如果是根据id来修改，则直接返回查询query
        boolean selectById = selectParam.selectById();
        if (selectById) {
            String id = JSONUtil.parseObj(selectParamObj).getStr("id");
            Criteria criteria = Criteria.where("_id").is(id);
            return new Query(criteria);
        }
        Field[] declaredFields = selectParamObj.getClass().getDeclaredFields();
        Criteria criteria = new Criteria();
        StringBuilder prefix = new StringBuilder();
        for (Field declaredField : declaredFields) {
            this.appendCriteria(criteria, declaredField, selectParamObj, prefix, new AtomicInteger(0));
        }

        return new Query(criteria);
    }


    /**
     * 功能描述：递归拼接查询条件，支持List<String>(size 为1 的查询条件) 如 criteria.and("list").is(MongoBean.getList<String>.get(0))
     * List<Object> 也支持 criteria.and("list.ObjectName").is(MongoBean.getList<Object>.get(0))
     * Map<Object,Object> 也支持 多条件 如{"key1":"value1","key2":"value2"} 会生成
     * criteria.and("map.key1").is(value1).and("map.key2").is(value2);
     * 同时支持常见数据类型
     *
     * @param criteria mongo criteria 拼接查询条件
     * @param field    字段
     * @param object   对象
     * @param prefix   key 前缀
     * @param deep     深度
     * @author liaoyi
     * @date 2022/3/12 1:13 AM
     */
    @SuppressWarnings("unchecked")
    private void appendCriteria(Criteria criteria, Field field, Object object, StringBuilder prefix, AtomicInteger deep) throws IllegalAccessException {
        //获取mongo bean的Field注解
        org.springframework.data.mongodb.core.mapping.Field fieldDeclaredAnnotation = field.getDeclaredAnnotation(org.springframework.data.mongodb.core.mapping.Field.class);
        if (null == fieldDeclaredAnnotation) {
            return;
        }
        //mongo bean 数据库字段
        String key = fieldDeclaredAnnotation.value();
        if (StringUtils.isBlank(key)) {
            return;
        }
        //获取参数的值
        field.setAccessible(true);
        Object value = field.get(object);
        //如果没有值，则不拼查询sql
        if (null == value) {
            return;
        }
        try {
            JSONObject jsonObject = JSONUtil.parseObj(value);
            //表明为一个对象
            boolean empty = jsonObject.isEmpty();
            if (empty) {
                //如果为空对象，则表明不是String，且不是key，value的形式的对象，只可能是包装类，或者时间等类型或者为数组
                if (value instanceof Collection) {
                    String[] split = prefix.toString().split("\\.");
                    int i = deep.get();
                    if (i == 0) {
                        prefix = new StringBuilder();
                    } else {
                        String[] strings = Arrays.copyOf(split, i);
                        String join = String.join(".", strings);
                        prefix = new StringBuilder(join);
                    }
                    prefix.append(key);
                    Collection<Object> collection = (Collection<Object>) value;
                    if (collection.stream().findFirst().isPresent()) {
                        Object o = collection.stream().findFirst().get();
                        //如果是List<String> 直接是 is o,因为如果存储的是List<String> 则查询时，值可能传一个条件
                        if (o instanceof String) {
                            criteria.and(prefix.toString()).is(o);
                        } else {
                            //为List<Object>
                            Field[] declaredFields = o.getClass().getDeclaredFields();
                            for (Field innerField : declaredFields) {
                                String prefixStr = prefix.toString();
                                try {
                                    deep.addAndGet(1);
                                    appendCriteria(criteria, innerField, o, new StringBuilder(prefixStr), deep);
                                } catch (IllegalAccessException e) {
                                    log.error("error ", e);
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                } else {
                    criteria.and(StringUtils.isBlank(prefix.toString()) ? key : prefix.append(".").append(key).toString()).is(value);
                }
            } else {
                //为一个对象，且表明有值,则要递归获取
                valueIsObject(value, deep, prefix, key, criteria);
            }
        } catch (Exception e) {
            //发生异常，通常是String类型，则直接拼
            criteria.and(StringUtils.isBlank(prefix.toString()) ? key : prefix.append(".").append(key).toString()).is(value);
        }
    }

    /**
     * 功能描述：如果值是object时的处理
     *
     * @param value    值
     * @param deep     深度
     * @param prefix   mongo key已拼接的 前缀
     * @param key      当前的key
     * @param criteria mongo查询
     * @author liaoyi
     * @date 2022/3/12 9:51 PM
     */
    private void valueIsObject(Object value, AtomicInteger deep, StringBuilder prefix, String key, Criteria criteria) throws IllegalAccessException {
        Class<?> aClass = value.getClass();
        Field[] declaredFields = aClass.getDeclaredFields();
        int i = deep.get();
        if (i == 0) {
            prefix = new StringBuilder(key);
        } else {
            String[] split = prefix.toString().split("\\.");
            String[] strings = Arrays.copyOf(split, i);
            String join = String.join(".", strings);
            prefix = new StringBuilder(join);
        }
        if (value instanceof Map) {
            Map<Object, Object> map = (Map<Object, Object>) value;
            for (Map.Entry<Object, Object> entry : map.entrySet()) {
                Object valueKey = entry.getKey();
                Object v = entry.getValue();
                String strPrefix = prefix + "." + valueKey;
                criteria.and(strPrefix).is(v);
            }
        } else {
            for (Field innerField : declaredFields) {
                String prefixStr = prefix.toString();
                deep.addAndGet(1);
                appendCriteria(criteria, innerField, value, new StringBuilder(prefixStr), deep);
            }
        }
    }

    /**
     * 功能描述：递归拼接查询条件，支持List<String>(size 为1 的查询条件) 如 criteria.and("list").is(MongoBean.getList<String>.get(0))
     * List<Object> 也支持 criteria.and("list.ObjectName").is(MongoBean.getList<Object>.get(0))
     * Map<Object,Object> 也支持 多条件 如{"key1":"value1","key2":"value2"} 会生成
     * criteria.and("map.key1").is(value1).and("map.key2").is(value2);
     * 同时支持常见数据类型
     *
     * @param criteria mongo criteria 拼接查询条件
     * @param field    字段
     * @param object   对象
     * @param prefix   key 前缀
     * @param deep     深度
     * @author liaoyi
     * @date 2022/3/12 1:13 AM
     */
    @SuppressWarnings("unchecked")
    private void appendUpdate(org.springframework.data.mongodb.core.query.Update criteria, Field field, Object object, StringBuilder prefix, AtomicInteger deep) throws IllegalAccessException {
        //获取mongo bean的Field注解
        org.springframework.data.mongodb.core.mapping.Field fieldDeclaredAnnotation = field.getDeclaredAnnotation(org.springframework.data.mongodb.core.mapping.Field.class);
        if (null == fieldDeclaredAnnotation) {
            return;
        }
        //mongo bean 数据库字段
        String key = fieldDeclaredAnnotation.value();
        if (StringUtils.isBlank(key)) {
            return;
        }
        //获取参数的值
        field.setAccessible(true);
        Object value = field.get(object);
        //如果没有值，则不拼查询sql
        if (null == value) {
            return;
        }
        try {
            JSONObject jsonObject = JSONUtil.parseObj(value);
            //表明为一个对象
            boolean empty = jsonObject.isEmpty();
            if (empty) {
                //如果为空对象，则表明不是String，且不是key，value的形式的对象，只可能是包装类，或者时间等类型或者为数组
                if (value instanceof Collection) {
                    String[] split = prefix.toString().split("\\.");
                    int i = deep.get();
                    if (i == 0) {
                        prefix = new StringBuilder();
                    } else {
                        String[] strings = Arrays.copyOf(split, i);
                        String join = String.join(".", strings);
                        prefix = new StringBuilder(join);
                    }
                    prefix.append(key);
                    Collection<Object> collection = (Collection<Object>) value;
                    if (collection.stream().findFirst().isPresent()) {
                        Object o = collection.stream().findFirst().get();
                        //如果是List<String> 直接是 is o,因为如果存储的是List<String> 则查询时，值可能传一个条件
                        if (o instanceof String) {
                            criteria.set(prefix.toString(), o);
                        } else {
                            Field[] declaredFields = o.getClass().getDeclaredFields();
                            for (Field innerField : declaredFields) {
                                String prefixStr = prefix.toString();
                                try {
                                    deep.addAndGet(1);
                                    appendUpdate(criteria, innerField, o, new StringBuilder(prefixStr), deep);
                                } catch (IllegalAccessException e) {
                                    log.error("error ", e);
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                } else {
                    criteria.set(StringUtils.isBlank(prefix.toString()) ? key : prefix.append(".").append(key).toString(), value);
                }
            } else {
                //为一个对象，且表明有值,则要递归获取
                Class<?> aClass = value.getClass();
                Field[] declaredFields = aClass.getDeclaredFields();
                int i = deep.get();
                if (i == 0) {
                    prefix = new StringBuilder(key);
                } else {
                    String[] split = prefix.toString().split("\\.");
                    String[] strings = Arrays.copyOf(split, i);
                    String join = String.join(".", strings);
                    prefix = new StringBuilder(join);
                }
                if (value instanceof Map) {
                    Map<Object, Object> map = (Map<Object, Object>) value;
                    for (Map.Entry<Object, Object> entry : map.entrySet()) {
                        Object valueKey = entry.getKey();
                        Object v = entry.getValue();
                        String strPrefix = prefix + "." + valueKey;
                        criteria.set(strPrefix, v);
                    }
                } else {
                    for (Field innerField : declaredFields) {
                        String prefixStr = prefix.toString();
                        deep.addAndGet(1);
                        appendUpdate(criteria, innerField, value, new StringBuilder(prefixStr), deep);
                    }
                }
            }
        } catch (Exception e) {
            //发生异常，通常是String类型，则直接拼
            criteria.set(StringUtils.isBlank(prefix.toString()) ? key : prefix.append(".").append(key).toString(), value);
        }
    }


}