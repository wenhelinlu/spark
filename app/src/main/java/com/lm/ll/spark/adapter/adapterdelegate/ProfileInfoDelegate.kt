package com.lm.ll.spark.adapter.adapterdelegate

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.hannesdorfmann.adapterdelegates3.AdapterDelegate
import com.lm.ll.spark.R
import com.lm.ll.spark.db.ProfileInfo
import kotlinx.android.synthetic.main.profile_item.view.*

class ProfileInfoDelegate(activity: AppCompatActivity) : AdapterDelegate<ArrayList<ProfileInfo>>() {
    private val inflater: LayoutInflater = activity.layoutInflater

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        return ProfileInfoViewHolder(inflater.inflate(R.layout.profile_item, parent, false))
    }

    override fun isForViewType(items: ArrayList<ProfileInfo>, position: Int): Boolean {
        return true
    }

    override fun onBindViewHolder(items: ArrayList<ProfileInfo>, position: Int, holder: RecyclerView.ViewHolder, payloads: MutableList<Any>) {
        val vh = holder as ProfileInfoViewHolder
        with(vh) {
            items[position].let {
                title.text = it.title
                info.text = it.value
            }
        }
    }

    companion object {
        class ProfileInfoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var title: TextView = itemView.textProfileTitle
            var info: TextView = itemView.textProfileInfo
        }
    }
}