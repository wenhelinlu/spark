package com.lm.ll.spark.adapter

import android.content.Context
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.lm.ll.spark.R
import com.lm.ll.spark.db.Article
import com.lm.ll.spark.util.ARTICLE_TEXT_INTENT_KEY

/**
 * 说明：此Adapter用于精华区、论坛列表等只显示title的Recyclerview
 * 作者：Created by ll on 2018-06-10 22:06.
 * 邮箱：wenhelinlu@gmail.com
 */
class SimpleArticleAdapter(mContext: Context, intent: Intent, articleList: ArrayList<Article>) : RecyclerView.Adapter<SimpleArticleAdapter.SimpleNewsListViewHolder>() {

    private val context = mContext
    private val list = articleList
    private var targetIntent: Intent = intent
    //列表数据源备份（用于搜索）
    private val listBackup = ArrayList(articleList)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleArticleAdapter.SimpleNewsListViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.article_item_simple, parent, false)

        return SimpleNewsListViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: SimpleArticleAdapter.SimpleNewsListViewHolder, position: Int) {
        holder.articleTitle.text = list[position].title

        holder.articleItem.setOnClickListener {
            val article = list[position]
            //防止参数重复添加
            if (targetIntent.hasExtra(ARTICLE_TEXT_INTENT_KEY)) {
                targetIntent.removeExtra(ARTICLE_TEXT_INTENT_KEY)
            }

            targetIntent.putExtra(ARTICLE_TEXT_INTENT_KEY, article)
            context.startActivity(targetIntent)
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
            list.addAll(listBackup.filter { x -> x.title!!.contains(text, true) } as ArrayList<Article>)
        }
        notifyDataSetChanged()
    }

    inner class SimpleNewsListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var articleItem: LinearLayout = itemView.findViewById(R.id.article_item_simple)
        var articleTitle: TextView = itemView.findViewById(R.id.article_title)
    }
}