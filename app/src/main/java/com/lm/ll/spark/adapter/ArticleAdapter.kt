package com.lm.ll.spark.adapter

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.hannesdorfmann.adapterdelegates3.ListDelegationAdapter
import com.lm.ll.spark.activity.ArticleDisplayActivity
import com.lm.ll.spark.adapter.adapterdelegate.ArticleSplitterAdapterDelegate
import com.lm.ll.spark.adapter.adapterdelegate.ArticleTextAdapterDelegate
import com.lm.ll.spark.adapter.adapterdelegate.CommentListAdapterDelegate
import com.lm.ll.spark.adapter.adapterdelegate.SimpleArticleListAdapterDelegate
import com.lm.ll.spark.application.InitApplication
import com.lm.ll.spark.db.Article
import com.lm.ll.spark.util.IS_CLASSIC_ARTICLE

/**
 * 作者：Created by ll on 2018-07-06 17:35.
 * 邮箱：wenhelinlu@gmail.com
 */
class ArticleAdapter(activity: AppCompatActivity, items: ArrayList<Article>) : ListDelegationAdapter<ArrayList<Article>>() {

    //Recyclerview内部item的自定义单击事件，用于通过点击正文显示或隐藏状态栏和底部工具栏
    lateinit var mItemClickListener: ArticleAdapter.Companion.OnItemClickListener

    init {

        val claDelegate = CommentListAdapterDelegate(activity)
        claDelegate.setOnItemClickListener(object : com.lm.ll.spark.listener.OnItemClickListener {
            override fun onItemClick(view: View, position: Int) {
                val intent = Intent(activity, ArticleDisplayActivity::class.java)
                InitApplication.curArticle = items[position]
                activity.startActivity(intent)
            }
        })

        val salaDelegate = SimpleArticleListAdapterDelegate(activity)
        salaDelegate.setOnItemClickListener(object : com.lm.ll.spark.listener.OnItemClickListener {
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
        delegatesManager.addDelegate(VIEW_TYPE_TEXT, ArticleTextAdapterDelegate(activity))
                .addDelegate(VIEW_TYPE_SPLITTER, ArticleSplitterAdapterDelegate(activity))
                .addDelegate(VIEW_TYPE_COMMENT, claDelegate)
                .addDelegate(VIEW_TYPE_COMMENT_SIMPLE, salaDelegate)

        // Set the items from super class.
        setItems(items)
    }

    companion object {
        const val VIEW_TYPE_TEXT = 0 //标识正文item
        const val VIEW_TYPE_SPLITTER = 1 //标识分割条item
        const val VIEW_TYPE_COMMENT = 2 //标识评论item
        const val VIEW_TYPE_COMMENT_SIMPLE = 3  //只有标题的评论项

        interface OnItemClickListener {
            fun onItemClick(view: View)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val vh = super.onCreateViewHolder(parent, viewType)
        //点击正文时才控制状态栏和底部工具栏的可见性
        if (viewType == VIEW_TYPE_TEXT) {
            (vh.itemView as ViewGroup).getChildAt(0).setOnClickListener {
                mItemClickListener.onItemClick(it)
            }
        }
        return vh
    }
}