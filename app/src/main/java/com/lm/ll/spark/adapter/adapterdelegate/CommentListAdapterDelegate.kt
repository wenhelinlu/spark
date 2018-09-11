package com.lm.ll.spark.adapter.adapterdelegate

import android.support.constraint.ConstraintLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.lm.ll.spark.R
import com.lm.ll.spark.application.InitApplication
import com.lm.ll.spark.db.Article
import com.lm.ll.spark.util.getPlaceholder
import kotlinx.android.synthetic.main.article_item.view.*

/**
 * 描述：正文界面中显示评论数据列表的Adapter
 * 作者：Created by ll on 2018-07-06 17:34.
 * 邮箱：wenhelinlu@gmail.com
 */
class CommentListAdapterDelegate(activity: AppCompatActivity) : BaseListAdapterDelegate<ArrayList<Article>>(activity) {
    override fun onCreateViewHolder(parent: ViewGroup?): RecyclerView.ViewHolder {
        val view = inflater.inflate(R.layout.article_item, parent, false)
        view.setOnClickListener(this)
        return ArticleCommentViewHolder(view)
    }

    override fun isForViewType(items: ArrayList<Article>, position: Int): Boolean {
        //是评论（即isArticle=1）且author有值时使用此布局
        return items[position].articleFlag == 1 && !items[position].author.isEmpty()
    }

    override fun onBindViewHolder(items: ArrayList<Article>, position: Int, holder: RecyclerView.ViewHolder, payloads: MutableList<Any>) {
        val vh = holder as ArticleCommentViewHolder
        with(vh) {
            itemView.tag = position
            items[position].let {
                //根据层级设置缩进效果
                commentPlaceholder.text = getPlaceholder(it.depth)
                commentTitle.text = it.title

                //设置评论标题颜色
                commentTitle.setTextColor(InitApplication.getInstance().getColor(R.color.md_blue_grey_700))

                commentAuthor.text = it.author
                commentDate.text = it.date
                commentTextLength.text = it.textLength
                commentReadCount.text = it.readCount
            }
        }
    }

    companion object {
        class ArticleCommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val commentItem: ConstraintLayout = itemView.article_item
            var commentPlaceholder: TextView = itemView.article_placeholder
            val commentTitle: TextView = itemView.article_title
            val commentAuthor: TextView = itemView.article_author
            val commentDate: TextView = itemView.article_date
            val commentTextLength: TextView = itemView.article_textLength
            val commentReadCount: TextView = itemView.article_readCount
        }
    }
}