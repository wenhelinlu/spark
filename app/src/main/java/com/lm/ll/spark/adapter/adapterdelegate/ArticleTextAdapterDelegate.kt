package com.lm.ll.spark.adapter.adapterdelegate

import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.hannesdorfmann.adapterdelegates3.AdapterDelegate
import com.lm.ll.spark.R
import com.lm.ll.spark.db.Article
import com.lm.ll.spark.util.getArticleTextSize
import kotlinx.android.synthetic.main.article_item_text.view.*


/**
 * 描述：显示文章正文数据的Adapter
 * 作者：Created by ll on 2018-07-06 17:31.
 * 邮箱：wenhelinlu@gmail.com
 */
class ArticleTextAdapterDelegate(activity: AppCompatActivity) : AdapterDelegate<ArrayList<Article>>() {
    private var inflater: LayoutInflater = activity.layoutInflater

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        return ArticleTextViewHolder(inflater.inflate(R.layout.article_item_text, parent, false))
    }

    override fun isForViewType(items: ArrayList<Article>, position: Int): Boolean {
        return items[position].articleFlag == 0
    }

    override fun onBindViewHolder(items: ArrayList<Article>, position: Int, holder: RecyclerView.ViewHolder, payloads: MutableList<Any>) {
        val vh = holder as ArticleTextViewHolder
        with(vh) {
            items[position].let {
                articleText.text = it.text
                articleText.setTextSize(TypedValue.COMPLEX_UNIT_SP, getArticleTextSize()) //根据设置的字体大小显示
            }
        }
    }

    companion object {
        class ArticleTextViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val articleText: TextView = itemView.tvText
        }
    }
}