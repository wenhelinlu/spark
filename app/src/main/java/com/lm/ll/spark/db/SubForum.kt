package com.lm.ll.spark.db

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

/**
 * @desc 论坛列表
 * @author LL
 * @time 2018-09-08 15:36
 */
@Entity
data class SubForum(
        @Id var id: Long = 0, //ObjectBox内部主键
        var url: String? = null, //url链接
        var title: String? = null, //标题
        var favorite: Int = 0  //是否被收藏, 1表示已收藏，0表示未收藏
)
