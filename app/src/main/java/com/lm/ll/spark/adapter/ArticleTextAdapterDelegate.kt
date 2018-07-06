package com.lm.ll.spark.adapter

import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.hannesdorfmann.adapterdelegates3.AdapterDelegate
import com.lm.ll.spark.db.Article
import io.realm.RealmList


/**
 * 作者：Created by ll on 2018-07-06 17:31.
 * 邮箱：wenhelinlu@gmail.com
 */
class ArticleTextAdapterDelegate(activity: AppCompatActivity) : AdapterDelegate<RealmList<Article>>() {
    private lateinit var inflater: LayoutInflater
    init {
        inflater = activity.layoutInflater
    }

    override fun onCreateViewHolder(parent: ViewGroup?): RecyclerView.ViewHolder {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun isForViewType(items: RealmList<Article>, position: Int): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onBindViewHolder(items: RealmList<Article>, position: Int, holder: RecyclerView.ViewHolder, payloads: MutableList<Any>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}