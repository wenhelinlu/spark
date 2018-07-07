package com.lm.ll.spark.adapter

import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.hannesdorfmann.adapterdelegates3.AdapterDelegate
import com.lm.ll.spark.R
import com.lm.ll.spark.db.Article
import io.realm.RealmList


/**
 * 描述：显示正文和评论之间分割条的Adapter
 * 作者：Created by ll on 2018-07-06 17:33.
 * 邮箱：wenhelinlu@gmail.com
 */
class ArticleSpliterAdapterDelegate(activity: AppCompatActivity) : AdapterDelegate<RealmList<Article>>() {
    private var inflater: LayoutInflater = activity.layoutInflater

    override fun onCreateViewHolder(parent: ViewGroup?): RecyclerView.ViewHolder {
        return ArticleSpliterViewHolder(inflater.inflate(R.layout.article_item_spliter, parent, false))
    }

    override fun isForViewType(items: RealmList<Article>, position: Int): Boolean {
        return items[position] == null
    }

    override fun onBindViewHolder(items: RealmList<Article>, position: Int, holder: RecyclerView.ViewHolder, payloads: MutableList<Any>) {

    }

    inner class ArticleSpliterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val divider: View = itemView.findViewById(R.id.viewDivider)
        val commentRemark: TextView = itemView.findViewById(R.id.tvCommentRemark)
    }
}