package com.lm.ll.spark.db

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


/**
 * 描述：记录查询历史
 * 作者：Created by ll on 2018-08-14 14:02.
 * 邮箱：wenhelinlu@gmail.com
 */
@Entity
data class QueryRecord(
        @Id
        var id: Long = 0, //objectbox内部主键
        var keyword: String? = null, //查询关键词
        var queryType: Int = 0, //查询类型（区分以后区分不同论坛内容）
        var insertTime: String = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) //文章收藏入库时间
)
