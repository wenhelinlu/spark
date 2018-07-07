package com.lm.ll.spark.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import com.lm.ll.spark.R
import com.lm.ll.spark.adapter.ArticleDelegateAdapter
import com.lm.ll.spark.api.TabooBooksApiService
import com.lm.ll.spark.db.Article
import com.lm.ll.spark.decoration.DashlineItemDecoration
import com.lm.ll.spark.repository.TabooArticlesRepository
import com.lm.ll.spark.util.ARTICLE_TEXT_INTENT_KEY
import com.lm.ll.spark.util.IS_CLASSIC_ARTICLE
import com.vicpin.krealmextensions.delete
import com.vicpin.krealmextensions.save
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.realm.RealmList
import kotlinx.android.synthetic.main.activity_display_article.*
import kotlinx.android.synthetic.main.article_display.*
import kotlinx.android.synthetic.main.bottom_toolbar_text.*
import retrofit2.HttpException
import java.net.ConnectException
import java.util.concurrent.TimeoutException

/**
 * @desc 使用AdapterDelegates实现Recyclerview的高级布局的方式显示正文
 * @author lm
 * @time 2018-07-07 22:27
 * @email: wenhelinlu@gmail.com
 * @version: 0.1
 */
class ArticleDisplayActivity : AppCompatActivity() {

    //是否是经典情色书库中文章的正文（需要单独解析）
    private var isClassic = false
    //是否需要强制刷新（如果是从我的收藏打开，则直接读取数据库中，否则重新从网上获取）
    private var isForceRefresh = false
    //接收从文章列表传过来的被点击的文章Model
    private lateinit var article: Article
    //评论列表adapter
    private lateinit var commentsAdapter: ArticleDelegateAdapter

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
        fullscreen_content_controls.visibility = View.VISIBLE
        val animation = AnimationUtils.loadAnimation(this@ArticleDisplayActivity, R.anim.fab_jump_from_down)
        fullscreen_content_controls.startAnimation(animation)
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

        loadTextWithRx()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(0)  //default is 100
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
        //从列表中传来的点击的标题
        article = this.intent.getParcelableExtra(ARTICLE_TEXT_INTENT_KEY)

        if (article.text.isNullOrEmpty()) {
            isForceRefresh = true
        }

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
            it.title = article.title
        }

//        //点击正文显示或隐藏状态栏和导航栏
//        tvText.setOnClickListener {
//            toggle()
//        }

        //收藏图标点击事件
        iv_favorite.setOnClickListener {

            //收藏或取消收藏
            if (article.isFavorite == 1) {
                article.isFavorite = 0
                Article().delete { equalTo("url", article.url) } //从数据库中删除此条数据
            } else {
                article.isFavorite = 1
                article.save() //将数据插入表中
            }

            iv_favorite.setImageResource(if (article.isFavorite == 1) R.drawable.ic_menu_favorite else R.drawable.ic_menu_unfavorite)
            Toast.makeText(this, if (article.isFavorite == 1) "收藏成功" else "取消收藏", Toast.LENGTH_SHORT).show()
        }

//        //滚动到最顶端
//        iv_scrollUp.setOnClickListener {
//            nestedScrollview.post {
//                nestedScrollview.fling(0) //NestedScrollView需要加此语句才能一键到最顶端
//                nestedScrollview.fullScroll(NestedScrollView.FOCUS_UP)
//            }
//        }
//
//        //滚动到最底端
//        iv_scrollDown.setOnClickListener {
//            nestedScrollview.post {
//                nestedScrollview.fullScroll(NestedScrollView.FOCUS_DOWN)
//            }
//        }

        //在浏览器中打开
        iv_openInBrowser.setOnClickListener {
            val intent = Intent()
            intent.action = "android.intent.action.VIEW"
            intent.data = Uri.parse(article.url)
            startActivity(intent)
        }

        val linearLayoutManager = LinearLayoutManager(this@ArticleDisplayActivity)
        //评论列表添加点线分隔线
        this.recyclerViewArticle.addItemDecoration(DashlineItemDecoration(10f))
        this.recyclerViewArticle.layoutManager = linearLayoutManager
        this.recyclerViewArticle.isNestedScrollingEnabled = false
    }

    /**
     * @desc 使用RxJava+Retrofit实现异步读取数据
     * @author lm
     * @time 2018-07-01 17:21
     */
    private fun loadTextWithRx() {
        val repository = TabooArticlesRepository(TabooBooksApiService.create())
        repository.getArticle(article, isClassic, isForceRefresh)
                .firstElement() //如果数据库中有数据，则直接取数据库中数据
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally {
                    //隐藏进度条
                    pb_loadArticle.visibility = View.GONE
                }
                .subscribe({ result ->
                    article = result
//                    tvText.text = article.text
//
//                    //加载正文后，显示分隔栏
//                    viewDivider.visibility = View.VISIBLE

                    //根据文章收藏状态显示不同的图标
                    if (article.isFavorite == 1) {
                        iv_favorite.setImageResource(R.drawable.ic_menu_favorite)
                    } else {
                        iv_favorite.setImageResource(R.drawable.ic_menu_unfavorite)
                    }

//                    //在正文加载完成后再显示评论区提示
//                    tvCommentRemark.text = this@ArticleDisplayActivity.getString(R.string.comment_remark)


                    commentsAdapter = ArticleDelegateAdapter(this@ArticleDisplayActivity, toArticleList(article))
                    recyclerViewArticle.adapter = commentsAdapter
                    recyclerViewArticle.adapter.notifyDataSetChanged()
                }, { error ->
                    //异常处理
                    when (error) {
                        is HttpException -> Toast.makeText(this, "网络异常", Toast.LENGTH_SHORT).show()
                        is IndexOutOfBoundsException -> Toast.makeText(this, "解析异常", Toast.LENGTH_SHORT).show()
                        is ConnectException -> Toast.makeText(this, "网络连接异常，请稍后重试", Toast.LENGTH_SHORT).show()
                        is TimeoutException -> Toast.makeText(this, "网络连接超时，请稍后重试", Toast.LENGTH_SHORT).show()
                        else -> Toast.makeText(this, error.toString(), Toast.LENGTH_LONG).show()
                    }
                })
    }

    private fun toArticleList(article: Article): RealmList<Article> {
        val list = RealmList<Article>()
        list.add(article)
        list.add(null)
        for (comment in article.comments) {
            val article = Article()

            article.url = comment.url
            article.title = comment.title
            article.textLength = comment.textLength
            article.author = comment.author
            article.date = comment.date
            article.readCount = comment.readCount
            article.text = comment.text
            article.isArticle = 1

            list.add(article)
        }
        return list
    }
}