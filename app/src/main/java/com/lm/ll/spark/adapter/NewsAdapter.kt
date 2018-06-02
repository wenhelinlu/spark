package com.lm.ll.spark.adapter

import android.content.Context
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
class NewsAdapter(mContext: Context, newsList: ArrayList<News>) : RecyclerView.Adapter<NewsAdapter.NewsListViewHolder>(){

    var context = mContext
    var list = newsList

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsAdapter.NewsListViewHolder {
        val view: View = LayoutInflater.from(context).inflate(R.layout.news_item,parent,false)
        return NewsListViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: NewsAdapter.NewsListViewHolder, position: Int) {
        holder.newsTitle.text = list[position].title
        holder.newsAuthor.text = "${list[position].author}"
        holder.newsDate.text = list[position].date
        holder.newsTextLength.text = list[position].textLength
        holder.newsReadCount.text = list[position].readCount
    }

    inner class NewsListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var newsTitle: TextView = itemView.findViewById(R.id.news_title)
        var newsAuthor: TextView = itemView.findViewById(R.id.news_author)
        var newsDate: TextView = itemView.findViewById(R.id.news_date)
        var newsTextLength: TextView = itemView.findViewById(R.id.news_textLength)
        var newsReadCount: TextView = itemView.findViewById(R.id.news_readCount)
    }

}