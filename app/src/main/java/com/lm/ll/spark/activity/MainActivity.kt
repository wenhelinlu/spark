package com.lm.ll.spark.activity

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.design.widget.Snackbar
import android.support.v4.view.GravityCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import com.lm.ll.spark.R
import com.lm.ll.spark.adapter.ArticleAdapter
import com.lm.ll.spark.application.InitApplication
import com.lm.ll.spark.db.Article
import com.lm.ll.spark.decoration.DashlineItemDecoration
import com.lm.ll.spark.repository.TabooArticlesRepository
import com.lm.ll.spark.util.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, SwipeRefreshLayout.OnRefreshListener {

    override fun onRefresh() {
        this.swipeRefreshTitles.isRefreshing = false
    }

    //文章列表数据源
    private var articleList: ArrayList<Article> = ArrayList()
    //文章列表adapter
    private lateinit var adapter: ArticleAdapter

    //当前加载的页数
    private var currentPage: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val layoutParams = window.attributes
        layoutParams.flags = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS or layoutParams.flags

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

//        fab.setOnClickListener { view ->
//            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                    .setAction("Action", null).show()
//        }


        //若函数参数对应的函数只有一个参数，在使用时，可以省略参数定义，直接使用“it”代替参数
        fab.setOnClickListener {
            Snackbar.make(it, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)

        //下拉刷新进度条颜色
        swipeRefreshTitles.setColorSchemeResources(R.color.colorPrimary, R.color.yellow, R.color.green)
        //触发刷新的下拉距离
        swipeRefreshTitles.setDistanceToTriggerSync(PULL_REFRESH_DISTANCE)
        //下拉刷新监听
        swipeRefreshTitles.setOnRefreshListener {
            loadContent()
        }

        //recyclerview设置
        val linearLayoutManager = LinearLayoutManager(this@MainActivity)
        this.recyclerViewTitles.addItemDecoration(DashlineItemDecoration())
//        this.recyclerViewTitles.addItemDecoration(NewsItemDecoration(2))
        this.recyclerViewTitles.layoutManager = linearLayoutManager

        //上拉加载更多
        recyclerViewTitles.addOnScrollListener(object : MyRecyclerViewOnScrollListener(linearLayoutManager) {
            override fun loadMoreData() {
                currentPage++
                loadContent(true)
            }
        })

        loadContent()
    }


    /**
     * @desc 加载文章列表
     * @author ll
     * @time 2018-05-29 19:40
     * @param isLoadMore 是否是加载更多操作
     */
    private fun loadContent(isLoadMore: Boolean = false) {

        val currentPos: Int = articleList.size

        val deferredLoad = async(CommonPool) {
            //如果下拉刷新，则只抓取第一页内容，否则加载下一页内容
            val pageIndex = if (isLoadMore) currentPage else 1
            val list = Spider.scratchArticleList("$BASE_URL$CURRENT_BASE_URL$pageIndex")

//            Log.d("首次列表size", list.size.toString())

            if (isLoadMore) {
                articleList.addAll(list) //如果是上拉加载更多，则直接将新获取的数据源添加到已有集合中
            } else {
                /**
                 *  如果不是第一次加载，即当前已存在数据，则在新获取的列表中找出和当前已存在的数据列表第一条数据相同
                 *  的数据位置（如果没有找到，则说明新获取的数据列表数据都为新数据，可直接添加当已有集合中），然后将新获取数据列表中
                 *  这个位置之前的数据添加到已有集合中
                 */
                if (articleList.count() > 0) {
                    val firstNews = list.findLast { x -> x.url == articleList[0].url }
                    if (firstNews != null) {
                        val firstIndex = list.indexOf(firstNews)
                        if (firstIndex > 0) {
                            val latest = list.take(firstIndex)
                            articleList.addAll(latest)
                        } else {
                        }
                    } else {
                    }
                } else {
                    articleList = list
                    //如果此时获取的集合数据不超过预定值，则继续加载数据
                    while (articleList.size < MIN_ROWS) {
                        currentPage++
                        val tmpList = Spider.scratchArticleList("$BASE_URL$CURRENT_BASE_URL$currentPage")
                        articleList.addAll(tmpList)
                    }
                }
            }
        }

        async(UI) {
            swipeRefreshTitles.isRefreshing = true
            deferredLoad.await()
//            Log.d("列表size", articleList.size.toString())
            adapter = ArticleAdapter(this@MainActivity, articleList)
            this@MainActivity.recyclerViewTitles.adapter = adapter
            this@MainActivity.recyclerViewTitles.adapter.notifyDataSetChanged()

            //上拉加载后，默认将新获取的数据源的上一行显示在最上面位置
            if (isLoadMore) {
                this@MainActivity.recyclerViewTitles.layoutManager.scrollToPosition(currentPos - 1)
            }

            //停止刷新
            swipeRefreshTitles.isRefreshing = false
        }
    }


    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_nightMode -> {
                var isNightMode = InitApplication.getInstance().isNightModeEnabled()
                isNightMode = !isNightMode
                InitApplication.getInstance().setIsNightModeEnabled(isNightMode)
                switchDayNightMode(isNightMode)
                recreate() //在onCreate方法中设置日、夜间模式后，不需要调用recreate()方法，但是，在其他方法中切换后，需要调用此方法

                return true
            }
            R.id.action_search -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_favorite -> {
                val intent = Intent(this@MainActivity, FavoriteNewsListActivity::class.java)
                this@MainActivity.startActivity(intent)
            }
            R.id.nav_elite -> {
                val intent = Intent(this@MainActivity, EliteEroticaArticleListActivity::class.java)
                this@MainActivity.startActivity(intent)
            }
            R.id.nav_classic -> {
                val intent = Intent(this@MainActivity, ClassicEroticaArticleListActivity::class.java)
                this@MainActivity.startActivity(intent)
            }
            R.id.nav_forum -> {
                val intent = Intent(this@MainActivity, DisplayArticleActivity::class.java)
                this@MainActivity.startActivity(intent)
            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }
}

//TODO: 首页显示论坛列表
//TODO：增加启动Splash
//TODO: 检测网络状态，不通时通过Toast提示
//TODO: 学习Gradle
//TODO：使用MVVM模式


