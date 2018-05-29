package com.lm.ll.spark

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.AdapterView
import android.widget.TextView
import com.lm.ll.spark.adapter.NewsAdapter
import com.lm.ll.spark.db.News
import com.lm.ll.spark.util.Spider
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async


/**
 * 作者：Created by ll on 2018-05-28 14:56.
 * 邮箱：wenhelinlu@gmail.com
 */
class NewsDisplayActivity: AppCompatActivity() {

    private var news: News? = null
    private var tv_text: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display_news)

        tv_text = findViewById(R.id.tv_text)
        tv_text!!.setMovementMethod(ScrollingMovementMethod.getInstance())

        news = getIntent().getParcelableExtra("news")

        loadText()



//        var webView = this.findViewById(R.id.web_view) as WebView
//
//        var client = WebViewClient()
//        webView.setWebViewClient(client)
//        webView.loadUrl(news!!.url)

    }

    /**
     * @desc 加载文章列表
     * @author ll
     * @time 2018-05-29 19:40
     */
    private fun loadText(){
        val deferred1 = async(CommonPool) {
            println("hello1")
            val spider = Spider()
            news = spider.scratchText(news!!)
        }

        async(UI) {
            println("hello2")
            deferred1.await()
            tv_text!!.text = news!!.text
        }
    }

}

