package com.lm.ll.spark.listener

import android.view.View

/**
 * @desc RecyclerView中Item点击事件
 * @author lm
 * @time 2018-09-11 22:28
 */
interface OnItemClickListener {
    fun onItemClick(view: View, position: Int)
}