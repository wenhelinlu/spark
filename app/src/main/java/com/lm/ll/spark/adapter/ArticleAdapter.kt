package com.lm.ll.spark.adapter

import android.content.Context
import android.content.Intent
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.lm.ll.spark.R
import com.lm.ll.spark.activity.ArticleDisplayActivity
import com.lm.ll.spark.db.Article
import com.lm.ll.spark.util.ARTICLE_TEXT_INTENT_KEY
import kotlinx.android.synthetic.main.article_item.view.*

/**
 * 作者：Created by ll on 2018-05-28 13:36.
 * 邮箱：wenhelinlu@gmail.com
 */
class ArticleAdapter(mContext: Context, articleList: ArrayList<Article>) : RecyclerView.Adapter<ArticleAdapter.ArticleListViewHolder>() {

    private val context = mContext
    private val list = articleList
    //列表数据源备份（用于搜索）
    private val listBackup = ArrayList(articleList)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleAdapter.ArticleListViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.article_item, parent, false)

        return ArticleListViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ArticleAdapter.ArticleListViewHolder, position: Int) {
        with(holder) {
            list[position].let {
                articleTitle.text = it.title

                articleAuthor.text = it.author
                articleDate.text = it.date
                articleTextLength.text = it.textLength
                articleReadCount.text = it.readCount

                articleItem.setOnClickListener {
                    //                    val intent = Intent(context, DisplayArticleActivity::class.java)
                    val intent = Intent(context, ArticleDisplayActivity::class.java)
                    intent.putExtra(ARTICLE_TEXT_INTENT_KEY, list[position])
                    context.startActivity(intent)
                }
            }
        }
    }

    /**
     * @desc 实现筛选功能
     * @author ll
     * @time 2018-06-10 17:06
     */
    fun filter(text: String) {
        list.clear()

        if (text.isEmpty()) {
            list.addAll(listBackup)
        } else {
            list.addAll(listBackup.filter { x -> (x.title!!.contains(text, true) || x.author!!.contains(text, true)) } as ArrayList<Article>)
        }
        notifyDataSetChanged()
    }

    inner class ArticleListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var articleItem: ConstraintLayout = itemView.article_item
        var articleTitle: TextView = itemView.article_title
        var articleAuthor: TextView = itemView.article_author
        var articleDate: TextView = itemView.article_date
        var articleTextLength: TextView = itemView.article_textLength
        var articleReadCount: TextView = itemView.article_readCount
    }
}