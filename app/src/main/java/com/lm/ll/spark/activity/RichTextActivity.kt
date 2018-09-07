package com.lm.ll.spark.activity

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.lm.ll.spark.R
import com.lm.ll.spark.api.TabooBooksApiService
import com.lm.ll.spark.db.Article
import com.lm.ll.spark.repository.TabooArticlesRepository
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider
import com.uber.autodispose.kotlin.autoDisposable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.article_display.*
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

        loadData()
    }

    private fun loadData(){
        loadDataWithRx()
    }

    private fun loadDataWithRx(){
        val repository = TabooArticlesRepository(TabooBooksApiService.create())
        val url = "https://site.6parker.com/chan1/index.php?app=forum&act=threadview&tid=14170357"
        repository.getRichTextArticle(url)
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

                }, { error ->
                    //异常处理
                    val msg =
                            when (error) {
                                is HttpException, is SSLHandshakeException, is ConnectException -> "网络连接异常"
                                is TimeoutException -> "网络连接超时"
                                is IndexOutOfBoundsException, is ClassCastException -> "解析异常"
                                else -> error.toString()
                            }
                    Snackbar.make(articleLayout, msg, Snackbar.LENGTH_LONG)
                            .setAction("重试") { loadData() }.show()
                })
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
}
