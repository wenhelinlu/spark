package com.lm.ll.spark.adapter.adapterdelegate

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.lm.ll.spark.R
import com.lm.ll.spark.application.InitApplication
import com.lm.ll.spark.db.Article
import com.lm.ll.spark.db.Article_
import com.lm.ll.spark.util.ObjectBox.getArticleBox
import kotlinx.android.synthetic.main.article_item.view.*

/**
 * @desc 普通文章列表布局
 * @author lm
 * @time 2018-07-08 14:28
 * @email: wenhelinlu@gmail.com
 * @version: 0.1
 */
class ArticleListAdapterDelegate(activity: AppCompatActivity) : BaseListAdapterDelegate<ArrayList<Article>>(activity) {

    //列表中已收藏文章标题颜色（区分日、夜间模式）
    private val favoriteColor = if (InitApplication.getInstance().isNightModeEnabled()) InitApplication.getInstance().getColor(R.color.md_blue_grey_700) else InitApplication.getInstance().getColor(R.color.md_blue_grey_400)
    //列表中未收藏文章标题颜色（区分日、夜间模式）
    private val unFavoriteColor = if (InitApplication.getInstance().isNightModeEnabled()) InitApplication.getInstance().getColor(R.color.colorTextNight) else InitApplication.getInstance().getColor(R.color.colorTextDay)

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        val view = inflater.inflate(R.layout.article_item, parent, false)
        view.setOnClickListener(this)
        return ArticleListViewHolder(view)
    }

    override fun isForViewType(items: ArrayList<Article>, position: Int): Boolean {
        return !items[position].author.isEmpty()
    }

    override fun onBindViewHolder(items: ArrayList<Article>, position: Int, holder: RecyclerView.ViewHolder, payloads: MutableList<Any>) {
        val vh = holder as ArticleListViewHolder
        with(vh) {
            itemView.tag = position
            items[position].let {
                articleTitle.text = it.title

                articleAuthor.text = it.author
                articleDate.text = it.date
                articleTextLength.text = it.textLength
                articleReadCount.text = it.readCount

                //如果文章已收藏，则单独设置颜色
                val favorite = getArticleBox().query().equal(Article_.url, it.url!!).build().findFirst()
                if (favorite != null) {
                    articleTitle.setTextColor(favoriteColor)
                } else {
                    articleTitle.setTextColor(unFavoriteColor)
                }
            }
        }
    }

    companion object {
        class ArticleListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var articleTitle: TextView = itemView.article_title
            var articleAuthor: TextView = itemView.article_author
            var articleDate: TextView = itemView.article_date
            var articleTextLength: TextView = itemView.article_textLength
            var articleReadCount: TextView = itemView.article_readCount
        }
    }
}