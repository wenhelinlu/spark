package com.lm.ll.spark.adapter.adapterdelegate

import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.bumptech.glide.request.RequestOptions
import com.hannesdorfmann.adapterdelegates3.AdapterDelegate
import com.lm.ll.spark.R
import com.lm.ll.spark.db.Article
import kotlinx.android.synthetic.main.article_item_image.view.*

/**
 * 作者：Created by ll on 2018-09-21 14:46.
 * 邮箱：wenhelinlu@gmail.com
 */
class ArticleImageAdapterDelegate(activity: AppCompatActivity) : AdapterDelegate<ArrayList<Article>>() {
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
                Glide.with(articleImage.context)
                        .load(it.text)
                        .apply(requestOptions)
                        .transition(withCrossFade()) //渐显效果
                        .into(articleImage)
            }
        }
    }

    companion object {
        class ArticleImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val articleImage: ImageView = itemView.article_image
        }

        //Glide参数
        lateinit var requestOptions: RequestOptions
            private set
    }

    init {
        requestOptions = RequestOptions()

        requestOptions.fitCenter()
        requestOptions.override(900, 900)
        requestOptions.placeholder(R.drawable.ic_placeholder_900dp)
        requestOptions.error(R.drawable.ic_image_error)
        requestOptions.diskCacheStrategy(DiskCacheStrategy.ALL)
    }
}