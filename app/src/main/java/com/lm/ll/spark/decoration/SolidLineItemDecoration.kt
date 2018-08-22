package com.lm.ll.spark.decoration

import android.content.Context
import android.graphics.Canvas
import android.support.v7.widget.RecyclerView

/**
 * 作者：Created by ll on 2018-06-01 11:27.
 * 邮箱：wenhelinlu@gmail.com
 */
class SolidLineItemDecoration(mContext: Context) : RecyclerView.ItemDecoration() {
    private val mDivider = mContext.obtainStyledAttributes(ATRRS).getDrawable(0)

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(c, parent, state)
        val left = parent.paddingLeft
        val right = parent.width - parent.paddingRight

        val childCount = parent.childCount

        for (i in 0..childCount) {
            val child = parent.getChildAt(i) ?: continue

            val params = child.layoutParams as RecyclerView.LayoutParams

            val top = child.bottom + params.bottomMargin

            mDivider!!.setBounds(left, top, right, top + mDivider.intrinsicHeight)
            mDivider.draw(c)
        }
    }

    companion object {
        //我们通过获取系统属性中的listDivider来添加，在系统中的AppTheme中设置
        val ATRRS = intArrayOf(android.R.attr.listDivider)
    }
}