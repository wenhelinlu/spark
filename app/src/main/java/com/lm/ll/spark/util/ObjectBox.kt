package com.lm.ll.spark.util

import android.content.Context
import com.lm.ll.spark.db.MyObjectBox
import io.objectbox.BoxStore


/**
 * BoxStore单例
 * 作者：Created by ll on 2018-07-24 16:59.
 * 邮箱：wenhelinlu@gmail.com
 */
object ObjectBox {
    lateinit var boxStore: BoxStore
        private set

    fun build(context: Context) {
        boxStore = MyObjectBox.builder().androidContext(context.applicationContext).build()
    }
}