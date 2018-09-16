package com.lm.ll.spark.activity

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.lm.ll.spark.R
import com.lm.ll.spark.api.TabooBooksApiService
import com.lm.ll.spark.application.InitApplication
import com.lm.ll.spark.db.Article
import com.lm.ll.spark.net.Spider
import com.lm.ll.spark.repository.TabooArticlesRepository
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider
import com.uber.autodispose.kotlin.autoDisposable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_rich_text.*
import org.jsoup.Jsoup
import retrofit2.HttpException
import java.net.ConnectException
import java.util.concurrent.TimeoutException
import java.util.regex.Pattern
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
                .autoDisposable(scopeProvider) //使用AutoDispose解除RxJava2订阅
                .subscribe({ result ->
                    val doc = Jsoup.parse(result)
                    val list = Spider.scratchRichTextData(doc)
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
                    val msg =
                            when (error) {
                                is HttpException, is SSLHandshakeException, is ConnectException -> "网络连接异常"
                                is TimeoutException -> "网络连接超时"
                                is IndexOutOfBoundsException, is ClassCastException -> "解析异常"
                                else -> error.toString()
                            }

                    Snackbar.make(richContentLayout, msg, Snackbar.LENGTH_LONG)
                            .setAction("重试") { loadData() }.show()
                })
    }

    /**
     * 获取img标签中的src值
     * @param content
     * @return
     */
    private fun getImgSrc(content: String): String? {
        var imgSrc: String? = null
        //目前img标签标示有3种表达式
        //<img alt="" src="1.jpg"/>   <img alt="" src="1.jpg"></img>     <img alt="" src="1.jpg">
        //开始匹配content中的<img />标签
        val pImg = Pattern.compile("<(img|IMG)(.*?)(/>|></img>|>)")
        val mImg = pImg.matcher(content)
        var resultImg = mImg.find()
        if (resultImg) {
            while (resultImg) {
                //获取到匹配的<img />标签中的内容
                val strImg = mImg.group(2)

                //开始匹配<img />标签中的src
                val pSrc = Pattern.compile("(src|SRC)=(\"|\')(.*?)(\"|\')")
                val mSrc = pSrc.matcher(strImg)
                if (mSrc.find()) {
                    imgSrc = mSrc.group(3)
                }
                //结束匹配<img />标签中的src

                //匹配content中是否存在下一个<img />标签，有则继续以上步骤匹配<img />标签中的src
                resultImg = mImg.find()
            }
        }
        return imgSrc
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
