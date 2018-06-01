package com.lm.ll.spark.decoration

import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.view.View


/**
 * 作者：Created by ll on 2018-06-01 11:27.
 * 邮箱：wenhelinlu@gmail.com
 */
class NewsItemDecoration(verticalSpaceHeight: Int) : RecyclerView.ItemDecoration() {

    val height = verticalSpaceHeight

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView , state: RecyclerView.State ) {
            outRect.bottom = height
    }
}