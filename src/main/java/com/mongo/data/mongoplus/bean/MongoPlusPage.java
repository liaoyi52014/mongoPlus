package com.mongo.data.mongoplus.bean;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author liaoyi
 * @version V1.0
 * @className MongPlusPage
 * @description
 * @date 2022/3/12 11:29 PM
 * @since [产品/模块版本]
 **/
@Data
@Builder
public class MongoPlusPage implements Serializable {

    private static final long serialVersionUID = -2951995418542687219L;
    /**
     * 分页对象
     */
    private List list;

    /**
     * 页码
     */
    private Long pageNo;

    /**
     * 页长
     */
    private Integer pageSize;

    /**
     * 总页数
     */
    private Long total;

}