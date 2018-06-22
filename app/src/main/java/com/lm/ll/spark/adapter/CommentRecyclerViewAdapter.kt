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
import com.lm.ll.spark.activity.DisplayArticleActivity
import com.lm.ll.spark.db.Comment
import com.lm.ll.spark.util.ARTICLE_TEXT_INTENT_KEY
import io.realm.RealmList


/**
 * 正文评论列表的Adapter
 * 作者：Created by ll on 2018-06-05 13:18.
 * 邮箱：wenhelinlu@gmail.com
 */
class CommentRecyclerViewAdapter(mContext: Context, articleList: RealmList<Comment>) : RecyclerView.Adapter<CommentRecyclerViewAdapter.CommentListViewHolder>() {

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
            list[position]?.let {
                commentTitle.text = it.title

                commentAuthor.text = it.author
                commentDate.text = it.date
                commentTextLength.text = it.textLength
                commentReadCount.text = it.readCount

                commentItem.setOnClickListener {
                    val intent = Intent(context, DisplayArticleActivity::class.java)
                    intent.putExtra(ARTICLE_TEXT_INTENT_KEY, list[position]!!.toArticle())
                    context.startActivity(intent)
                }
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