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
import com.lm.ll.spark.util.ARTICLE_TEXT_INTENT_KEY


/**
 * 正文评论列表的Adapter
 * 作者：Created by ll on 2018-06-05 13:18.
 * 邮箱：wenhelinlu@gmail.com
 */
class CommentRecyclerViewAdapter(mContext: Context, articleList: ArrayList<Article>) : RecyclerView.Adapter<CommentRecyclerViewAdapter.CommentListViewHolder>() {

    private val context = mContext
    private val list = articleList

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentRecyclerViewAdapter.CommentListViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.comment_item,parent,false)
        return CommentListViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: CommentRecyclerViewAdapter.CommentListViewHolder, position: Int) {
        with(holder) {
            commentTitle.text = list[position].title

            commentAuthor.text = "${list[position].author}"
            commentDate.text = list[position].date
            commentTextLength.text = list[position].textLength
            commentReadCount.text = list[position].readCount

            commentItem.setOnClickListener {
                val news = list[position]
                val intent = Intent(context, ArticleDisplayActivity::class.java)
                intent.putExtra(ARTICLE_TEXT_INTENT_KEY, news)
                context.startActivity(intent)
            }
        }
    }

    inner class CommentListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val commentItem: ConstraintLayout = itemView.findViewById(R.id.comment_item)
        val commentTitle: TextView = itemView.findViewById(R.id.comment_title)
        val commentAuthor: TextView = itemView.findViewById(R.id.comment_author)
        val commentDate: TextView = itemView.findViewById(R.id.comment_date)
        val commentTextLength: TextView = itemView.findViewById(R.id.comment_textLength)
        val commentReadCount: TextView = itemView.findViewById(R.id.comment_readCount)
    }
}