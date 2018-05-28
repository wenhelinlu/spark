package com.lm.ll.spark

import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity
import android.webkit.WebView


/**
 * 作者：Created by ll on 2018-05-28 14:56.
 * 邮箱：wenhelinlu@gmail.com
 */
public class NewsDisplayActivity: AppCompatActivity() {

    private var newsUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        setContentView(R.layout.activity_display_news)
        newsUrl = getIntent().getStringExtra("news_url")
        var webView = findViewById(R.id.web_view) as WebView
        webView.getSettings().setJavaScriptEnabled(true)
        webView.loadUrl(newsUrl)

    }
}

