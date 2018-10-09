package com.lm.ll.spark.adapter.adapterdelegate

import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.lm.ll.spark.R
import com.lm.ll.spark.db.SubForum
import kotlinx.android.synthetic.main.sub_forum_item.view.*

/**
 * @desc 论坛列表界面布局数据源绑定
 * @author lm
 * @time 2018-09-09 10:52
 * @email: wenhelinlu@gmail.com
 * @version: 0.1
 */
class SubForumItemListAdapterDelegate(activity: AppCompatActivity) : BaseListAdapterDelegate<ArrayList<SubForum>>(activity) {

    override fun onCreateViewHolder(parent: ViewGroup?): RecyclerView.ViewHolder {
        val view = inflater.inflate(R.layout.sub_forum_item, parent, false)
        view.setOnClickListener(this)
        view.setOnLongClickListener(this)

        return SiteMapItemListViewHolder(view)
    }

    override fun isForViewType(items: ArrayList<SubForum>, position: Int): Boolean {
        return true
    }

    override fun onBindViewHolder(items: ArrayList<SubForum>, position: Int, holder: RecyclerView.ViewHolder, payloads: MutableList<Any>) {
        val vh = holder as SiteMapItemListViewHolder
        vh.itemView.tag = position
        with(vh) {
            itemView.tag = position
            items[position].let {
                siteMapTitle.text = it.title
//                siteMapTitle.setCompoundDrawables(if (it.favorite == 1) R.drawable.ic_menu_favorite else R.drawable.ic_menu_unfavorite_grey)
                siteMapFavorite.setImageResource(if (it.favorite == 1) R.drawable.ic_menu_favorite else R.drawable.ic_menu_unfavorite_grey)
            }
        }
    }

    companion object {
        class SiteMapItemListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var siteMapTitle: TextView = itemView.site_map_item_title
            var siteMapFavorite: ImageView = itemView.site_map_item_favorite
        }
    }
}