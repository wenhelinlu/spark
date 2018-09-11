package com.lm.ll.spark.adapter.adapterdelegate

import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.lm.ll.spark.R
import com.lm.ll.spark.db.Article
import kotlinx.android.synthetic.main.article_item_simple.view.*


/**
 * 只有文章标题的列表布局（如精华区和经典文库文章）
 * 作者：Created by ll on 2018-07-09 15:37.
 * 邮箱：wenhelinlu@gmail.com
 */
class SimpleArticleListAdapterDelegate(activity: AppCompatActivity) : BaseListAdapterDelegate<ArrayList<Article>>(activity) {
    override fun onCreateViewHolder(parent: ViewGroup?): RecyclerView.ViewHolder {
        val view = inflater.inflate(R.layout.article_item_simple, parent, false)
        view.setOnClickListener(this)
        return ArticleListViewHolder(view)
    }

    override fun isForViewType(items: ArrayList<Article>, position: Int): Boolean {
        return items[position].author.isEmpty() && !items[position].url.isNullOrEmpty() //author没有值时使用此布局
    }

    override fun onBindViewHolder(items: ArrayList<Article>, position: Int, holder: RecyclerView.ViewHolder, payloads: MutableList<Any>) {
        val vh = holder as ArticleListViewHolder
        with(vh) {
            itemView.tag = position
            items[position].let {
                articleTitle.text = it.title
            }
        }
    }

    companion object {
        class ArticleListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var articleItem: LinearLayout = itemView.article_item_simple
            var articleTitle: TextView = itemView.article_title_simple
        }
    }
}