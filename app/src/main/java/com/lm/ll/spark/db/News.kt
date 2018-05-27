package com.lm.ll.spark.db


/**
 * Created by ll on 2018-05-24 17:23.
 */
data class News(
        var id: Int = 0, //id
        var title: String? = null, //标题
        var url: String? = null, //url链接
        var author: String? = null, //作者
        var date: String? = null, //日期
        var readCount: String? = null, //阅读数
        var childNodes: ArrayList<News>? = null //子节点，如评论
)

