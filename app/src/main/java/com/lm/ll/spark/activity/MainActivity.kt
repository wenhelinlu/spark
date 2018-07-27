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
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import com.lm.ll.spark.R
import com.lm.ll.spark.adapter.ArticleListAdapter
import com.lm.ll.spark.api.TabooBooksApiService
import com.lm.ll.spark.application.InitApplication
import com.lm.ll.spark.db.Article
import com.lm.ll.spark.decoration.SolidLineItemDecoration
import com.lm.ll.spark.repository.TabooArticlesRepository
import com.lm.ll.spark.util.*
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider
import com.uber.autodispose.kotlin.autoDisposable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import retrofit2.HttpException
import java.net.ConnectException
import java.util.*
import java.util.concurrent.TimeoutException
import javax.net.ssl.SSLHandshakeException


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, SwipeRefreshLayout.OnRefreshListener {

    override fun onRefresh() {
        this.swipeRefreshTitles.isRefreshing = false
    }

    //使用AutoDispose解除Rxjava2订阅
    private val scopeProvider by lazy { AndroidLifecycleScopeProvider.from(this) }
    //文章列表数据源
    private var articleList: ArrayList<Article> = ArrayList()
    //文章列表adapter
    private lateinit var adapter: ArticleListAdapter
    //当前加载的页数
    private var currentPage: Int = 1
    //recyclerview的layoutmanager
    private val linearLayoutManager = LinearLayoutManager(this@MainActivity)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val layoutParams = window.attributes
        layoutParams.flags = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS or layoutParams.flags

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        //若函数参数对应的函数只有一个参数，在使用时，可以省略参数定义，直接使用“it”代替参数
        fab.setOnClickListener {
            Snackbar.make(it, "获取最新文章？", Snackbar.LENGTH_LONG)
                    .setAction("刷新") { loadContent() }.show()
        }

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)

        //下拉刷新进度条颜色
        swipeRefreshTitles.setColorSchemeResources(R.color.md_teal_500, R.color.md_orange_500, R.color.md_light_blue_500)
        //触发刷新的下拉距离
        swipeRefreshTitles.setDistanceToTriggerSync(PULL_REFRESH_DISTANCE)
        //下拉刷新监听
        swipeRefreshTitles.setOnRefreshListener {
            loadContent()
//            loadListWithRx()
        }

        //recyclerview设置
        this.recyclerViewTitles.addItemDecoration(SolidLineItemDecoration(this@MainActivity))
        this.recyclerViewTitles.layoutManager = linearLayoutManager

        //上拉加载更多
        recyclerViewTitles.addOnScrollListener(object : MyRecyclerViewOnScrollListener(linearLayoutManager) {
            override fun loadMoreData() {
                currentPage++
                loadContent(true)
//                loadListWithRx(true)
            }
        })

//        loadListWithRx()

        loadContent()
    }


    private fun loadListWithRx(isLoadMore: Boolean = false) {
        val repository = TabooArticlesRepository(TabooBooksApiService.create())

        val getListRx = when {
            currentPage == 1 -> {
                val firstPage = repository.getArticleList("tree$currentPage")
                val secondPage = repository.getArticleList("tree${++currentPage}")
                val thirdPage = repository.getArticleList("tree${++currentPage}")
                Observable.concat(firstPage, secondPage, thirdPage)
            }
            isLoadMore -> repository.getArticleList("tree${++currentPage}")
            else -> repository.getArticleList("tree1") //如果下拉刷新，则只抓取第一页内容
        }

        getListRx
                .subscribeOn(Schedulers.io())
                .doOnSubscribe {
                    //默认情况下，doOnSubscribe执行在subscribe发生的线程，而如果在doOnSubscribe()之后有subscribeOn()的话，它将执行在离它最近的subscribeOn()所指定的线程，所以可以利用此特点，在线程开始前显示进度条等UI操作
                    showProgressbar()
                }
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doAfterTerminate {
                    hideProgressbar()
                }
                .doOnDispose { Log.i("AutoDispose", "Disposing subscription from onCreate()") }
                .autoDisposable(scopeProvider) //使用autodispose解除Rxjava2订阅
                .subscribe({ result ->
                    val currentPos: Int = articleList.size
                    if (isLoadMore) {
                        articleList.addAll(result) //如果是上拉加载更多，则直接将新获取的数据源添加到已有集合中
                    } else {
                        /**
                         *  如果不是第一次加载，即当前已存在数据，则在新获取的列表中找出和当前已存在的数据列表第一条数据相同
                         *  的数据位置（如果没有找到，则说明新获取的数据列表数据都为新数据，可直接添加当已有集合中），然后将新获取数据列表中
                         *  这个位置之前的数据添加到已有集合中
                         */
                        if (articleList.count() > 0) {
                            val firstNews = result.findLast { x -> x.url == articleList[0].url }
                            if (firstNews != null) {
                                val firstIndex = result.indexOf(firstNews)
                                if (firstIndex > 0) {
                                    val latest = result.take(firstIndex)
                                    articleList.addAll(latest)
                                } else {
                                }
                            } else {
                            }
                        } else {
                            articleList = result
                        }
                    }
                    adapter = ArticleListAdapter(this@MainActivity, articleList)
                    this@MainActivity.recyclerViewTitles.adapter = adapter
                    this@MainActivity.recyclerViewTitles.adapter.notifyDataSetChanged()
                    //上拉加载后，默认将新获取的数据源的上一行显示在最上面位置
                    if (isLoadMore) {
                        this@MainActivity.recyclerViewTitles.layoutManager.scrollToPosition(currentPos - 1)
                    }
                }, { error ->
                    //异常处理
                    val msg =
                            when (error) {
                                is HttpException, is SSLHandshakeException,is ConnectException -> "网络连接异常"
                                is TimeoutException -> "网络连接超时"
                                is IndexOutOfBoundsException -> "解析异常"
                                else -> error.toString()
                            }
                    Snackbar.make(this.fab, msg, Snackbar.LENGTH_LONG)
                            .setAction("重试") { loadListWithRx() }.show()
                })
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
                    while (articleList.size < LIST_MIN_COUNT) {
                        currentPage++
                        val tmpList = Spider.scratchArticleList("$BASE_URL$CURRENT_BASE_URL$currentPage")
                        articleList.addAll(tmpList)
                    }
                }
            }
        }

        async(UI) {
            showProgressbar()
            deferredLoad.await()
            adapter = ArticleListAdapter(this@MainActivity, articleList)
            this@MainActivity.recyclerViewTitles.adapter = adapter
            this@MainActivity.recyclerViewTitles.adapter.notifyDataSetChanged()

            //上拉加载后，默认将新获取的数据源的上一行显示在最上面位置
            if (isLoadMore) {
                linearLayoutManager.scrollToPositionWithOffset(currentPos - 1, 0)
            }

            hideProgressbar()
        }
    }

    /**
     * @desc 隐藏加载进度条
     * @author ll
     * @time 2018-07-10 15:17
     */
    private fun hideProgressbar() {
        //停止刷新
        this.swipeRefreshTitles.isRefreshing = false
    }

    /**
     * @desc 显示加载进度条
     * @author ll
     * @time 2018-07-10 17:48
     */
    private fun showProgressbar() {
        swipeRefreshTitles.isRefreshing = true
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
                val intent = Intent(this@MainActivity, ArticleDisplayActivity::class.java)
                this@MainActivity.startActivity(intent)
            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }
}

//TODO: 首页显示论坛列表
//TODO: 检测网络状态，不通时通过Toast提示
//TODO: 学习Gradle
//TODO：使用MVVM模式


