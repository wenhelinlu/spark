package com.lm.ll.spark.adapter.adapterdelegate

import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.hannesdorfmann.adapterdelegates3.AdapterDelegate
import com.lm.ll.spark.R
import com.lm.ll.spark.db.Article
import kotlinx.android.synthetic.main.article_item_image.view.*


/**
 * 作者：Created by ll on 2018-09-21 14:46.
 * 邮箱：wenhelinlu@gmail.com
 */
class ArticleImageAdapterDelegate (activity: AppCompatActivity) : AdapterDelegate<ArrayList<Article>>() {
    private var inflater: LayoutInflater = activity.layoutInflater


    override fun onCreateViewHolder(parent: ViewGroup?): RecyclerView.ViewHolder {
        return ArticleImageViewHolder(inflater.inflate(R.layout.article_item_image, parent, false))
    }

    override fun isForViewType(items: ArrayList<Article>, position: Int): Boolean {
        return items[position].articleFlag == 3
    }

    override fun onBindViewHolder(items: ArrayList<Article>, position: Int, holder: RecyclerView.ViewHolder, payloads: MutableList<Any>) {
        val vh = holder as ArticleImageViewHolder
        with(vh) {
            items[position].let {
                //TODO 使用Glide加载图片
                Glide.with(articleImage.context)
                        .load(it.text)
                        .into(articleImage)
            }
        }
    }

    companion object {
        class ArticleImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val articleImage: ImageView = itemView.article_image
        }
    }
}