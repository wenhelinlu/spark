package com.lm.ll.spark.adapter

import com.hannesdorfmann.adapterdelegates3.ListDelegationAdapter
import com.lm.ll.spark.db.Article
import io.realm.RealmList
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup


/**
 * 作者：Created by ll on 2018-07-06 17:35.
 * 邮箱：wenhelinlu@gmail.com
 */
class ArticleDelegateAdapter(activity: AppCompatActivity, items: RealmList<Article>) : ListDelegationAdapter<RealmList<Article>>() {

    init {

        // DelegatesManager is a protected Field in ListDelegationAdapter
        delegatesManager.addDelegate(ArticleTextAdapterDelegate(activity))
                .addDelegate(ArticleSpliterAdapterDelegate(activity))
                .addDelegate(ArticleCommentAdapterDelegate(activity))

        // Set the items from super class.
        setItems(items)
    }

    override fun getItemCount(): Int {
        return super.getItemCount()
    }

    override fun getItemViewType(position: Int): Int {
        return super.getItemViewType(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return super.onCreateViewHolder(parent, viewType)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
    }
}