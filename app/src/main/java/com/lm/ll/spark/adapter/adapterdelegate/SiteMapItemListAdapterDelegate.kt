package com.lm.ll.spark.adapter.adapterdelegate

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.hannesdorfmann.adapterdelegates3.AdapterDelegate
import com.lm.ll.spark.R
import com.lm.ll.spark.activity.MainActivity
import com.lm.ll.spark.db.SiteMap
import com.lm.ll.spark.db.SiteMap_
import com.lm.ll.spark.util.ObjectBox
import kotlinx.android.synthetic.main.site_map_item.view.*

/**
 * @desc 论坛列表界面布局数据源绑定
 * @author lm
 * @time 2018-09-09 10:52
 * @email: wenhelinlu@gmail.com
 * @version: 0.1
 */
class SiteMapItemListAdapterDelegate(activity: AppCompatActivity) : AdapterDelegate<ArrayList<SiteMap>>() {
    private val inflater: LayoutInflater = activity.layoutInflater
    private val context = activity.applicationContext

    override fun onCreateViewHolder(parent: ViewGroup?): RecyclerView.ViewHolder {
        return SiteMapItemListViewHolder(inflater.inflate(R.layout.site_map_item, parent, false))
    }

    override fun isForViewType(items: ArrayList<SiteMap>, position: Int): Boolean {
        return true
    }

    override fun onBindViewHolder(items: ArrayList<SiteMap>, position: Int, holder: RecyclerView.ViewHolder, payloads: MutableList<Any>) {
        val vh = holder as SiteMapItemListViewHolder
        with(vh) {
            items[position].let {
                siteMapTitle.text = it.title
                siteMapFavorite.setImageResource(if (it.favorite == 1) R.drawable.ic_menu_favorite else R.drawable.ic_menu_unfavorite)
                siteMapItem.setOnClickListener {
                    val intent = Intent(context, MainActivity::class.java)
//                    InitApplication.curArticle = items[position]
                    context.startActivity(intent)
                }
                //长按收藏或取消收藏
                siteMapItem.setOnLongClickListener {
                    //如果查询记录在数据库中不存在，则插入数据库中
                    val f = ObjectBox.getSiteMapBox().find(SiteMap_.id, items[position].id).first()
                    f.favorite = if (f.favorite == 0) 1 else 0
                    ObjectBox.getSiteMapBox().put(f)
                    siteMapFavorite.setImageResource(if (f.favorite == 1) R.drawable.ic_menu_favorite else R.drawable.ic_menu_unfavorite)
                    true
                }
            }
        }
    }

    companion object {
        class SiteMapItemListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var siteMapItem: LinearLayout = itemView.site_map_item_layout
            var siteMapTitle: TextView = itemView.site_map_item_title
            var siteMapFavorite: ImageView = itemView.site_map_item_favorite
        }
    }
}