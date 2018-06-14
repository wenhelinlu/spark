package com.lm.ll.spark

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.ScrollView
import android.widget.Toast
import com.lm.ll.spark.adapter.CommentRecyclerViewAdapter
import com.lm.ll.spark.db.Article
import com.lm.ll.spark.decoration.DashlineItemDecoration
import com.lm.ll.spark.util.ARTICLE_TEXT_INTENT_KEY
import com.lm.ll.spark.util.IS_CLASSIC_ARTICLE
import com.lm.ll.spark.util.Spider
import com.vicpin.krealmextensions.delete
import com.vicpin.krealmextensions.query
import com.vicpin.krealmextensions.save
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_display_article.*
import kotlinx.android.synthetic.main.bottom_toolbar_text.*
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async

/**
 * 显示文章正文
 * 作者：Created by ll on 2018-06-13 16:56.
 * 邮箱：wenhelinlu@gmail.com
 */
class DisplayArticleActivity : AppCompatActivity() {

    //是否是经典情色书库中文章的正文（需要单独解析）
    private var isClassic = false
    //接收从文章列表传过来的被点击的文章Model
    private lateinit var article: Article
    //此文章下的首层评论
    private var comments: ArrayList<Article> = ArrayList()
    //评论列表adapter
    private lateinit var commentsAdapter: CommentRecyclerViewAdapter

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
        fullscreen_content.systemUiVisibility =
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
        fullscreen_content_controls.visibility = View.VISIBLE
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
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private val mDelayHideTouchListener = View.OnTouchListener { _, _ ->
        if (DisplayArticleActivity.AUTO_HIDE) {
            delayedHide(DisplayArticleActivity.AUTO_HIDE_DELAY_MILLIS)
        }
        false
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_display_article)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mVisible = true

        // Set up the user interaction to manually show or hide the system UI.
        tvText.setOnClickListener { toggle() }

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        toolbar_bottom_text.setOnTouchListener(mDelayHideTouchListener)

        Realm.init(this)

        initData()

        initView()

        loadText()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100)
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
        fullscreen_content_controls.visibility = View.GONE
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
        fullscreen_content.systemUiVisibility =
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
        private const val UI_ANIMATION_DELAY = 300
    }

    /**
     * @desc 初始化数据
     * @author ll
     * @time 2018-06-07 16:33
     */
    private fun initData() {
        //从列表中传来的点击的标题
        article = this.intent.getParcelableExtra(ARTICLE_TEXT_INTENT_KEY)

        //文章来源（普通还是经典书库中的）
        if (this.intent.hasExtra(IS_CLASSIC_ARTICLE)) {
            isClassic = this.intent.getBooleanExtra(IS_CLASSIC_ARTICLE, false)
        }
    }

    /**
     * @desc 初始化视图
     * @author ll
     * @time 2018-06-07 16:34
     */
    private fun initView() {

        //跟?结合使用， let函数可以在对象不为 null 的时候执行函数内的代码，从而避免了空指针异常的出现。
        this.supportActionBar?.let {
            it.title = article.title
        }

        //点击正文显示或隐藏状态栏和导航栏
        tvText.setOnClickListener {
            toggle()
        }

        //收藏图标点击事件
        iv_favorite.setOnClickListener {

            //收藏或取消收藏
            if (article.isFavorited == 1) {
                article.isFavorited = 0
                Article().delete { equalTo("url", article.url) } //从数据库中删除此条数据
            } else {
                article.isFavorited = 1
                article.save() //将数据插入表中
            }

            iv_favorite.setImageResource(if (article.isFavorited == 1) R.drawable.ic_menu_favorited else R.drawable.ic_menu_unfavorite)
            Toast.makeText(this, if (article.isFavorited == 1) "收藏成功" else "取消收藏", Toast.LENGTH_SHORT).show()
        }

        //滚动到最顶端
        iv_scrollUp.setOnClickListener {
            scrollviewText.post {
                scrollviewText.fullScroll(ScrollView.FOCUS_UP)
            }
        }

        //滚动到最底端
        iv_scrollDown.setOnClickListener {
            scrollviewText.post {
                scrollviewText.fullScroll(ScrollView.FOCUS_DOWN)
            }
        }

        //在浏览器中打开
        iv_openInBrowser.setOnClickListener {
            val intent = Intent()
            intent.action = "android.intent.action.VIEW"
            intent.data = Uri.parse(article.url)
            startActivity(intent)
        }

        val linearLayoutManager = LinearLayoutManager(this@DisplayArticleActivity)
        //评论列表添加点线分隔线
        this.recyclerViewComment.addItemDecoration(DashlineItemDecoration())
        this.recyclerViewComment.layoutManager = linearLayoutManager
        this.recyclerViewComment.isNestedScrollingEnabled = false
    }

    /**
     * @desc 加载文章正文和评论
     * @author ll
     * @time 2018-05-29 19:40
     */
    private fun loadText() {
        val deferredLoad = async(CommonPool) {
            val spider = Spider()
            if (isClassic) { //经典文库的文章解析方式不同
                article = spider.scratchClassicEroticaArticleText(article)
            } else {
                article = spider.scratchText(article, comments) //正文中可能也包含链接（比如精华区）
                comments.reverse() //因为在精华区中，章节链接是倒序显示，所以将其翻转
                comments.addAll(spider.scratchComments(article))
            }
        }

        async(UI) {

            //如果正文有内容，则说明是从本地读取的，不需要再从网上抓取
            if (article.text == null || article.text.toString().isEmpty()) {
                deferredLoad.await()
                //如果是从主页打开的链接，则查询此条数据在数据库中是否存在
                val find = query<Article> {
                    equalTo("url", article.url)
                }.firstOrNull()
                //如果存在，说明此文章已被收藏并存入数据库中
                if (find != null) {
                    article.isFavorited = 1
                }
            }

            tvText.text = article.text

            //加载正文后，显示分隔栏
            viewDivider.visibility = View.VISIBLE

            //根据文章收藏状态显示不同的图标
            if (article.isFavorited == 1) {
                iv_favorite.setImageResource(R.drawable.ic_menu_favorited)
            } else {
                iv_favorite.setImageResource(R.drawable.ic_menu_unfavorite)
            }

            //在正文加载完成后再显示评论区提示
            tvCommentRemark.text = this@DisplayArticleActivity.getString(R.string.comment_remark)

            commentsAdapter = CommentRecyclerViewAdapter(this@DisplayArticleActivity, comments)
            recyclerViewComment.adapter = commentsAdapter
            recyclerViewComment.adapter.notifyDataSetChanged()
        }
    }
}
