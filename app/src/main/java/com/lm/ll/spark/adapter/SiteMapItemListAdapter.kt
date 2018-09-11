package com.lm.ll.spark.adapter

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import com.hannesdorfmann.adapterdelegates3.ListDelegationAdapter
import com.lm.ll.spark.activity.MainActivity
import com.lm.ll.spark.adapter.adapterdelegate.SiteMapItemListAdapterDelegate
import com.lm.ll.spark.db.SiteMap
import com.lm.ll.spark.db.SiteMap_
import com.lm.ll.spark.listener.OnItemClickListener
import com.lm.ll.spark.listener.OnItemLongClickListener
import com.lm.ll.spark.util.ObjectBox
import com.lm.ll.spark.util.toast

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
        val delegate = SiteMapItemListAdapterDelegate(activity)
        delegate.setOnItemClickListener(object : OnItemClickListener {
            override fun onItemClick(view: View, position: Int) {
                val intent = Intent(activity, MainActivity::class.java)
//                    InitApplication.curArticle = items[position]
                activity.startActivity(intent)
            }
        })

        delegate.setOnItemLongClickListener(object : OnItemLongClickListener {
            override fun onItemLongClick(view: View, position: Int) {
                //如果查询记录在数据库中不存在，则插入数据库中
                val f = ObjectBox.getSiteMapBox().find(SiteMap_.id, items[position].id).first()
                f.favorite = if (f.favorite == 0) 1 else 0
                ObjectBox.getSiteMapBox().put(f)
//                siteMapFavorite.setImageResource(if (f.favorite == 1) R.drawable.ic_menu_favorite else R.drawable.ic_menu_unfavorite)
                Toast.makeText(activity, "", Toast.LENGTH_SHORT).show()
                activity.toast(if (f.favorite == 1) "收藏成功" else "取消收藏")
            }
        })
        // DelegatesManager is a protected Field in ListDelegationAdapter
        delegatesManager.addDelegate(delegate)

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