package com.lm.ll.spark.adapter

import android.content.Intent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.hannesdorfmann.adapterdelegates3.ListDelegationAdapter
import com.lm.ll.spark.activity.ArticleListActivity
import com.lm.ll.spark.adapter.adapterdelegate.SubForumItemListAdapterDelegate
import com.lm.ll.spark.db.SubForum
import com.lm.ll.spark.listener.OnItemClickListener
import com.lm.ll.spark.listener.OnItemLongClickListener
import com.lm.ll.spark.util.GlobalConst.Companion.SUB_FORUM_TITLE
import com.lm.ll.spark.util.GlobalConst.Companion.SUB_FORUM_URL
import com.lm.ll.spark.util.ObjectBox
import com.lm.ll.spark.util.toast

/**
 * @desc
 * @author lm
 * @time 2018-09-09 11:03
 * @email: wenhelinlu@gmail.com
 * @version: 0.1
 */
class SubForumItemListAdapter(activity: AppCompatActivity, items: ArrayList<SubForum>) : ListDelegationAdapter<ArrayList<SubForum>>() {
    //列表数据源备份（用于搜索）
    private val listBackup = ArrayList(items)

    init {
        val delegate = SubForumItemListAdapterDelegate(activity)
        delegate.setOnItemClickListener(object : OnItemClickListener {
            override fun onItemClick(view: View, position: Int) {
                val intent = Intent(activity, ArticleListActivity::class.java)
                intent.putExtra(SUB_FORUM_URL, items[position].url)
                intent.putExtra(SUB_FORUM_TITLE, items[position].title)
                activity.startActivity(intent)
            }
        })

        delegate.setOnItemLongClickListener(object : OnItemLongClickListener {
            override fun onItemLongClick(view: View, position: Int) {
                //更改收藏状态并更新到数据库中
                val f = items[position]
                f.favorite = if (f.favorite == 0) 1 else 0
                ObjectBox.getSubForumBox().put(f)
                items[position] = f
                notifyDataSetChanged()
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