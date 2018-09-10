package com.lm.ll.spark.adapter

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.hannesdorfmann.adapterdelegates3.ListDelegationAdapter
import com.lm.ll.spark.activity.ArticleDisplayActivity
import com.lm.ll.spark.adapter.adapterdelegate.ArticleListAdapterDelegate
import com.lm.ll.spark.adapter.adapterdelegate.SimpleArticleListAdapterDelegate
import com.lm.ll.spark.application.InitApplication
import com.lm.ll.spark.db.Article
import com.lm.ll.spark.util.IS_CLASSIC_ARTICLE
import com.lm.ll.spark.util.OnItemClickListener

/**
 * @desc
 * @author lm
 * @time 2018-07-08 14:26
 * @email: wenhelinlu@gmail.com
 * @version: 0.1
 */
class ArticleListAdapter(activity: AppCompatActivity, items: ArrayList<Article>) : ListDelegationAdapter<ArrayList<Article>>() {
    //列表数据源备份（用于搜索）
    private val listBackup = ArrayList(items)

    init {
        val delegate = ArticleListAdapterDelegate(activity)
        delegate.setOnItemClickListener(object : OnItemClickListener {
            override fun onItemClick(view: View, position: Int) {
                val intent = Intent(activity, ArticleDisplayActivity::class.java)
                InitApplication.curArticle = items[position]
                if (items[position].classicalFlag == 1) {
                    intent.putExtra(IS_CLASSIC_ARTICLE, true)
                }
                activity.startActivity(intent)
            }
        })
        // DelegatesManager is a protected Field in ListDelegationAdapter
        delegatesManager.addDelegate(delegate)
                .addDelegate(SimpleArticleListAdapterDelegate(activity))

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
            items.addAll(listBackup.filter { x -> (x.title!!.contains(text, true) || x.author.contains(text, true)) })
        }
        notifyDataSetChanged()
    }
}