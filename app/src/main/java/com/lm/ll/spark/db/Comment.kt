package com.lm.ll.spark.db

import com.squareup.moshi.JsonClass
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


/**
 * Desc: 文章评论
 * 作者：Created by ll on 2018-06-22 10:42.
 * 邮箱：wenhelinlu@gmail.com
 */
@Entity
@JsonClass(generateAdapter = true)
data class Comment(
        @Id var id: Long = 0, //objectbox内部主键
        var url: String? = null, //url链接
        var title: String = "", //标题
        var author: String = "", //作者
        var date: String = "", //文章发表日期
        var textLength: String = "", //文章字数
        var readCount: String = "", //阅读数
        var text: String = "", //文章正文
        var depth: Int = 0, //评论深度（用于缩进显示）
        var insertTime: String? = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) //文章收藏入库时间
)


@JsonClass(generateAdapter = true)
data class Comment_Json(
        val objects: List<Comment>
)