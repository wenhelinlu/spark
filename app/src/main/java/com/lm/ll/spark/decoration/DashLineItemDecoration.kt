package com.lm.ll.spark.decoration

import android.graphics.*
import android.support.v7.widget.RecyclerView

/**
 * @Desc RecyclerView的虚线分隔线效果
 * @param dashInterval 点线之间距离
 * @param ignoreRows 从第一行开始计数，不需要显示分割线的行数
 */
class DashLineItemDecoration(private val dashInterval: Float = 5f, private val ignoreRows: Int = 0) : RecyclerView.ItemDecoration() {
    override fun onDrawOver(c: Canvas?, parent: RecyclerView?, state: RecyclerView.State?) {
        super.onDrawOver(c, parent, state)
        val left = parent!!.paddingLeft
        val right = parent.width - parent.paddingRight

        val childCount = parent.childCount

        for (i in 0..childCount) {
            val child = parent.getChildAt(i) ?: continue

            //ignoreRows之前的行不需要显示分割线
            if (i < ignoreRows) {
                continue
            }
            val params = child.layoutParams as RecyclerView.LayoutParams

            val top = child.bottom + params.bottomMargin

            val paint = Paint()
            paint.style = Paint.Style.STROKE
            paint.color = Color.GRAY
            val path = Path()
            path.moveTo(left.toFloat(), top.toFloat())
            path.lineTo(right.toFloat(), top.toFloat())

            val effects = DashPathEffect(floatArrayOf(dashInterval, dashInterval, dashInterval, dashInterval), 8f)//此处单位是像素不是dp  注意 请自行转化为dp
            paint.pathEffect = effects
            c!!.drawPath(path, paint)
        }
    }
}