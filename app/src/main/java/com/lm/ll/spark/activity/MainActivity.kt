package com.lm.ll.spark.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.core.view.GravityCompat
import android.view.MenuItem
import android.view.WindowManager
import android.widget.Switch
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.lm.ll.spark.R
import com.lm.ll.spark.adapter.ViewPagerAdapter
import com.lm.ll.spark.api.TabooBooksApiService
import com.lm.ll.spark.application.InitApplication
import com.lm.ll.spark.fragment.NewsFragment
import com.lm.ll.spark.fragment.SubForumFragment
import com.lm.ll.spark.fragment.VideoFragment
import com.lm.ll.spark.repository.TabooArticlesRepository
import com.lm.ll.spark.util.GlobalConst
import com.lm.ll.spark.util.getExceptionDesc
import com.lm.ll.spark.util.switchDayNightMode
import com.lm.ll.spark.util.toast
import com.uber.autodispose.AutoDispose.autoDisposable
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider
import com.uber.autodispose.autoDispose
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_rich_text.*
import kotlinx.android.synthetic.main.app_bar_main.*
import retrofit2.HttpException
import java.net.ConnectException
import java.util.concurrent.TimeoutException
import javax.net.ssl.SSLHandshakeException

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
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }
}
