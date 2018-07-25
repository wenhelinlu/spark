package com.lm.ll.spark.adapter.adapterdelegate

import android.content.Intent
import android.support.constraint.ConstraintLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.hannesdorfmann.adapterdelegates3.AdapterDelegate
import com.lm.ll.spark.R
import com.lm.ll.spark.activity.ArticleDisplayActivity
import com.lm.ll.spark.application.InitApplication
import com.lm.ll.spark.db.Article
import com.lm.ll.spark.util.IS_CLASSIC_ARTICLE
import kotlinx.android.synthetic.main.article_item.view.*

/**
 * @desc 普通文章列表布局
 * @author lm
 * @time 2018-07-08 14:28
 * @email: wenhelinlu@gmail.com
 * @version: 0.1
 */
class ArticleListAdapterDelegate(activity: AppCompatActivity) : AdapterDelegate<ArrayList<Article>>() {
    private val inflater: LayoutInflater = activity.layoutInflater
    private val context = activity.applicationContext

    override fun onCreateViewHolder(parent: ViewGroup?): RecyclerView.ViewHolder {
        return ArticleListViewHolder(inflater.inflate(R.layout.article_item, parent, false))
    }

    override fun isForViewType(items: ArrayList<Article>, position: Int): Boolean {
        return !items[position].author.isNullOrEmpty()
    }

    override fun onBindViewHolder(items: ArrayList<Article>, position: Int, holder: RecyclerView.ViewHolder, payloads: MutableList<Any>) {
        val vh = holder as ArticleListViewHolder
        with(vh) {
            items[position].let {
                articleTitle.text = it.title

                articleAuthor.text = it.author
                articleDate.text = it.date
                articleTextLength.text = it.textLength
                articleReadCount.text = it.readCount

                articleItem.setOnClickListener {
                    val intent = Intent(context, ArticleDisplayActivity::class.java)
                    InitApplication.curArticle = items[position]
//                    intent.putExtra(ARTICLE_TEXT_INTENT_KEY, items[position])
                    if (items[position].classicalFlag == 1) {
                        intent.putExtra(IS_CLASSIC_ARTICLE, true)
                    }
                    context.startActivity(intent)
                }
            }
        }
    }

    companion object {
        class ArticleListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var articleItem: ConstraintLayout = itemView.article_item
            var articleTitle: TextView = itemView.article_title
            var articleAuthor: TextView = itemView.article_author
            var articleDate: TextView = itemView.article_date
            var articleTextLength: TextView = itemView.article_textLength
            var articleReadCount: TextView = itemView.article_readCount
        }
    }
}