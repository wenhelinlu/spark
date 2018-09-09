package com.lm.ll.spark.adapter

import android.support.v7.app.AppCompatActivity
import com.hannesdorfmann.adapterdelegates3.ListDelegationAdapter
import com.lm.ll.spark.adapter.adapterdelegate.SiteMapItemListAdapterDelegate
import com.lm.ll.spark.db.SiteMap

/**
 * @desc
 * @author lm
 * @time 2018-09-09 11:03
 * @email: wenhelinlu@gmail.com
 * @version: 0.1
 */
class SiteMapItemListAdapter(activity: AppCompatActivity, items: ArrayList<SiteMap>) : ListDelegationAdapter<ArrayList<SiteMap>>() {
    //列表数据源备份（用于搜索）
    private val listBackup = ArrayList(items)

    init {
        // DelegatesManager is a protected Field in ListDelegationAdapter
        delegatesManager.addDelegate(SiteMapItemListAdapterDelegate(activity))

        // Set the items from super class.
        setItems(items)
    }

    /**
     * @desc 实现筛选功能
     * @author ll
     * @time 2018-06-10 17:06
     */
    fun filter(text: String) {
        items.clear()

        if (text.isEmpty()) {
            items.addAll(listBackup)
        } else {
            items.addAll(listBackup.filter { x -> (x.title!!.contains(text, true)) })
        }
        notifyDataSetChanged()
    }
}