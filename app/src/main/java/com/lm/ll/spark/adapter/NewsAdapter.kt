package com.lm.ll.spark.adapter

import android.content.Context
import android.content.Intent
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.lm.ll.spark.NewsDisplayActivity
import com.lm.ll.spark.R
import com.lm.ll.spark.db.News
import com.lm.ll.spark.util.DETAIL_INTENT_KEY

/**
 * 作者：Created by ll on 2018-05-28 13:36.
 * 邮箱：wenhelinlu@gmail.com
 */
class NewsAdapter(mContext: Context, newsList: ArrayList<News>) : RecyclerView.Adapter<NewsAdapter.NewsListViewHolder>(){

    private val context = mContext
    private val list = newsList
    //列表数据源备份（用于搜索）
    private val listBackup = ArrayList(newsList)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsAdapter.NewsListViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.news_item,parent,false)

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

        holder.newsItem.setOnClickListener {
                    val news = list[position]
                    val intent = Intent(context, NewsDisplayActivity::class.java)
            intent.putExtra(DETAIL_INTENT_KEY, news)
                    context.startActivity(intent)
        }

    }

    /**
     * @desc 实现筛选功能
     * @author ll
     * @time 2018-06-10 17:06
     */
    fun filter(text: String) {
        list.clear()

        if (text.isNullOrEmpty()) {
            list.addAll(listBackup)
        } else {
            list.addAll(listBackup.filter { x -> (x.title!!.contains(text, true) || x.author!!.contains(text, true)) } as ArrayList<News>)
        }
        notifyDataSetChanged()
    }

    inner class NewsListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var newsItem: ConstraintLayout = itemView.findViewById(R.id.news_item)
        var newsTitle: TextView = itemView.findViewById(R.id.news_title)
        var newsAuthor: TextView = itemView.findViewById(R.id.news_author)
        var newsDate: TextView = itemView.findViewById(R.id.news_date)
        var newsTextLength: TextView = itemView.findViewById(R.id.news_textLength)
        var newsReadCount: TextView = itemView.findViewById(R.id.news_readCount)
    }
}