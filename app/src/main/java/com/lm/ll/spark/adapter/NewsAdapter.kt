package com.lm.ll.spark.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.lm.ll.spark.R
import com.lm.ll.spark.db.News

/**
 * 作者：Created by ll on 2018-05-28 13:36.
 * 邮箱：wenhelinlu@gmail.com
 */
class NewsAdapter(private val mContext: Context, private val newsList: List<News>) : BaseAdapter() {
    private var view: View? = null
    private var viewHolder: ViewHolder? = null

    override fun getCount(): Int {
        return newsList.size
    }

    override fun getItem(position: Int): Any {
        return newsList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        if (convertView ==
                null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.news_item, null)
            viewHolder = ViewHolder()
            viewHolder!!.newsTitle = view!!
                    .findViewById<View>(R.id.news_title) as TextView
            viewHolder!!.newsDesc = view!!.findViewById<View>(R.id.news_desc) as TextView
            viewHolder!!.newsTime = view!!.findViewById<View>(R.id.news_time) as TextView
            view!!.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view!!.tag as ViewHolder
        }
        viewHolder!!.newsTitle!!.setText(newsList[position].title)
        viewHolder!!.newsDesc!!.setText(newsList[position].author)
        viewHolder!!.newsTime!!.text = "来自 : " + newsList[position].date
        return view!!
    }

    internal inner class ViewHolder {
        var newsTitle: TextView? = null
        var newsDesc: TextView? = null
        var newsTime: TextView? = null
    }

}