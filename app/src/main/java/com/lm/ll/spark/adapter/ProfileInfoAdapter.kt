package com.lm.ll.spark.adapter

import androidx.appcompat.app.AppCompatActivity
import com.hannesdorfmann.adapterdelegates3.ListDelegationAdapter
import com.lm.ll.spark.adapter.adapterdelegate.ProfileInfoDelegate
import com.lm.ll.spark.db.ProfileInfo

class ProfileInfoAdapter(activity: AppCompatActivity, items: ArrayList<ProfileInfo>) : ListDelegationAdapter<ArrayList<ProfileInfo>>() {

    init {

        // DelegatesManager is a protected Field in ListDelegationAdapter
        delegatesManager.addDelegate(VIEW_TYPE_TEXT, ProfileInfoDelegate(activity))

        // Set the items from super class.
        setItems(items)
    }

    companion object {
        const val VIEW_TYPE_TEXT = 0 //标识正文item
        const val VIEW_TYPE_SPLITTER = 1 //标识分割条item
        const val VIEW_TYPE_COMMENT = 2 //标识评论item
        const val VIEW_TYPE_COMMENT_SIMPLE = 3  //只有标题的评论项
    }
}