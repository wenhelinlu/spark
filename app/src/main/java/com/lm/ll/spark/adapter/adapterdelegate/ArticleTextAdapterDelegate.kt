package com.lm.ll.spark.adapter.adapterdelegate

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
import kotlinx.android.synthetic.main.article_item_text.view.*


/**
 * 描述：显示文章正文数据的Adapter
 * 作者：Created by ll on 2018-07-06 17:31.
 * 邮箱：wenhelinlu@gmail.com
 */
class ArticleTextAdapterDelegate(activity: AppCompatActivity) : AdapterDelegate<RealmList<Article>>() {
    private var inflater: LayoutInflater = activity.layoutInflater

    override fun onCreateViewHolder(parent: ViewGroup?): RecyclerView.ViewHolder {
        return ArticleTextViewHolder(inflater.inflate(R.layout.article_item_text, parent, false))
    }

    override fun isForViewType(items: RealmList<Article>, position: Int): Boolean {
        return items[position] != null && items[position]!!.isArticle == 0
    }

    override fun onBindViewHolder(items: RealmList<Article>, position: Int, holder: RecyclerView.ViewHolder, payloads: MutableList<Any>) {
        val vh = holder as ArticleTextViewHolder
        with(vh) {
            items[position]?.let {
                articleText.text = it.text
            }
        }
    }

    companion object {
        class ArticleTextViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val articleText: TextView = itemView.tvText
        }
    }
}