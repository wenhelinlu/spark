package com.lm.ll.spark.decoration

import android.graphics.*
import android.support.v7.widget.RecyclerView


/**
 * RecyclerView的虚线分隔线效果
 */
class DashlineItemDecoration : RecyclerView.ItemDecoration() {
    override fun onDrawOver(c: Canvas?, parent: RecyclerView?, state: RecyclerView.State?) {
        super.onDrawOver(c, parent, state)
        val left = parent!!.paddingLeft
        val right = parent.width - parent.paddingRight

        val childCount = parent.childCount

        for (i in 0..childCount) {
            val child = parent.getChildAt(i) ?: continue
            val params = child.layoutParams as RecyclerView.LayoutParams

            val top = child.bottom + params.bottomMargin

            val paint = Paint()
            paint.style = Paint.Style.STROKE
            paint.color = Color.GRAY
            val path = Path()
            path.moveTo(left.toFloat(), top.toFloat())
            path.lineTo(right.toFloat(), top.toFloat())

            val effects = DashPathEffect(floatArrayOf(10f, 10f, 10f, 10f), 8f)//此处单位是像素不是dp  注意 请自行转化为dp
            paint.pathEffect = effects
            c!!.drawPath(path, paint)
        }
    }
}