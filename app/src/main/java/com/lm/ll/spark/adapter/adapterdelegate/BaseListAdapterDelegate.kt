package com.lm.ll.spark.adapter.adapterdelegate

import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import com.hannesdorfmann.adapterdelegates3.AdapterDelegate
import com.lm.ll.spark.util.OnItemClickListener


abstract class BaseListAdapterDelegate<T>(activity: AppCompatActivity) : AdapterDelegate<T>(), View.OnClickListener {

    var mOnItemClickListener: OnItemClickListener? = null

    override fun onClick(v: View?) {
        if (mOnItemClickListener != null) {
            mOnItemClickListener!!.onItemClick(v!!, v.tag.toString().toInt())
        }
    }

    val inflater: LayoutInflater = activity.layoutInflater
    val context = activity.applicationContext

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.mOnItemClickListener = listener
    }

}