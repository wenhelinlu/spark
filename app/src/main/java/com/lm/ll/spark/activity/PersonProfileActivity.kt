package com.lm.ll.spark.activity

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import com.afollestad.materialdialogs.MaterialDialog
import com.lm.ll.spark.R
import com.lm.ll.spark.adapter.ProfileInfoAdapter
import com.lm.ll.spark.api.TabooBooksApiService
import com.lm.ll.spark.db.ProfileInfo
import com.lm.ll.spark.decoration.SolidLineItemDecoration
import com.lm.ll.spark.http.PersistentCookieJarHelper
import com.lm.ll.spark.repository.TabooArticlesRepository
import com.lm.ll.spark.util.LOG_TAG_COMMON
import com.lm.ll.spark.util.PROFILE_INFO_KEY
import com.lm.ll.spark.util.toast
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider
import com.uber.autodispose.kotlin.autoDisposable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_person_profile.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import retrofit2.HttpException
import java.net.ConnectException
import java.util.concurrent.TimeoutException
import javax.net.ssl.SSLHandshakeException

class PersonProfileActivity : AppCompatActivity() {
    /**
     * @desc 文章列表数据源
     * @author ll
     * @time 2018-08-14 9:53
     */
    private var profileInfoList: ArrayList<ProfileInfo> = ArrayList()

    /**
     * @desc RecyclerView的adapter
     * @author ll
     * @time 2018-08-14 9:53
     */
    private lateinit var mAdapter: ProfileInfoAdapter

    /**
     * @desc 登录成功后的个人档案网页文本
     * @author LL
     * @time 2018-08-24 10:18
     */
    private var profileInfoText: String = ""


    //使用AutoDispose解除RxJava2订阅
    private val scopeProvider by lazy { AndroidLifecycleScopeProvider.from(this) }

    /**
     * @desc RecyclerView的LayoutManager
     * @author ll
     * @time 2018-08-14 9:52
     */
    private val mLinearLayoutManager = LinearLayoutManager(this@PersonProfileActivity)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_person_profile)

        if (intent.hasExtra(PROFILE_INFO_KEY)) {
            profileInfoText = intent.getStringExtra(PROFILE_INFO_KEY)
        }


        val layoutParams = window.attributes
        layoutParams.flags = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS or layoutParams.flags

        setSupportActionBar(toolbar)
        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }

        initView()
        loadDataWithRx()
//        loadData()
    }

    /**
     * @desc 初始化视图
     * @author LL
     * @time 2018-08-24 10:14
     */
    private fun initView() {
        //RecyclerView设置
        this.recyclerViewProfile.addItemDecoration(SolidLineItemDecoration(this@PersonProfileActivity))
        this.recyclerViewProfile.layoutManager = mLinearLayoutManager
        mAdapter = ProfileInfoAdapter(this@PersonProfileActivity, profileInfoList)
        this.recyclerViewProfile.adapter = mAdapter
    }

    /**
     * @desc 加载数据
     * @author LL
     * @time 2018-08-24 10:16
     */
    private fun loadData() {
        val doc = Jsoup.parse(profileInfoText)
        val masthead = doc.select("div.content_list")
        val docTrs = masthead[0].selectFirst("tbody").select("tr")
        for (e in docTrs) {
            val info = ProfileInfo((e.childNode(1) as Element).text(), (e.childNode(2) as Element).text())
            profileInfoList.add(info)
        }

        val accountTrs = masthead[3].selectFirst("tbody").select("tr")
        for (e in accountTrs) {
            if (e.childNodeSize() != 4) {
                continue
            }
            val info = ProfileInfo((e.childNode(1) as Element).text(), (e.childNode(2) as Element).text())
            profileInfoList.add(info)
        }
    }

    /**
     * @desc 加载个人信息
     * @author LL
     * @time 2018-08-27 15:51
     */
    private fun loadDataWithRx() {
        val repository = TabooArticlesRepository(TabooBooksApiService.create())
        repository.getProfileInfo(profileInfoText)
                .firstElement() //如果数据库中有数据，则直接取数据库中数据
                .subscribeOn(Schedulers.io())
                .doOnSubscribe {
                    showProgress(true)
                }
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doAfterTerminate {
                    showProgress(false)
                }
                .doOnDispose { Log.i("AutoDispose", "Disposing subscription from onCreate()") }
                .autoDisposable(scopeProvider) //使用AutoDispose解除RxJava2订阅
                .subscribe({ result ->
                    profileInfoList.clear()
                    profileInfoList.addAll(result)
                    refreshData()
                }, { error ->
                    //异常处理
                    val msg =
                            when (error) {
                                is HttpException, is SSLHandshakeException, is ConnectException -> "网络连接异常"
                                is TimeoutException -> "网络连接超时"
                                is IndexOutOfBoundsException, is ClassCastException -> "解析异常"
                                else -> error.toString()
                            }
                    Snackbar.make(fab, msg, Snackbar.LENGTH_LONG)
                            .setAction("重试") { loadDataWithRx() }.show()
                })
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
        this@PersonProfileActivity.recyclerViewProfile.adapter!!.notifyDataSetChanged()
    }

    /**
     * @desc 显示进度条
     * @author ll
     * @time 2018-07-10 17:48
     */
    private fun showProgress(show: Boolean) {
        this.pb_loadProfileInfo.visibility = if (show) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_person_profile, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item!!.itemId) {
            R.id.action_logout -> {
                MaterialDialog(this).show {
                    title(text = "确认注销")
                    message(text = "确定要注销登录吗？")
                    positiveButton(text = "注销") {
                        logout()
                    }
                    negativeButton(text = "取消")
                }
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * @desc 注销
     * @author lm
     * @time 2018-08-26 20:32
     */
    private fun logout() {
        val repository = TabooArticlesRepository(TabooBooksApiService.create())
        //username=markherd&password=025646Lu&dologin=+%B5%C7%C2%BC+
        repository.logout()
                .subscribeOn(Schedulers.io())
                .doOnSubscribe {
                    showProgress(true)
                }
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doAfterTerminate {
                    showProgress(false)
                }
                .doOnDispose { Log.i("AutoDispose", "Disposing subscription from onCreate()") }
                .autoDisposable(scopeProvider) //使用AutoDispose解除RxJava2订阅
                .subscribe({ result ->
                    Log.d(LOG_TAG_COMMON, "result = $result")
                    PersistentCookieJarHelper.getCookieJar()!!.clear()
                    val intent = Intent(this@PersonProfileActivity, LoginActivity::class.java)
                    this@PersonProfileActivity.startActivity(intent)
                }, { error ->
                    //异常处理
                    val msg =
                            when (error) {
                                is HttpException, is SSLHandshakeException, is ConnectException -> "网络连接异常"
                                is TimeoutException -> "网络连接超时"
                                is IndexOutOfBoundsException, is ClassCastException -> "解析异常"
                                else -> error.toString()
                            }
                    toast(msg)
                })
    }
}
