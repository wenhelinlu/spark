package com.lm.ll.spark.listener

import android.view.View

/**
 * @desc RecyclerView的Item长按事件接口
 * @author lm
 * @time 2018-09-11 22:54
 * @email: wenhelinlu@gmail.com
 * @version: 0.1
 */
interface OnItemLongClickListener {
    fun onItemLongClick(view: View, position: Int)
}