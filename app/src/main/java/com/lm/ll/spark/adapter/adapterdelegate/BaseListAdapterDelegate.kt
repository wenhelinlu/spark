package com.lm.ll.spark.adapter.adapterdelegate

import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.hannesdorfmann.adapterdelegates3.AdapterDelegate
import com.lm.ll.spark.listener.OnItemClickListener
import com.lm.ll.spark.listener.OnItemLongClickListener


/**
 * @desc 支持RecyclerView中Item的Click操作和LongClick操作的AdapterDelegate基类
 * @author LL
 * @time 2018-09-10 17:48
 */
abstract class BaseListAdapterDelegate<T>(activity: AppCompatActivity) : AdapterDelegate<T>(), View.OnClickListener, View.OnLongClickListener {

    var mOnItemClickListener: OnItemClickListener? = null
    var mOnItemLongClickListener: OnItemLongClickListener? = null

    /**
     * @desc 点击事件
     * @author lm
     * @time 2018-09-11 23:08
     */
    override fun onClick(v: View?) {
        if (mOnItemClickListener != null) {
            mOnItemClickListener!!.onItemClick(v!!, v.tag.toString().toInt()) //itemView的tag属性存放position值，即点击的item在数据集合中的位置
        }
    }

    /**
     * @desc 长按事件
     * @author lm
     * @time 2018-09-11 23:08
     */
    override fun onLongClick(v: View?): Boolean {
        if (mOnItemLongClickListener != null) {
            mOnItemLongClickListener!!.onItemLongClick(v!!, v.tag.toString().toInt()) //itemView的tag属性存放position值，即点击的item在数据集合中的位置
        }
        return true
    }

    val inflater: LayoutInflater = activity.layoutInflater

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.mOnItemClickListener = listener
    }

    fun setOnItemLongClickListener(listener: OnItemLongClickListener) {
        this.mOnItemLongClickListener = listener
    }

}