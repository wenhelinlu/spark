package com.lm.ll.spark.adapter.adapterdelegate

import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hannesdorfmann.adapterdelegates3.AdapterDelegate
import com.lm.ll.spark.R
import com.lm.ll.spark.db.Article


/**
 * 描述：显示正文和评论之间分割条的Adapter
 * 作者：Created by ll on 2018-07-06 17:33.
 * 邮箱：wenhelinlu@gmail.com
 */
class ArticleSplitterAdapterDelegate(activity: AppCompatActivity) : AdapterDelegate<ArrayList<Article>>() {
    private var inflater: LayoutInflater = activity.layoutInflater

    override fun onCreateViewHolder(parent: ViewGroup?): RecyclerView.ViewHolder {
        return ArticleSplitterViewHolder(inflater.inflate(R.layout.article_item_splitter, parent, false))
    }

    override fun isForViewType(items: ArrayList<Article>, position: Int): Boolean {
        return items[position].articleFlag == 2 && !items[position].url.isNullOrEmpty()
    }

    override fun onBindViewHolder(items: ArrayList<Article>, position: Int, holder: RecyclerView.ViewHolder, payloads: MutableList<Any>) {

    }

    inner class ArticleSplitterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        //分割条固定内容，不需要显示
    }
}