package com.lm.ll.spark.util

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log

abstract class MyRecyclerViewOnScrollListener(linearLayoutManager: LinearLayoutManager) : RecyclerView.OnScrollListener() {
    private var linearLayoutManager: LinearLayoutManager = linearLayoutManager
    //屏幕上可见的item数量
    private var visibleItemCount: Int = 0
    //已经加载出来的item数量
    private var totalItemCount: Int = 0
    //屏幕上可见的第一个item在整个列表的位置
    private var firstVisibleItemPosition: Int = 0
    //是否正在上拉加载数据中
    private var isLoadingMore: Boolean = false
    //记录之前的数据总数
    private var oldTotal: Int = 0

    /**
     * 滑动状态改变
     *
     * @param recyclerView 当前滚动的 RecyclerView
     * @param newState     当前滚动的状态，有三个值
     *                     public static final int SCROLL_STATE_IDLE = 0;静止没滚动
     *                     public static final int SCROLL_STATE_DRAGGING = 1;用户正在用手指滚动
     *                     public static final int SCROLL_STATE_SETTLING = 2;自动滚动
     */
    override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
        super.onScrollStateChanged(recyclerView, newState)
    }

    /**
     * 正在滑动
     *
     * @param recyclerView 当前滚动的 RecyclerView
     * @param dx           水平滚动距离
     * @param dy           垂直滚动距离
     *                     dx > 0 时为手指向左滚动,列表滚动显示右面的内容
     *                     dx < 0 时为手指向右滚动,列表滚动显示左面的内容
     *                     dy > 0 时为手指向上滚动,列表滚动显示下面的内容
     *                     dy < 0 时为手指向下滚动,列表滚动显示上面的内容
     */
    override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
//        Log.d("LL","dy的值：$dy")
        //向下滑动
        if (dy > 0) {
            visibleItemCount = linearLayoutManager.childCount
            totalItemCount = linearLayoutManager.itemCount
            firstVisibleItemPosition = linearLayoutManager.findFirstVisibleItemPosition()
        }

        //如果正在加载中
        if (isLoadingMore) {
            //说明加载结束
            if (totalItemCount > oldTotal) {
                isLoadingMore = false
                oldTotal = totalItemCount
            }
        }

        Log.d("LL", "visibleItemCount: $visibleItemCount, firstVisibleItemPosition: $firstVisibleItemPosition, totalItemCount: $totalItemCount")
        //如果没有正在加载中，并且当前屏幕上可见item的总数 + 屏幕上可见第一条item位置 >= 目前加载出来的数据总数，表示已经滑到底部
        if (!isLoadingMore && totalItemCount > 0 && (visibleItemCount + firstVisibleItemPosition) >= totalItemCount) {
            isLoadingMore = true
            Log.d("LL", "visibleItemCount load more data")
            loadMoreData()
        }
    }

    public abstract fun loadMoreData()
}