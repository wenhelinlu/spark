package com.lm.ll.spark.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.lm.ll.spark.R
import com.lm.ll.spark.adapter.ArticleAdapter
import com.lm.ll.spark.api.TabooBooksApiService
import com.lm.ll.spark.application.InitApplication
import com.lm.ll.spark.db.Article
import com.lm.ll.spark.db.Article_
import com.lm.ll.spark.decoration.DashLineItemDecoration
import com.lm.ll.spark.net.Spider
import com.lm.ll.spark.repository.TabooArticlesRepository
import com.lm.ll.spark.util.*
import com.lm.ll.spark.util.GlobalConst.Companion.TEXT_IMAGE_SPLITER
import com.lm.ll.spark.util.ObjectBox.getArticleBox
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider
import com.uber.autodispose.autoDispose
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.article_display.*
import kotlinx.android.synthetic.main.bottom_toolbar_text.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import retrofit2.HttpException
import java.net.ConnectException
import java.util.concurrent.TimeoutException
import javax.net.ssl.SSLHandshakeException

/**
 * @desc 使用AdapterDelegates实现Recyclerview的高级布局的方式显示正文
 * @author lm
 * @time 2018-07-07 22:27
 * @email: wenhelinlu@gmail.com
 * @version: 0.1
 */
class ArticleDisplayActivity : CoroutineScopeActivity() {

    //是否是经典情色书库中文章的正文（需要单独解析）
    private var isClassic = false
    //是否需要强制刷新（如果是从我的收藏打开，则直接读取数据库中，否则重新从网上获取）
    private var isForceRefresh = false

    //接收从文章列表传过来的被点击的文章Model
    private lateinit var currentArticle: Article
    //可绑定不同布局的adapter
    private lateinit var adapter: ArticleAdapter
    //使用AutoDispose解除RxJava2订阅
    private val scopeProvider by lazy { AndroidLifecycleScopeProvider.from(this) }
    //RecyclerView的LayoutManager
    private val linearLayoutManager = LinearLayoutManager(this@ArticleDisplayActivity)

    /**
     * @desc 用于延迟触发隐藏状态栏、导航栏等操作
     * @author ll
     * @time 2018-06-14 16:28
     */
    private val mHideHandler = Handler()
    /**
     * @desc 延迟隐藏系统状态栏和导航栏
     * @author ll
     * @time 2018-06-14 15:53
     */
    private val mHidePart2Runnable = Runnable {
        // Delayed removal of status and navigation bar

        // Note that some of these constants are new as of API 16 (Jelly Bean)
        // and API 19 (KitKat). It is safe to use them, as they are inlined
        // at compile-time and do nothing on earlier devices.
        articleLayout.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LOW_PROFILE or
                        View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
    }

    /**
     * @desc 延迟显示toolbar和底部控件
     * @author ll
     * @time 2018-06-14 15:50
     */
    private val mShowPart2Runnable = Runnable {
        // Delayed display of UI elements
        supportActionBar?.show()
        fullscreen_article_controls.visibility = View.VISIBLE
        val animation = AnimationUtils.loadAnimation(this@ArticleDisplayActivity, R.anim.fab_jump_from_down)
        fullscreen_article_controls.startAnimation(animation)
    }

    /**
     * @desc 控制全屏显示
     * @author ll
     * @time 2018-06-14 15:51
     */
    private var mVisible: Boolean = false
    /**
     * @desc 隐藏状态栏和导航栏线程
     * @author ll
     * @time 2018-06-14 15:52
     */
    private val mHideRunnable = Runnable { hide() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.article_display)

        initData()

        initView()

        loadData()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(0)  //default is 100
    }

    /**
     * @desc 覆写此方法，已收藏的文章退出阅读时保存阅读位置到数据库中
     * @author ll
     * @time 2018-07-18 11:45
     */
    override fun onPause() {
        super.onPause()

        //被收藏的文章在退出阅读时才存入阅读位置，之前因为这段代码，以为是Realm的问题导致无法取消收藏
        if (currentArticle.favorite == 1) {
            val position = linearLayoutManager.findFirstVisibleItemPosition()

            //获取与该view的顶部的偏移量
            var offset = 0
            val currentView = linearLayoutManager.findViewByPosition(position)
            if (currentView != null) {
                offset = currentView.top
            }
            currentArticle.leavePosition = position
            currentArticle.offset = offset
            getArticleBox().put(currentArticle)  //更新数据也是put方法
        }
    }

    /**
     * @desc 控制全屏显示
     * @author ll
     * @time 2018-06-14 15:38
     */
    private fun toggle() {
        if (mVisible) {
            hide()
        } else {
            show()
        }
    }

    /**
     * @desc 隐藏状态栏和导航栏，全屏显示
     * @author ll
     * @time 2018-06-14 15:38
     */
    private fun hide() {
        // Hide UI first
        supportActionBar?.hide()
        fullscreen_article_controls.visibility = View.GONE
        val animation = AnimationUtils.loadAnimation(this@ArticleDisplayActivity, R.anim.fab_jump_to_down)
        fullscreen_article_controls.startAnimation(animation)

        mVisible = false

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable)
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    /**
     * @desc 显示状态栏和导航栏
     * @author ll
     * @time 2018-06-14 15:47
     */
    private fun show() {
        // Show the system bar
        articleLayout.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        mVisible = true

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable)
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    /**
     * Schedules a call to hide() in [delayMillis], canceling any
     * previously scheduled calls.
     */
    private fun delayedHide(delayMillis: Int) {
        mHideHandler.removeCallbacks(mHideRunnable)
        mHideHandler.postDelayed(mHideRunnable, delayMillis.toLong())
    }

    companion object {
        /**
         * Whether or not the system UI should be auto-hidden after
         * [AUTO_HIDE_DELAY_MILLIS] milliseconds.
         */
        private const val AUTO_HIDE = true

        /**
         * If [AUTO_HIDE] is set, the number of milliseconds to wait after
         * user interaction before hiding the system UI.
         */
        private const val AUTO_HIDE_DELAY_MILLIS = 3000

        /**
         * Some older devices needs a small delay between UI widget updates
         * and a change of the status and navigation bar.
         */
        private const val UI_ANIMATION_DELAY = 100
    }

    /**
     * @desc 初始化数据
     * @author ll
     * @time 2018-06-07 16:33
     */
    private fun initData() {
        //使用深拷贝，避免打开文章评论后再返回，会把评论的正文赋值给当前正文
        currentArticle = InitApplication.curArticle!!.deepCopy()

        currentArticle.articleFlag = 0  //适用正文item布局

        if (currentArticle.text.isEmpty()) {
            isForceRefresh = true
        }

        //文章来源（普通还是经典书库中的）
        if (currentArticle.classicalFlag == 1) {
            isClassic = true
        }
    }

    /**
     * @desc 初始化视图
     * @author ll
     * @time 2018-06-07 16:34
     */
    private fun initView() {

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mVisible = true

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        toolbar_bottom_article.setOnTouchListener { _, _ ->
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS)
            }
            false
        }

        //跟?结合使用， let函数可以在对象不为 null 的时候执行函数内的代码，从而避免了空指针异常的出现。
        this.supportActionBar?.let {
            it.title = currentArticle.title
        }

        //收藏图标点击事件
        iv_favorite.setOnClickListener {
            //收藏或取消收藏
            if (currentArticle.favorite == 1) {
                currentArticle.favorite = 0
                //从数据库中删除此条数据
                getArticleBox().remove(currentArticle.id)
            } else {
                currentArticle.favorite = 1
                //将数据插入表中
                getArticleBox().put(currentArticle)
            }

            iv_favorite.setImageResource(if (currentArticle.favorite == 1) R.drawable.ic_menu_favorite else R.drawable.ic_menu_unfavorite)
            toast(if (currentArticle.favorite == 1) "收藏成功" else "取消收藏")
        }

        //滚动到正文开始位置
        iv_scrollUp.setOnClickListener {
            this.linearLayoutManager.scrollToPositionWithOffset(0, 0)
        }

        //滚动到正文结束位置
        iv_scrollDown.setOnClickListener {
            this.linearLayoutManager.scrollToPositionWithOffset(this.recyclerViewArticle.adapter!!.itemCount - 1, 0)
        }

        //繁体转换成简体
        iv_translate.setOnClickListener {
            currentArticle.title = currentArticle.title!!.convertToSimplifiedChinese(true)
            currentArticle.text = currentArticle.text.convertToSimplifiedChinese(true)

            iv_translate.setImageResource(R.drawable.ic_menu_translated)

            updateAdapter()
            toast("转换完成")
        }

        //缓存评论列表内容
        iv_cachecomment.setOnClickListener {
            hide()
            val msg = if (currentArticle.commentsCached == 0) "确定要缓存评论列表内容吗?" else "确定要清除评论列表缓存内容吗?"
            try {
                Snackbar.make(articleLayout, msg, Snackbar.LENGTH_LONG)
                        .setAction("确定") { cacheComments() }.show()
            } catch (ex: Exception) {
                toast(ex.message!!)
            }
        }

        //在浏览器中打开
        iv_openInBrowser.setOnClickListener {
            val intent = Intent()
            intent.action = "android.intent.action.VIEW"
            intent.data = Uri.parse(currentArticle.url)
            startActivity(intent)
        }

        //评论列表添加点线分隔线
        this.recyclerViewArticle.addItemDecoration(DashLineItemDecoration(10f, 2))
        this.recyclerViewArticle.layoutManager = linearLayoutManager
    }

    /**
     * @desc 缓存评论列表内容
     * @author Administrator
     * @time 2019-01-30 15:46
     */
    private fun cacheComments() {
        launch {
            withContext(Dispatchers.IO) {
                //缓存或清除缓存
                if (currentArticle.commentsCached == 1) {
                    currentArticle.commentsCached = 0
                    //从数据库中删除当前文章缓存的评论列表数据
                    var commentIds = getArticleBox().query().equal(Article_.parentId, currentArticle.id).build().findIds().toList()
                    getArticleBox().removeByKeys(commentIds)

                } else {
                    currentArticle.commentsCached = 1
                    //将评论列表内容缓存到数据表中
                    val comments = toArticleList(currentArticle, true)
                    //从网络中抓取文章
                    val list = arrayListOf<Article>()
                    comments.forEach {
                        try {
                            val doc = Jsoup.parse(it.url)
                            val item = Spider.scratchText(doc, it)
                            list.add(item)
                        } catch (ex: Exception) {
                        }
                    }
                }
            }
            iv_cachecomment.setImageResource(if (currentArticle.commentsCached == 1) R.drawable.ic_menu_cached else R.drawable.ic_menu_uncache)
            toast(if (currentArticle.commentsCached == 1) "批量缓存成功" else "缓存已清除")
        }
    }

    /**
     * @desc 加载数据
     * @author ll
     * @time 2018-07-10 17:23
     */
    private fun loadData() {
        loadTextWithRx()
    }

    /**
     * @desc 使用RxJava+Retrofit实现异步读取数据
     * @author lm
     * @time 2018-07-01 17:21
     */
    private fun loadTextWithRx() {
        val repository = TabooArticlesRepository(TabooBooksApiService.create())
        repository.getArticle(currentArticle, isClassic, isForceRefresh)
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
                .autoDispose(scopeProvider) //使用AutoDispose解除RxJava2订阅
                .subscribe({ result ->
                    currentArticle = result
                    getReadingStatus()
                }, { error ->
                    //异常处理
                    val msg = getExceptionDesc(error)
                    Snackbar.make(articleLayout, msg, Snackbar.LENGTH_LONG)
                            .setAction("重试") { loadData() }.show()
                })
    }

    /**
     * @desc 获取文章阅读状态
     * @author ll
     * @time 2018-07-10 15:23
     */
    private fun getReadingStatus() {

        //查询此文章是否已收藏（在数据库中存在）
        //注意：之所以这一步不在InitData中操作，是因为已收藏的文章的评论可能会有更新，如果在InitData中直接用数据库中的数据替换，
        //那么，就没有入口来获取最新的文章数据，放在这里，则从主列表打开文章时，会认为是没有收藏过的文章，这样可以加载最新的数据
        val find = getArticleBox().query().equal(Article_.url, currentArticle.url!!).build().findFirst()
        //如果存在，说明此文章已被收藏并存入数据库中
        if (find != null) {
            currentArticle.id = find.id  //id为Long类型，由ObjectBox自动生成
            currentArticle.favorite = 1
            currentArticle.commentsCached = find.commentsCached
            currentArticle.leavePosition = find.leavePosition
            currentArticle.offset = find.offset
        }

        linearLayoutManager.scrollToPositionWithOffset(currentArticle.leavePosition, currentArticle.offset)

        //根据文章收藏状态显示不同的图标
        if (currentArticle.favorite == 1) {
            iv_favorite.setImageResource(R.drawable.ic_menu_favorite)
        } else {
            iv_favorite.setImageResource(R.drawable.ic_menu_unfavorite)
        }
        updateAdapter()
    }

    /**
     * @desc 更新RecyclerView的Adapter
     * @author ll
     * @time 2018-07-10 15:23
     */
    private fun updateAdapter() {
        adapter = ArticleAdapter(this@ArticleDisplayActivity, toArticleList(currentArticle))
        adapter.mItemClickListener = object : ArticleAdapter.Companion.OnItemClickListener {
            override fun onItemClick(view: View) {
                toggle() //点击正文显示或隐藏状态栏和导航栏
            }
        }
        recyclerViewArticle.adapter = adapter
        recyclerViewArticle.adapter!!.notifyDataSetChanged()
    }

    /**
     * @desc 显示进度条
     * @author ll
     * @time 2018-07-10 17:48
     */
    private fun showProgress(show: Boolean) {
        this.pb_loadArticle.visibility = if (show) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    /**
     * @desc 将article中的comment列表转换成article列表，用于使用不同布局的Adapter中
     * @author lm
     * @time 2018-07-07 23:35
     */
    private fun toArticleList(article: Article, onlyComments: Boolean = false): ArrayList<Article> {
        val list = ArrayList<Article>()

        if (!onlyComments) {
            // 正文布局数据
            val text = article.text
            val textList = text.split(TEXT_IMAGE_SPLITER)
            textList.map {
                var a = article.copy()
                a.text = if (it.contains("<img")) {
                    getImgSrc(it)!!
                } else {
                    it
                }
                a.articleFlag = if (it.contains("<img")) {
                    3
                } else {
                    0
                }
                list.add(a)
            }

            // 分割条布局数据
            val spliter = Article()
            spliter.url = null
            spliter.articleFlag = 2
            list.add(spliter)
        }

        // 评论布局数据
        for (comment in article.comments) {
            val temp = Article()

            temp.url = comment.url
            temp.title = comment.title
            temp.textLength = comment.textLength
            temp.author = comment.author
            temp.date = comment.date
            temp.readCount = comment.readCount
            temp.text = comment.text
            temp.articleFlag = 1
            temp.depth = comment.depth

            list.add(temp)
        }
        return list
    }
}