package com.lm.ll.spark.adapter.adapterdelegate

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.hannesdorfmann.adapterdelegates3.AdapterDelegate
import com.lm.ll.spark.R
import com.lm.ll.spark.activity.ArticleDisplayActivity
import com.lm.ll.spark.application.InitApplication
import com.lm.ll.spark.db.Article
import com.lm.ll.spark.util.IS_CLASSIC_ARTICLE
import kotlinx.android.synthetic.main.article_item_simple.view.*


/**
 * 只有评论标题的列表布局（如精华区文章中的其他章节链接）
 * 作者：Created by ll on 2018-07-09 17:47.
 * 邮箱：wenhelinlu@gmail.com
 */
class SimpleCommentListAdapterDelegate(activity: AppCompatActivity) : AdapterDelegate<ArrayList<Article>>() {
    private val inflater: LayoutInflater = activity.layoutInflater
    private val context = activity.applicationContext

    override fun onCreateViewHolder(parent: ViewGroup?): RecyclerView.ViewHolder {
        return SimpleCommentListViewHolder(inflater.inflate(R.layout.article_item_simple, parent, false))
    }

    override fun isForViewType(items: ArrayList<Article>, position: Int): Boolean {
        return items[position].author.isEmpty()  //有数据且author没有值时使用此布局
    }

    override fun onBindViewHolder(items: ArrayList<Article>, position: Int, holder: RecyclerView.ViewHolder, payloads: MutableList<Any>) {
        val vh = holder as SimpleCommentListViewHolder
        with(vh) {
            items[position].let {
                articleTitle.text = it.title

                articleItem.setOnClickListener {
                    val intent = Intent(context, ArticleDisplayActivity::class.java)
                    InitApplication.curArticle = items[position]
                    if (items[position].classicalFlag == 1) {
                        intent.putExtra(IS_CLASSIC_ARTICLE, true)
                    }
                    context.startActivity(intent)
                }
            }
        }
    }

    companion object {
        class SimpleCommentListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var articleItem: LinearLayout = itemView.article_item_simple
            var articleTitle: TextView = itemView.article_title_simple
        }
    }
}