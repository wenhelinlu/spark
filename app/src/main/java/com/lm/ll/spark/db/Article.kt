package com.lm.ll.spark.db

import com.squareup.moshi.JsonClass
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.relation.ToMany
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


/**
 * Created by ll on 2018-05-24 17:23.
 */
@Entity
@JsonClass(generateAdapter = true)
data class Article(
        @Id var id: Long = 0, //ObjectBox内部主键
        var url: String? = null, //url链接
        var title: String? = null, //标题
        var author: String = "", //作者
        var date: String = "", //文章发表日期
        var textLength: String = "", //文章字数
        var readCount: String = "", //阅读数
        var text: String = "", //文章正文
        var insertTime: String = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), //文章收藏入库时间
        var favorite: Int = 0,  //是否被收藏, 1表示已收藏，0表示未收藏
        var commentsCached: Int = 0, //评论列表是否被缓存,1表示已缓存，0表示未缓存
        var parentId: Long = -1, //父Id，对于被缓存的评论列表来说，此值存储的是文章的id，这样如果撤销缓存，可以根据此值快速从数据表中删除
        var articleFlag: Int = 0, //是否是Article，0表示Article正文文本，1表示Comment，2表示正文和评论列表间的分隔栏，3表示正文中的图标标签（包含<img的文本）
        var classicalFlag: Int = 0, //是否是经典书库文章，0表示否，1表示是（解析正文方式不同）
        var leavePosition: Int = 0, //当前RecyclerView中第一个可见的item的位置
        var offset: Int = 0, //与该view的顶部的偏移量
        var depth: Int = 0 //列表层级，控制评论列表的缩进

){
    lateinit var comments: ToMany<Comment> //此文章的评论列表
}


@JsonClass(generateAdapter = true)
data class Article_Json(
        val objects: List<Article>
)