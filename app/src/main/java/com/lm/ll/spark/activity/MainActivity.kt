package com.lm.ll.spark.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.design.widget.Snackbar
import android.support.v4.view.GravityCompat
import android.support.v4.widget.SimpleCursorAdapter
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SearchView
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import android.widget.Switch
import com.google.firebase.analytics.FirebaseAnalytics
import com.lm.ll.spark.R
import com.lm.ll.spark.adapter.ArticleListAdapter
import com.lm.ll.spark.application.InitApplication
import com.lm.ll.spark.db.Article
import com.lm.ll.spark.db.QueryRecord
import com.lm.ll.spark.db.QueryRecord_
import com.lm.ll.spark.decoration.SolidLineItemDecoration
import com.lm.ll.spark.enum.ForumType
import com.lm.ll.spark.http.PersistentCookieHelper
import com.lm.ll.spark.util.*
import com.lm.ll.spark.util.ObjectBox.getQueryRecordBox
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.withContext
import java.net.URLEncoder


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, SwipeRefreshLayout.OnRefreshListener {

    override fun onRefresh() {
        hideProgressbar()
    }

    private lateinit var mFirebaseAnalytics: FirebaseAnalytics

    /**
     * @desc 文章列表数据源
     * @author ll
     * @time 2018-08-14 9:53
     */
    private var articleList: ArrayList<Article> = ArrayList()
    /**
     * @desc 文章列表数据源备份（用于在查询前将articleList备份，然后退出查询时恢复原有的文章列表数据）
     * @author ll
     * @time 2018-08-14 9:53
     */
    private var articleListBackup: ArrayList<Article> = ArrayList()
    /**
     * @desc RecyclerView的adapter
     * @author ll
     * @time 2018-08-14 9:53
     */
    private lateinit var mAdapter: ArticleListAdapter

    /**
     * @desc 当前加载的页数
     * @author ll
     * @time 2018-08-14 9:53
     */
    private var currentPage: Int = 1

    /**
     * @desc RecyclerView的LayoutManager
     * @author ll
     * @time 2018-08-14 9:52
     */
    private val linearLayoutManager = LinearLayoutManager(this@MainActivity)

    /**
     * @desc 标识是否处于查询状态中
     * @author ll
     * @time 2018-08-16 15:20
     */
    private var isQueryStatus = false

    /**
     * @desc 查询结果当前页码
     * @author ll
     * @time 2018-08-16 9:29
     */
    private var queryCurrentPage = 1
    /**
     * @desc 加密后的get请求查询参数
     * @author ll
     * @time 2018-08-16 10:27
     */
    private var encodedKeyword = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Obtain the FirebaseAnalytics instance.
        this.mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)

        val layoutParams = window.attributes
        layoutParams.flags = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS or layoutParams.flags

        setSupportActionBar(toolbar)

        //若函数参数对应的函数只有一个参数，在使用时，可以省略参数定义，直接使用“it”代替参数
        fab.setOnClickListener { it ->
            if (isQueryStatus) {
                Snackbar.make(it, "当前处于查询状态，此操作不可用", Snackbar.LENGTH_LONG)
                        .setAction("了解") { }.show()
            } else {
                Snackbar.make(it, "获取最新文章？", Snackbar.LENGTH_LONG)
                        .setAction("刷新") { loadData(::getArticleList) }.show()
            }
        }

        //设置侧栏菜单
        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()
        nav_view.setNavigationItemSelectedListener(this)

//        initLoginStatus()

        initNightMode()

        //下拉刷新进度条颜色
        swipeRefreshTitles.setColorSchemeResources(R.color.md_teal_500, R.color.md_orange_500, R.color.md_light_blue_500)
        //触发刷新的下拉距离
        swipeRefreshTitles.setDistanceToTriggerSync(PULL_REFRESH_DISTANCE)
        //下拉刷新监听
        swipeRefreshTitles.setOnRefreshListener {
            if (isQueryStatus) {
                //查询过程中屏蔽下拉刷新操作
                hideProgressbar()
            } else {
                loadData(::getArticleList)
            }
        }

        //RecyclerView设置
        this.recyclerViewTitles.addItemDecoration(SolidLineItemDecoration(this@MainActivity))
        this.recyclerViewTitles.layoutManager = linearLayoutManager
        mAdapter = ArticleListAdapter(this@MainActivity, articleList)
        this.recyclerViewTitles.adapter = mAdapter

        //上拉加载更多
        recyclerViewTitles.addOnScrollListener(object : MyRecyclerViewOnScrollListener(linearLayoutManager) {
            override fun loadMoreData() {
                if (isQueryStatus) {
                    queryCurrentPage++
                    loadData(::queryArticleList, true)
                } else {
                    currentPage++
                    loadData(::getArticleList, true)
                }
            }
        })
        loadData(::getArticleList)
    }

    /**
     * @desc 初始化登录状态
     * @author lm
     * @time 2018-08-26 21:02
     */
    private fun initLoginStatus() {
        val helper = PersistentCookieHelper(InitApplication.getInstance())

        //根据本地是否有cookie值，判断是否已登录，如果已登录，则不显示登录菜单，如果未登录，则不显示个人信息菜单
        val menuItem = if ((helper["home.6park.com"] ?: ArrayList()).count() == 0) {
            this.nav_view.menu.findItem(R.id.nav_profile)
        } else {
            this.nav_view.menu.findItem(R.id.nav_login)
        }
        menuItem.isVisible = false  // true 为显示，false 为隐藏
    }

    /**
     * @desc 初始化夜间模式
     * @author lm
     * @time 2018-08-19 12:49
     */
    private fun initNightMode(){
        val switchItem = nav_view.menu.findItem(R.id.nav_nightMode_switch)
        val switch = switchItem.actionView.findViewById<Switch>(R.id.switchNightMode)
        val isNightMode = InitApplication.getInstance().isNightModeEnabled()
        switch.isChecked = isNightMode

        switch.setOnCheckedChangeListener { _, isChecked ->
            InitApplication.getInstance().setIsNightModeEnabled(isChecked)
            switchDayNightMode(isChecked)
            recreate() //在onCreate方法中设置日、夜间模式后，不需要调用recreate()方法，但是，在其他方法中切换后，需要调用此方法
        }
    }

    /**
     * @desc 加载数据
     * @author ll
     * @time 2018-08-13 20:59
     * @param download 函数类型参数，实际的下载方法
     * @param isLoadMore 是否是加载更多数据
     */
    private fun loadData(download: (page: Int) -> ArrayList<Article>, isLoadMore: Boolean = false) {
        val currentPos: Int = articleList.size

        async(UI) {
            showProgressbar()
            withContext(CommonPool) {
                //如果下拉刷新，则只抓取第一页内容，否则加载下一页内容
                var pageIndex = if (isLoadMore) if (isQueryStatus) queryCurrentPage else currentPage else 1
                val list = download(pageIndex)

                //Log.d(LOG_TAG_COMMON, "isLoadMore = $isLoadMore, pageIndex = $pageIndex, list'size = ${list.size}")

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
                        articleList.clear()
                        articleList.addAll(list)
                        //如果此时获取的集合数据不超过预定值，则继续加载数据
                        while (articleList.size < LIST_MIN_COUNT) {
                            pageIndex = if (isQueryStatus) ++queryCurrentPage else ++currentPage
                            val tmpList = download(pageIndex)
                            articleList.addAll(tmpList)
                        }
                    }
                }
            }
            refreshData()

            //上拉加载后，默认将新获取的数据源的上一行显示在最上面位置
            if (isLoadMore) {
                linearLayoutManager.scrollToPositionWithOffset(currentPos - 1, 0)
            }

            hideProgressbar()
        }
    }

    /**
     * @desc 根据页码获取文章列表，注意：11页之前（不包含第11页）的url和第11页及之后的url不同
     * @author lm
     * @time 2018-07-28 15:50
     * @param pageIndex 页码
     */
    private fun getArticleList(pageIndex: Int): ArrayList<Article> {

        //11页之前（不包含第11页）的url和第11页及之后的url不同
        val url = if (pageIndex <= 10) {
            "$BASE_URL$CURRENT_BASE_URL$pageIndex"
        } else {
            "${BASE_URL}index.php?app=forum&act=list&pre=55764&nowpage=$pageIndex&start=55764"
        }
//        Log.d(LOG_TAG_COMMON, url)
        return Spider.scratchArticleList(url)
    }

    /**
     * @desc 根据关键词和页码获取查询结果
     * @author ll
     * @time 2018-08-13 20:57
     * @param pageIndex 查询结果页码
     */
    private fun queryArticleList(pageIndex: Int): ArrayList<Article> {
        val url = "https://www.cool18.com/bbs4/index.php?action=search&bbsdr=life6&act=threadsearch&app=forum&keywords=$encodedKeyword&submit=%B2%E9%D1%AF&p=$pageIndex"
        return Spider.scratchQueryArticles(url)
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

    /**
     * @desc 刷新数据
     * @author ll
     * @time 2018-08-14 9:44
     */
    private fun refreshData() {
        /**
         * 注意，如果此adapter绑定的数据源articleList重新赋值了，则表示此数据源在内存中的地址改变，adapter会认为原数据源没有改变，
         * 此时调用notifyDataSetChanged()方法不起作用，必须重新绑定数据源才可以。
         * 解决方法是不直接给articleList赋新值，而是调用articleList的addAll()方法（视情况而定，可以先clear），这样adapter的
         * notifyDataSetChanged()方法就会起作用，列表可以正常刷新
         */
        this@MainActivity.recyclerViewTitles.adapter!!.notifyDataSetChanged()
    }

    /**
     * @desc 查询操作
     * @author ll
     * @time 2018-08-16 10:33
     */
    private fun queryArticle(keyword: String) {

        addAnalysisData(keyword)
        //初始化查询状态及备份原有数据
        isQueryStatus = true
        articleListBackup.clear()
        articleListBackup.addAll(articleList)

        articleList.clear()
        //get请求中，因留园网为gb2312编码，所以中文参数以gb2312字符集编码（okhttp默认为utf-8编码）
        encodedKeyword = URLEncoder.encode(keyword, "gb2312")
        //查询结果页码重置为1
        queryCurrentPage = 1
        loadData(::queryArticleList)
    }

    /**
     * @desc 退出查询状态，恢复原有数据
     * @author ll
     * @time 2018-08-16 10:58
     */
    private fun quitQueryStatus() {
        isQueryStatus = false
        articleList.clear()
        articleList.addAll(articleListBackup)
        refreshData()
    }


    /**
     * @desc 保存查询记录到数据库中
     * @author ll
     * @time 2018-08-14 15:52
     */
    private fun saveQueryRecord(keyword: String) {
        //如果查询记录在数据库中不存在，则插入数据库中
        val f = getQueryRecordBox().find(QueryRecord_.keyword, keyword)
        if (f == null || f.count() == 0) {
            val record = QueryRecord()
            record.keyword = keyword
            record.queryType = ForumType.TABOO_BOOK.ordinal
            getQueryRecordBox().put(record)
        }
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    @SuppressLint("RestrictedApi")
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)

        val searchView = menu.findItem(R.id.action_search).actionView as SearchView
        val mSearchAutoComplete = searchView.findViewById(R.id.search_src_text) as SearchView.SearchAutoComplete
        //设置触发查询的最少字符数（默认2个字符才会触发查询）
        mSearchAutoComplete.threshold = 0

        //初始状态下显示全部查询历史记录
        var list = getQueryRecord()
        var cursor = getQueryRecordCursor(list)
        searchView.suggestionsAdapter = SimpleCursorAdapter(this@MainActivity, R.layout.search_item, cursor, arrayOf("text"), intArrayOf(R.id.searchKeyword), SimpleCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER)

        //查询记录项事件监听
        searchView.setOnSuggestionListener(object : SearchView.OnSuggestionListener {
            override fun onSuggestionSelect(position: Int): Boolean {
                return false
            }

            //点击事件，触发查询操作
            override fun onSuggestionClick(position: Int): Boolean {
                searchView.setQuery(list[position].keyword, true)
                searchView.clearFocus()
                return true
            }
        })

        //查询关键词事件监听
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            /**
             * @desc 提交查询事件
             * @author ll
             * @time 2018-08-14 9:50
             */
            override fun onQueryTextSubmit(query: String): Boolean {
                //关键词不为空时执行查询操作
                if (!query.isBlank()) {
                    saveQueryRecord(query)
                    queryArticle(query)
                }

                return true
            }

            /**
             * @desc 查询文本改变事件
             * @author ll
             * @time 2018-08-14 9:50
             */
            override fun onQueryTextChange(s: String): Boolean {
                //关键词清空时恢复原有数据列表
                if (s.isBlank()) {
                    quitQueryStatus()
                }

                list = getQueryRecord(s)
                //根据输入的文本过滤查询历史记录
                cursor = getQueryRecordCursor(list)
                // 如果适配器已经存在，则只需要更新适配器中的cursor对象即可。
                searchView.suggestionsAdapter.changeCursor(cursor)

                return true
            }
        })

        return true
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_favorite -> {
                val intent = Intent(this@MainActivity, FavoriteArticleListActivity::class.java)
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

            R.id.nav_login -> {
                val intent = Intent(this@MainActivity, LoginActivity::class.java)
                this@MainActivity.startActivity(intent)
            }
            R.id.nav_profile -> {
                val intent = Intent(this@MainActivity, PersonProfileActivity::class.java)
                this@MainActivity.startActivity(intent)
            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    /**
     * @desc 添加统计信息
     * @author LL
     * @time 2018-08-27 11:00
     */
    private fun addAnalysisData(keyword: String) {
        try {
            val bundle = Bundle()
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "1")
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "linlu")
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "查询")
            bundle.putString(FirebaseAnalytics.Event.SEARCH, keyword)
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

//TODO: 首页显示论坛列表
//TODO: 检测网络状态，不通时通过Toast提示
//TODO: 学习Gradle
//TODO：使用MVVM模式


