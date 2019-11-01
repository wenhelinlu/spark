package com.lm.ll.spark.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.WindowManager
import android.widget.Switch
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.viewpager.widget.ViewPager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.lm.ll.spark.R
import com.lm.ll.spark.adapter.ViewPagerAdapter
import com.lm.ll.spark.api.TabooBooksApiService
import com.lm.ll.spark.application.InitApplication
import com.lm.ll.spark.db.Article_Json
import com.lm.ll.spark.db.Comment_Json
import com.lm.ll.spark.db.QueryRecord_Json
import com.lm.ll.spark.fragment.NewsFragment
import com.lm.ll.spark.fragment.SubForumFragment
import com.lm.ll.spark.fragment.VideoFragment
import com.lm.ll.spark.repository.TabooArticlesRepository
import com.lm.ll.spark.util.*
import com.lm.ll.spark.util.GlobalConst.Companion.LOG_TAG_COMMON
import com.lm.ll.spark.util.GlobalConst.Companion.REQUEST_CODE_IMPORT_ARTICLE
import com.lm.ll.spark.util.GlobalConst.Companion.REQUEST_CODE_IMPORT_COMMENT
import com.lm.ll.spark.util.GlobalConst.Companion.REQUEST_CODE_IMPORT_KEYWORD
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider
import com.uber.autodispose.autoDispose
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private var menuItem: MenuItem? = null

    //使用AutoDispose解除RxJava2订阅
    private val scopeProvider by lazy { AndroidLifecycleScopeProvider.from(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val layoutParams = window.attributes
        layoutParams.flags = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS or layoutParams.flags

        setSupportActionBar(toolbar)

        //设置侧栏菜单
        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()
        nav_view.setNavigationItemSelectedListener(this)

        initLoginStatus()

        initNightMode()

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                if (menuItem != null) {
                    menuItem!!.isChecked = false
                } else {
                    navigation.menu.getItem(0).isChecked = false
                }
                menuItem = navigation.menu.getItem(position)
                menuItem!!.isChecked = true
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })

        setupViewPager(viewPager)
    }

    /**
     * @desc 设置ViewPager的Adapter
     * @author ll
     * @time 2018-10-09 11:58
     */
    private fun setupViewPager(viewPager: ViewPager) {
        val adapter = ViewPagerAdapter(supportFragmentManager)

        adapter.addFragment(NewsFragment.newInstance())
        adapter.addFragment(VideoFragment.newInstance())
        adapter.addFragment(SubForumFragment.newInstance())
        viewPager.adapter = adapter
    }


    /**
     * @desc 初始化登录状态
     * @author lm
     * @time 2018-08-26 21:02
     */
    @SuppressLint("CheckResult", "AutoDispose")
    private fun initLoginStatus() {
        val repository = TabooArticlesRepository(TabooBooksApiService.create())
        repository.checkLoginStatus()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnDispose { Log.i("AutoDispose", "Disposing subscription from onCreate()") }
                .autoDispose(scopeProvider) //使用AutoDispose解除RxJava2订阅
                .subscribe(
                        { result ->
                            //根据访问个人信息页面返回的文本中是否包含已登录状态标记文本，判断是否已登录，如果已登录，则不显示登录菜单，如果未登录，则不显示个人信息菜单
                            val menuItem = if (result.contains(GlobalConst.LOGINING_STATUS)) {
                                this.nav_view.menu.findItem(R.id.nav_login)
                            } else {
                                this.nav_view.menu.findItem(R.id.nav_profile)
                            }
                            menuItem.isVisible = false  // true 为显示，false 为隐藏
                        },
                        { error ->
                            //异常处理
                            val msg = getExceptionDesc(error)

                            toast(msg)
                        })
    }

    /**
     * @desc 初始化夜间模式
     * @author lm
     * @time 2018-08-19 12:49
     */
    private fun initNightMode() {
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
     * @desc 点击BottomNavigationView，切换ViewPager中显示的Fragment
     * @author ll
     * @time 2018-10-09 11:00
     */
    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        viewPager.currentItem = item.order
        true
    }


    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_favorite -> {
                this@MainActivity.startActivity(Intent(this@MainActivity, FavoriteArticleListActivity::class.java))
            }
            R.id.nav_elite -> {
                this@MainActivity.startActivity(Intent(this@MainActivity, EliteEroticaArticleListActivity::class.java))
            }
            R.id.nav_classic -> {
                this@MainActivity.startActivity(Intent(this@MainActivity, ClassicEroticaArticleListActivity::class.java))
            }
            R.id.nav_login -> {
                this@MainActivity.startActivity(Intent(this@MainActivity, LoginActivity::class.java))
            }
            R.id.nav_profile -> {
                this@MainActivity.startActivity(Intent(this@MainActivity, PersonProfileActivity::class.java))
            }
            R.id.nav_settings -> {
                this@MainActivity.startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
            }
            R.id.nav_about -> {
                this@MainActivity.startActivity(Intent(this@MainActivity, AboutPageActivity::class.java))
//                this@ArticleListActivity.startActivity(Intent(this@ArticleListActivity,RichTextActivity::class.java))
            }
            R.id.nav_import_articles -> {
                toast("导入文章")
                performFileSearch(REQUEST_CODE_IMPORT_ARTICLE)
            }
            R.id.nav_import_comments -> {
                toast("导入评论")
                performFileSearch(REQUEST_CODE_IMPORT_COMMENT)
            }
            R.id.nav_import_keywords -> {
                toast("导入查询历史")
                performFileSearch(REQUEST_CODE_IMPORT_KEYWORD)
            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().
            data?.data?.also { uri ->
                try {
                    var jsonStr = readTextFromUri(uri)
                    //注意必须是javaObjectType，要不会被擦除泛型
//                    var keywords = Gson()?.fromJson(jsonStr, QueryRecord_Json::class.javaObjectType)
//                    if(keywords != null){
//                        var c = keywords.objects.count()
//                    }
                    val moshi: Moshi = Moshi.Builder().build()

                    when (requestCode) {
                        REQUEST_CODE_IMPORT_KEYWORD -> {
                            val adapter: JsonAdapter<QueryRecord_Json> = moshi.adapter(QueryRecord_Json::class.java)
                            val keywords = adapter.fromJson(jsonStr)
                            if (keywords != null) {
                                //先清空
                                ObjectBox.getQueryRecordBox().removeAll()
                                //插入数据库中
                                keywords.objects.forEach { item ->
                                    run {
                                        //id默认为0，由ObjectBox自动赋值，否则会报错 java.lang.IllegalArgumentException: ID is higher or equal to internal ID sequence: 1 (vs. 1). Use ID 0 (zero) to insert new entities.
                                        item.id = 0
                                        ObjectBox.getQueryRecordBox().put(item)
                                    }
                                }
                            }
                        }
                        REQUEST_CODE_IMPORT_ARTICLE -> {
                            val adapter: JsonAdapter<Article_Json> = moshi.adapter(Article_Json::class.java)
                            val articles = adapter.fromJson(jsonStr)
                            if (articles != null) {
                                //先清空
                                ObjectBox.getArticleBox().removeAll()
                                //插入数据库中
                                articles.objects.forEach { item ->
                                    run {
                                        //id默认为0，由ObjectBox自动赋值，否则会报错 java.lang.IllegalArgumentException: ID is higher or equal to internal ID sequence: 1 (vs. 1). Use ID 0 (zero) to insert new entities.
                                        item.id = 0
                                        ObjectBox.getArticleBox().put(item)
                                    }
                                }
                            }
                        }
                        REQUEST_CODE_IMPORT_COMMENT -> {
                            val adapter: JsonAdapter<Comment_Json> = moshi.adapter(Comment_Json::class.java)
                            val comments = adapter.fromJson(jsonStr)
                            if (comments != null) {
                                //先清空
                                ObjectBox.getCommentBox().removeAll()
                                //插入数据库中
                                comments.objects.forEach { item ->
                                    run {
                                        //id默认为0，由ObjectBox自动赋值，否则会报错 java.lang.IllegalArgumentException: ID is higher or equal to internal ID sequence: 1 (vs. 1). Use ID 0 (zero) to insert new entities.
                                        item.id = 0
                                        ObjectBox.getCommentBox().put(item)
                                    }
                                }
                            }
                        }
                    }

                } catch (t: Throwable) {
                    Log.e(LOG_TAG_COMMON, t.message!!)
                    toast(t.message!!)
                }
            }
        }
    }

    private fun performFileSearch(requestCode: Int) {

        // ACTION_OPEN_DOCUMENT is the intent to choose a file via the system's file
        // browser.
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            // Filter to only show results that can be "opened", such as a
            // file (as opposed to a list of contacts or timezones)
            addCategory(Intent.CATEGORY_OPENABLE)

            // Filter to show only images, using the image MIME data type.
            // If one wanted to search for ogg vorbis files, the type would be "audio/ogg".
            // To search for all documents available via installed storage providers,
            // it would be "*/*".
            type = "*/*"
        }

        startActivityForResult(intent, requestCode)
    }

    @Throws(IOException::class)
    private fun readTextFromUri(uri: Uri): String {
        val stringBuilder = StringBuilder()
        contentResolver.openInputStream(uri)?.use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                var line: String? = reader.readLine()
                while (line != null) {
                    stringBuilder.append(line)
                    line = reader.readLine()
                }
            }
        }
        return stringBuilder.toString()
    }
}
