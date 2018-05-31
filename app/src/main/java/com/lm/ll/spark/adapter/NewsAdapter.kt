package com.lm.ll.spark.adapter

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.lm.ll.spark.R
import com.lm.ll.spark.db.News

/**
 * 作者：Created by ll on 2018-05-28 13:36.
 * 邮箱：wenhelinlu@gmail.com
 */
class NewsAdapter(private val mContext: Context, private val newsList: ArrayList<News>) : RecyclerView.Adapter<NewsAdapter.NewsListViewHolder>(){

    var context = mContext
    var list = newsList

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsListViewHolder {
        var view: View = LayoutInflater.from(context).inflate(R.layout.news_item,parent,false)
        return NewsListViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: NewsListViewHolder, position: Int) {
        holder.newsTitle.text = list[position].title
        holder.newsDesc.text = "${list[position].author}"
        holder.newsTime.text = list[position].date
    }

    inner class NewsListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var newsTitle: TextView
        var newsDesc: TextView
        var newsTime: TextView

        init {
            newsTitle = itemView.findViewById(R.id.news_title)
            newsDesc = itemView.findViewById(R.id.news_desc)
            newsTime = itemView.findViewById(R.id.news_time)
        }
    }

}