package com.lm.ll.spark.activity

import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SearchView
import android.view.Menu
import android.view.MenuItem
import com.lm.ll.spark.R
import com.lm.ll.spark.adapter.ArticleListAdapter
import com.lm.ll.spark.db.Article
import com.lm.ll.spark.db.Article_
import com.lm.ll.spark.decoration.SolidLineItemDecoration
import com.lm.ll.spark.util.GlobalConst.Companion.PULL_REFRESH_DISTANCE
import com.lm.ll.spark.util.ObjectBox
import com.lm.ll.spark.util.toast
import kotlinx.android.synthetic.main.elite_erotica_article_list.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 作者：Created by ll on 2018-06-07 17:15.
 * 邮箱：wenhelinlu@gmail.com
 */
class FavoriteArticleListActivity : CoroutineScopeActivity(), SwipeRefreshLayout.OnRefreshListener {
    override fun onRefresh() {
        showProgress(false)
    }

    //文章列表数据源
    private var articleList: ArrayList<Article> = ArrayList()
    //文章列表adapter
    private lateinit var adapter: ArticleListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.elite_erotica_article_list)

        supportActionBar!!.title = this.getString(R.string.action_favorite)

        //下拉刷新进度条颜色
        swipeRefreshEliteList.setColorSchemeResources(R.color.md_teal_500, R.color.md_orange_500, R.color.md_light_blue_500)
        //触发刷新的下拉距离
        swipeRefreshEliteList.setDistanceToTriggerSync(PULL_REFRESH_DISTANCE)

        val linearLayoutManager = LinearLayoutManager(this@FavoriteArticleListActivity)

        this.recyclerViewEliteList.addItemDecoration(SolidLineItemDecoration(this@FavoriteArticleListActivity))
        this.recyclerViewEliteList.layoutManager = linearLayoutManager

        adapter = ArticleListAdapter(this@FavoriteArticleListActivity, articleList)
        this@FavoriteArticleListActivity.recyclerViewEliteList.adapter = adapter

        loadData()
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    /**
     * @desc 从数据库中读取数据
     * @author Administrator
     * @time 2019-02-01 14:46
     */
    private fun loadData() {
        launch {
            showProgress(true)
            try {
                withContext(Dispatchers.IO) {
                    //注意：Article_.comments中的下划线，这个Article_是ObjectBox内部生成的properties class,即属性类，通过它可以直接获取Article类的各个属性
                    val articles = ObjectBox.getArticleBox().query().orderDesc(Article_.insertTime).build().find()
                    articleList.clear()
                    articleList.addAll(ArrayList(articles))
                }
            } catch (e: Exception) {
                toast("加载失败")
            }

            showProgress(false)
            refreshData()
        }
    }

    /**
     * @desc 更新RecyclerView的Adapter
     * @author ll
     * @time 2018-07-10 15:23
     */
    private fun refreshData() {
        this@FavoriteArticleListActivity.recyclerViewEliteList.adapter!!.notifyDataSetChanged()
    }

    /**
     * @desc 显示进度条
     * @author ll
     * @time 2018-07-10 17:48
     */
    private fun showProgress(show: Boolean) {
        this.swipeRefreshEliteList.isRefreshing = show
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.myfavorite, menu)

        val searchView = menu.findItem(R.id.action_search_favorite).actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                (this@FavoriteArticleListActivity.recyclerViewEliteList.adapter as ArticleListAdapter).filter(query)
                return true
            }

            override fun onQueryTextChange(s: String): Boolean {
                (this@FavoriteArticleListActivity.recyclerViewEliteList.adapter as ArticleListAdapter).filter(s)
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
                refreshData()
                return true
            }
            R.id.action_sort_title -> {
                articleList.sortBy { x -> x.title }
                refreshData()
                return true
            }
            R.id.action_sort_insertDate -> {
                articleList.sortByDescending { x -> x.insertTime } //按插入时间降序排列
                refreshData()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
