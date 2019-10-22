package com.lm.ll.spark.adapter

import android.content.Intent
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.hannesdorfmann.adapterdelegates3.ListDelegationAdapter
import com.lm.ll.spark.activity.ArticleDisplayActivity
import com.lm.ll.spark.adapter.adapterdelegate.*
import com.lm.ll.spark.application.InitApplication
import com.lm.ll.spark.db.Article
import com.lm.ll.spark.util.GlobalConst.Companion.IS_CLASSIC_ARTICLE

/**
 * 作者：Created by ll on 2018-07-06 17:35.
 * 邮箱：wenhelinlu@gmail.com
 */
class ArticleAdapter(activity: AppCompatActivity, items: ArrayList<Article>) : ListDelegationAdapter<ArrayList<Article>>() {

    //RecyclerView内部item的自定义单击事件，用于通过点击正文显示或隐藏状态栏和底部工具栏
    lateinit var mItemClickListener: OnItemClickListener

    init {

        val claDelegate = CommentListAdapterDelegate(activity)
        claDelegate.setOnItemClickListener(object : com.lm.ll.spark.listener.OnItemClickListener {
            override fun onItemClick(view: View, position: Int) {
                //如果是从评论列表中打开的链接，则使用curArticleFromCommentList作为跳转传输的中介，而不使用curArticle，
                // 防止从此链接打开的界面返回正文时，会把此链接打开后加载的正文覆盖原始的正文
                InitApplication.curArticle = items[position]
                val intent = Intent(activity, ArticleDisplayActivity::class.java)
                activity.startActivity(intent)
            }
        })

        val salaDelegate = SimpleArticleListAdapterDelegate(activity)
        salaDelegate.setOnItemClickListener(object : com.lm.ll.spark.listener.OnItemClickListener {
            override fun onItemClick(view: View, position: Int) {
                //如果是从评论列表中打开的链接，则使用curArticleFromCommentList作为跳转传输的中介，而不使用curArticle，
                // 防止从此链接打开的界面返回正文时，会把此链接打开后加载的正文覆盖原始的正文
                //TODO 注意：此处可能会存在问题，比如精华区正常列表也使用SimpleArticleListAdapterDelegate，所以还是可能造成混乱，需要再考虑
                InitApplication.curArticle = items[position]
                val intent = Intent(activity, ArticleDisplayActivity::class.java)
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
                .addDelegate(VIEW_TYPE_IMAGE,ArticleImageAdapterDelegate(activity))

        // Set the items from super class.
        setItems(items)
    }

    companion object {
        const val VIEW_TYPE_TEXT = 0 //标识正文item
        const val VIEW_TYPE_SPLITTER = 1 //标识分割条item
        const val VIEW_TYPE_COMMENT = 2 //标识评论item
        const val VIEW_TYPE_COMMENT_SIMPLE = 3  //只有标题的评论项
        const val VIEW_TYPE_IMAGE = 4 //正文中的图片

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