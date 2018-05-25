package com.lm.ll.spark.util

import org.jsoup.nodes.Document
import org.jsoup.Jsoup
import org.jsoup.select.Elements


/**
 * Created by ll on 2018-05-24 19:29.
 */
class Spider {
    fun scratch(webUrl:String){
//        val mList: MutableList<News> = ArrayList()
        val doc: Document = Jsoup.connect(webUrl).get()
        val titleLinks: Elements = doc.select ("div#d_list")
        println("news's count: " + titleLinks.size)
        for (e in titleLinks){
            val title = e.select("a").text()
            println("新闻标题：" + title)
            val uri = e.select("a").attr("href")
            println("新闻链接" +uri)
//            val news = News()
//            news.newsTitle = title
//            news.newsUrl = uri
//            mList.add(news)
        }
//        print(mList.size)
    }
}