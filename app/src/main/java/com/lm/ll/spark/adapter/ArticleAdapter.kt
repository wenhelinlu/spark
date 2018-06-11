package com.lm.ll.spark.adapter

import android.content.Context
import android.content.Intent
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.lm.ll.spark.ArticleDisplayActivity
import com.lm.ll.spark.R
import com.lm.ll.spark.db.Article
import com.lm.ll.spark.util.DETAIL_INTENT_KEY

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
        holder.articleTitle.text = list[position].title

        holder.articleAuthor.text = "${list[position].author}"
        holder.articleDate.text = list[position].date
        holder.articleTextLength.text = list[position].textLength
        holder.articleReadCount.text = list[position].readCount

        holder.articleItem.setOnClickListener {
                    val news = list[position]
            val intent = Intent(context, ArticleDisplayActivity::class.java)
            intent.putExtra(DETAIL_INTENT_KEY, news)
                    context.startActivity(intent)
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
        var articleItem: ConstraintLayout = itemView.findViewById(R.id.article_item)
        var articleTitle: TextView = itemView.findViewById(R.id.article_title)
        var articleAuthor: TextView = itemView.findViewById(R.id.article_author)
        var articleDate: TextView = itemView.findViewById(R.id.article_date)
        var articleTextLength: TextView = itemView.findViewById(R.id.article_textLength)
        var articleReadCount: TextView = itemView.findViewById(R.id.article_readCount)
    }
}