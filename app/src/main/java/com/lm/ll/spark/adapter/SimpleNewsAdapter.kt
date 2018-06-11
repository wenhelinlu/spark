package com.lm.ll.spark.adapter

import android.content.Context
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.lm.ll.spark.R
import com.lm.ll.spark.db.News
import com.lm.ll.spark.util.DETAIL_INTENT_KEY

/**
 * 说明：此Adapter用于精华区、论坛列表等只显示title的Recyclerview
 * 作者：Created by ll on 2018-06-10 22:06.
 * 邮箱：wenhelinlu@gmail.com
 */
class SimpleNewsAdapter(mContext: Context, intent: Intent, newsList: ArrayList<News>) : RecyclerView.Adapter<SimpleNewsAdapter.SimpleNewsListViewHolder>() {

    private val context = mContext
    private val list = newsList
    private var targetIntent: Intent = intent
    //列表数据源备份（用于搜索）
    private val listBackup = ArrayList(newsList)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleNewsAdapter.SimpleNewsListViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.news_item_simple, parent, false)

        return SimpleNewsListViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: SimpleNewsAdapter.SimpleNewsListViewHolder, position: Int) {
        holder.newsTitle.text = list[position].title

        holder.newsItem.setOnClickListener {
            val news = list[position]
            //防止参数重复添加
            if (targetIntent.hasExtra(DETAIL_INTENT_KEY)) {
                targetIntent.removeExtra(DETAIL_INTENT_KEY)
            }

            targetIntent.putExtra(DETAIL_INTENT_KEY, news)
            context.startActivity(targetIntent)
        }

    }

    /**
     * @desc 实现筛选功能
     * @author ll
     * @time 2018-06-10 17:06
     */
    fun filter(text: String) {
        list.clear()

        if (text.isEmpty()) {
            list.addAll(listBackup)
        } else {
            list.addAll(listBackup.filter { x -> x.title!!.contains(text, true) } as ArrayList<News>)
        }
        notifyDataSetChanged()
    }

    inner class SimpleNewsListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var newsItem: LinearLayout = itemView.findViewById(R.id.news_item_simple)
        var newsTitle: TextView = itemView.findViewById(R.id.news_title)
    }
}