package com.lm.ll.spark.enum


/**
 * 描述：下载的数据类型，用于区分是普通文章列表还是查询到的文章列表，在recyclerview的下拉和上滑加载数据时判断调用的数据接口
 * 作者：Created by ll on 2018-08-14 9:55.
 * 邮箱：wenhelinlu@gmail.com
 */
enum class LoadDataType {
    /**
     * @desc 普通文章列表
     * @author ll
     * @time 2018-08-14 9:56
     */
    COMMON_ARTICLE_LIST,
    /**
     * @desc 根据关键词查询到的文章列表
     * @author ll
     * @time 2018-08-14 9:57
     */
    QUERY_ARTICLE_LIST
}