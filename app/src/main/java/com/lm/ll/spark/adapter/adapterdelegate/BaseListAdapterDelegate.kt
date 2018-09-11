package com.lm.ll.spark.adapter.adapterdelegate

import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import com.hannesdorfmann.adapterdelegates3.AdapterDelegate
import com.lm.ll.spark.listener.OnItemClickListener


/**
 * @desc 支持RecyclerView中Item的Click操作的AdapterDelegate基类
 * @author LL
 * @time 2018-09-10 17:48
 */
abstract class BaseListAdapterDelegate<T>(activity: AppCompatActivity) : AdapterDelegate<T>(), View.OnClickListener {

    var mOnItemClickListener: OnItemClickListener? = null

    override fun onClick(v: View?) {
        if (mOnItemClickListener != null) {
            mOnItemClickListener!!.onItemClick(v!!, v.tag.toString().toInt()) //itemView的tag属性存放position值，即点击的item在数据集合中的位置
        }
    }

    val inflater: LayoutInflater = activity.layoutInflater

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.mOnItemClickListener = listener
    }

}