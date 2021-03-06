package com.mongo.data.mongoplus.service.impl;


import com.mongo.data.mongoplus.anntation.MongoBean;
import com.mongo.data.mongoplus.exception.MongoPlusException;
import com.mongo.data.mongoplus.service.IMongoPlusService;
import com.mongodb.client.result.DeleteResult;
import org.apache.maven.surefire.shade.org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author liaoyi
 * @version V1.0
 * @className MongoPlusServiceImpl
 * @description
 * @date 2022/4/2 11:50
 * @since [产品/模块版本]
 **/

public class MongoPlusServiceImpl<T> implements IMongoPlusService<T> {
    public MongoPlusServiceImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }
    protected MongoTemplate mongoTemplate;

    @Override
    public void addOne(T t) {
        mongoTemplate.save(t);

    }

    @Override
    public void addBatch(Collection<T> collection) {
        assert collection.size()>0:"查询条件不允许为空";
        for (T next : collection) {
            mongoTemplate.save(next);
        }
    }

    @Override
    public T getById(String id) {
        assert StringUtils.isNotBlank(id):"查询条件不允许为空";
        Criteria criteria = Criteria.where("_id").is(new ObjectId(id));
        Optional.<Class<T>>ofNullable(getMongoBean()).orElseThrow(() -> {
            throw new MongoPlusException(this.getClass().getName() + " @MongoBean  annotation must exists");
        });
        return mongoTemplate.findOne(new Query(criteria), Optional.ofNullable(getMongoBean()).orElseThrow(() -> {
            throw new MongoPlusException(this.getClass().getName() + " @MongoBean  annotation must exists");
        }));
    }

    @Override
    public List<T> getByCondition(T t) {
        assert t!=null:"查询条件不允许为空";
        Criteria criteria = new Criteria();
        this.createCriteria(t, criteria,null);
        return mongoTemplate.find(new Query(criteria),Optional.ofNullable(getMongoBean()).orElseThrow(() -> {
            throw new MongoPlusException(this.getClass().getName() + " @MongoBean  annotation must exists");
        }));
    }

    @Override
    public long updateById(T t) {
        assert t!=null:"查询条件不允许为空";
        Object idObj=null;
        try {
            Field idField = t.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            idObj = idField.get(t);
            assert idObj!=null:"根据id更新，ID不允许为空";
            Criteria criteria=Criteria.where("_id").is(new ObjectId(idObj.toString()));
            //把id设为空，避免修改id
            idField.set(t,null);
            Update update=new Update();
            this.mongoCommonUpdate(update,t,null);

            return mongoTemplate.updateMulti(new Query(criteria),update,Optional.ofNullable(getMongoBean()).orElseThrow(() -> {
            throw new MongoPlusException(this.getClass().getName() + " @MongoBean  annotation must exists");
        })).getModifiedCount();
        } catch (NoSuchFieldException | IllegalAccessException e) {
           throw new MongoPlusException("updateById by Id= "+idObj+" error cause by "+ Arrays.toString(e.getStackTrace()));
        }
    }

    @Override
    public long updateByCondition(T queryBean, T updateBean) {
        assert null!=queryBean&&null!=updateBean:"修改参数不允许为空";
        Criteria criteria=new Criteria();
        this.createCriteria(queryBean,criteria,null);
        //创建update
        Update update=new Update();
        this.mongoCommonUpdate(update,updateBean,null);
        return mongoTemplate.updateMulti(new Query(criteria),update,Optional.ofNullable(getMongoBean()).orElseThrow(() -> {
            throw new MongoPlusException(this.getClass().getName() + " @MongoBean  annotation must exists");
        })).getModifiedCount();
    }

    @Override
    public long removeByIds(List<String> ids) {
        assert ids.size()>0:"根据id删除不允许集合为空";
        AtomicLong num=new AtomicLong(0);
        ids.forEach(id->{
            DeleteResult res = mongoTemplate.remove(new Query(Criteria.where("_id").is(new ObjectId(id))));
            long deletedCount = res.getDeletedCount();
            num.addAndGet(deletedCount);
        });

        return num.get();
    }

    @Override
    public long removeById(String id) {
        assert StringUtils.isNotBlank(id):"参数不允许为空";
        DeleteResult res = mongoTemplate.remove(new Query(Criteria.where("_id").is(new ObjectId(id))));
        return res.getDeletedCount();
    }

    @Override
    public List<T> getByCriteria(Criteria criteria) {
        assert criteria!=null:"查询条件不允许为空";
        return mongoTemplate.find(new Query(criteria),Optional.ofNullable(getMongoBean()).orElseThrow(() -> {
            throw new MongoPlusException(this.getClass().getName() + " @MongoBean  annotation must exists");
        }));
    }

    @Override
    public List<T> getByQuery(Query query) {
        assert query!=null:"查询条件不允许为空";
        return mongoTemplate.find(query,Optional.ofNullable(getMongoBean()).orElseThrow(() -> {
            throw new MongoPlusException(this.getClass().getName() + " @MongoBean  annotation must exists");
        }));
    }

    @Override
    public T getOneByCriteria(Criteria criteria) {
        assert criteria!=null:"查询条件不允许为空";
        return mongoTemplate.findOne(new Query(criteria),Optional.ofNullable(getMongoBean()).orElseThrow(() -> {
            throw new MongoPlusException(this.getClass().getName() + " @MongoBean  annotation must exists");
        }));
    }

    @Override
    public T getOneByQuery(Query query) {
        assert query!=null:"查询条件不允许为空";
        return mongoTemplate.findOne(query,Optional.ofNullable(getMongoBean()).orElseThrow(() -> {
            throw new MongoPlusException(this.getClass().getName() + " @MongoBean  annotation must exists");
        }));
    }

    @Override
    public long updateByQuery(Query query, Update update) {
        assert query!=null&&update!=null:"修改参数不允许为空";
        return mongoTemplate.updateMulti(query,update,Optional.ofNullable(getMongoBean()).orElseThrow(() -> {
            throw new MongoPlusException(this.getClass().getName() + " @MongoBean  annotation must exists");
        })).getModifiedCount();
    }

    @Override
    public long updateByQuery(Query query, T updateBean) {
        assert query!=null&&updateBean!=null:"修改参数不允许为空";
        Update update=new Update();
        this.mongoCommonUpdate(update,updateBean, null);
       return this.updateByQuery(query,update);
    }

    @Override
    public long updateByCriteria(Criteria criteria, T updateBean) {
        assert criteria!=null&&updateBean!=null:"修改参数不允许为空";
        Query query=new Query(criteria);
        return this.updateByQuery(query,updateBean);
    }

    @Override
    public long deleteByQuery(Query query) {
        assert query!=null:"查询条件不允许为空";
        return mongoTemplate.remove(query,Optional.ofNullable(getMongoBean()).orElseThrow(() -> {
            throw new MongoPlusException(this.getClass().getName() + " @MongoBean  annotation must exists");
        })).getDeletedCount();
    }

    @Override
    public long deleteByCriteria(Criteria criteria) {
        assert  null!=criteria:"删除条件不允许为空";
        Query query=new Query(criteria);
        return this.deleteByQuery(query);
    }

    private Class<T> getMongoBean() {
        MongoBean annotation = this.getClass().getAnnotation(MongoBean.class);
        if (null == annotation) {
            return null;
        }
        return (Class<T>) annotation.beanClass();
    }

    /**
     * 功能描述 创建Criteria
     *
     * @param t        bean 对象
     * @param criteria 查询参数
     * @param parentKey 上一级的key
     * @author liaoyi
     * @date 2022/4/24 16:23
     */
    private void createCriteria(Object t, Criteria criteria,String parentKey) {
        Class<?> aClass = t.getClass();
        Field[] declaredFields = aClass.getDeclaredFields();
        for (Field declaredField : declaredFields) {
            org.springframework.data.mongodb.core.mapping.Field annotation = declaredField.getAnnotation(org.springframework.data.mongodb.core.mapping.Field.class);
            if (null == annotation) {
                continue;
            }
            declaredField.setAccessible(true);
            String key = annotation.value();
            if (StringUtils.isBlank(key)) {
                continue;
            }
            if(StringUtils.isNotBlank(parentKey)){
                key=parentKey+"."+key;
            }
            try {
                Object o = declaredField.get(t);
                if (null != o) {
                    if (o instanceof String) {
                        String value = (String)o;
                        if (StringUtils.isNotBlank(value)) {
                            criteria.and(key).is(value);
                        }
                    } else if (!(o instanceof Date) && !(o instanceof Map) && !(o instanceof List)) {
                        this.createCriteria(o, criteria, key);
                    } else {
                        criteria.and(key).is(o);
                    }
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    /** 功能描述：生成公用的update
     * @param t 泛型对象
     * @param parentKey 上级的key
     * @author liaoyi
     * @date 2022/3/7 9:31 AM
     */
    private void mongoCommonUpdate(Update update,Object t,String parentKey){
        Class<?> aClass = t.getClass();
        Field[] declaredFields = aClass.getDeclaredFields();
        for (Field declaredField : declaredFields) {
            declaredField.setAccessible(true);
            org.springframework.data.mongodb.core.mapping.Field annotation = declaredField.getAnnotation(org.springframework.data.mongodb.core.mapping.Field.class);
            if(null==annotation){
                continue;
            }
            String key = annotation.value();
            if(StringUtils.isBlank(key)){
                continue;
            }
            if(StringUtils.isNotBlank(parentKey)){
                key=parentKey+"."+key;
            }
            try {
                Object o = declaredField.get(t);
                if(null!=o){
                    if (o instanceof String) {
                        String value = (String)o;
                        if (StringUtils.isNotBlank(value)) {
                            update.set(key, value);
                        }
                    } else if (!(o instanceof Date) && !(o instanceof Map) && !(o instanceof List)) {
                        this.mongoCommonUpdate(update,o, key);
                    } else {
                        update.set(key, o);
                    }
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

}