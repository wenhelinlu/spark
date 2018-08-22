package com.lm.ll.spark.util

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView

abstract class MyRecyclerViewOnScrollListener(private var linearLayoutManager: LinearLayoutManager) : RecyclerView.OnScrollListener() {
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
    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
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

        //如果没有正在加载中，并且当前屏幕上可见item的总数 + 屏幕上可见第一条item位置 >= 目前加载出来的数据总数，表示已经滑到底部
        if (!isLoadingMore && totalItemCount > 0 && (visibleItemCount + firstVisibleItemPosition) >= totalItemCount) {
            isLoadingMore = true
            loadMoreData()
        }
    }

    abstract fun loadMoreData()
}

//class OnScrollListener(val layoutManager: LinearLayoutManager, val adapter: RecyclerView.Adapter<RecyclerAdapter.ViewHolder>, val dataList: MutableList<Int>) : RecyclerView.OnScrollListener() {
//    var previousTotal = 0
//    var loading = true
//    val visibleThreshold = 10
//    var firstVisibleItem = 0
//    var visibleItemCount = 0
//    var totalItemCount = 0
//
//    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//        super.onScrolled(recyclerView, dx, dy)
//
//        visibleItemCount = recyclerView.childCount
//        totalItemCount = layoutManager.itemCount
//        firstVisibleItem = layoutManager.findFirstVisibleItemPosition()
//
//        if (loading) {
//            if (totalItemCount > previousTotal) {
//                loading = false
//                previousTotal = totalItemCount
//            }
//        }
//
//        if (!loading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)) {
//            val initialSize = dataList.size
//            updateDataList(dataList)
//            val updatedSize = dataList.size
//            recyclerView.post { adapter.notifyItemRangeInserted(initialSize, updatedSize) }
//            loading = true
//        }
//    }
//}