package com.lm.ll.spark.activity

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.lm.ll.spark.R
import com.lm.ll.spark.api.TabooBooksApiService
import com.lm.ll.spark.application.InitApplication
import com.lm.ll.spark.db.Article
import com.lm.ll.spark.repository.TabooArticlesRepository
import com.lm.ll.spark.util.getExceptionDesc
import com.lm.ll.spark.util.getImgSrc
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider
import com.uber.autodispose.autoDisposable
import com.uber.autodispose.autoDispose
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_rich_text.*
import org.jsoup.Jsoup
import retrofit2.HttpException
import java.net.ConnectException
import java.util.concurrent.TimeoutException
import javax.net.ssl.SSLHandshakeException


/**
 * @desc 支持图文混排的页面
 * @author LL
 * @time 2018-09-06 15:36
 */
class RichTextActivity : AppCompatActivity() {

    //接收从文章列表传过来的被点击的文章Model
    private lateinit var currentArticle: Article
    //使用AutoDispose解除RxJava2订阅
    private val scopeProvider by lazy { AndroidLifecycleScopeProvider.from(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rich_text)

        initData()
        loadData()
    }

    /**
     * @desc 初始化数据
     * @author lm
     * @time 2018-09-16 21:23
     */
    private fun initData() {
        //从列表中传来的点击的标题
        currentArticle = InitApplication.curArticle!!
    }

    /**
     * @desc 加载数据
     * @author lm
     * @time 2018-09-16 21:23
     */
    private fun loadData() {
        loadDataWithRx()
    }

    /**
     * @desc 使用Rxjava加载数据
     * @author lm
     * @time 2018-09-16 21:23
     */
    private fun loadDataWithRx() {
        val repository = TabooArticlesRepository(TabooBooksApiService.create())
//        val url = "https://site.6parker.com/chan1/index.php?app=forum&act=threadview&tid=14170357"
        repository.getRichTextArticle(currentArticle.url!!)
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
                    val doc = Jsoup.parse(result)
                    val list = ArrayList<String>()
                    for (text in list) {
                        if (text.contains("<img") && text.contains("src=")) {
                            //imagePath可能是本地路径，也可能是网络地址
                            val imagePath = getImgSrc(text)
                            tv_note_content.addImageViewAtIndex(tv_note_content.lastIndex, imagePath)
                        } else {
                            tv_note_content.addTextViewAtIndex(tv_note_content.lastIndex, text)
                        }
                    }

                }, { error ->
                    //异常处理
                    val msg = getExceptionDesc(error)

                    Snackbar.make(richContentLayout, msg, Snackbar.LENGTH_LONG)
                            .setAction("重试") { loadData() }.show()
                })
    }

    /**
     * @desc 显示进度条
     * @author ll
     * @time 2018-07-10 17:48
     */
    private fun showProgress(show: Boolean) {
        this.pb_loadContent.visibility = if (show) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }
}
