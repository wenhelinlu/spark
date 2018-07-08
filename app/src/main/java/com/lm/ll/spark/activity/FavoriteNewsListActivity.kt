package com.lm.ll.spark.activity

import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SearchView
import android.view.Menu
import android.view.MenuItem
import com.lm.ll.spark.R
import com.lm.ll.spark.adapter.ArticleListAdapter
import com.lm.ll.spark.db.Article
import com.lm.ll.spark.decoration.SolidLineItemDecoration
import com.vicpin.krealmextensions.querySorted
import io.realm.Sort
import kotlinx.android.synthetic.main.elite_erotica_article_list.*
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async

/**
 * 作者：Created by ll on 2018-06-07 17:15.
 * 邮箱：wenhelinlu@gmail.com
 */
class FavoriteNewsListActivity : AppCompatActivity(), SwipeRefreshLayout.OnRefreshListener {
    override fun onRefresh() {
        this.swipeRefreshEliteList.isRefreshing = false
    }

    //文章列表数据源
    private var articleList: ArrayList<Article> = ArrayList()
    //文章列表adapter
    private lateinit var adapter: ArticleListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.elite_erotica_article_list)

        supportActionBar!!.title = this.getString(R.string.action_favorite)

        val linearLayoutManager = LinearLayoutManager(this@FavoriteNewsListActivity)

        this.recyclerViewEliteList.addItemDecoration(SolidLineItemDecoration(this@FavoriteNewsListActivity))
        this.recyclerViewEliteList.layoutManager = linearLayoutManager

        loadContent()
    }

    /**
     * @desc 加载文章列表
     * @author ll
     * @time 2018-05-29 19:40
     */
    private fun loadContent() {
        val deferredLoad = async(CommonPool) {
            //按插入时间降序排列
            articleList = Article().querySorted("insertTime", Sort.DESCENDING) as ArrayList<Article>
        }

        async(UI) {
            swipeRefreshEliteList.isRefreshing = true
            deferredLoad.await()
            adapter = ArticleListAdapter(this@FavoriteNewsListActivity, articleList)
            this@FavoriteNewsListActivity.recyclerViewEliteList.adapter = adapter
            this@FavoriteNewsListActivity.recyclerViewEliteList.adapter.notifyDataSetChanged()

            //停止刷新
            swipeRefreshEliteList.isRefreshing = false
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.myfavorite, menu)

        val searchView = menu.findItem(R.id.action_search_favorite).actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                (this@FavoriteNewsListActivity.recyclerViewEliteList.adapter as ArticleListAdapter).filter(query)
                return true
            }

            override fun onQueryTextChange(s: String): Boolean {
                (this@FavoriteNewsListActivity.recyclerViewEliteList.adapter as ArticleListAdapter).filter(s)
                return true
            }
        })

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_sort_author -> {
                articleList.sortBy { x -> x.author }
                this.recyclerViewEliteList.adapter.notifyDataSetChanged()
                return true
            }
            R.id.action_sort_title -> {
                articleList.sortBy { x -> x.title }
                this.recyclerViewEliteList.adapter.notifyDataSetChanged()
                return true
            }
            R.id.action_sort_insertDate -> {
                articleList.sortBy { x -> x.insertTime }
                this.recyclerViewEliteList.adapter.notifyDataSetChanged()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
