package com.lm.ll.spark

import org.jsoup.nodes.Document
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.select.Elements


/**
 * Created by ll on 2018-05-24 19:29.
 */
class Spider() {
    fun scratch(webUrl:String){
        val mList: MutableList<News> = MutableList()
        val doc: Document = Jsoup.connect(webUrl).get()
        val titleLinks: Elements = doc.select ("div.d_list")
        for (e in titleLinks){
            val title = e.select("a").text()
            val uri = e.select("a").attr("href")
            val news = News()
            news.newsTitle = title
            news.newsUrl = uri
            mList.add(news)
        }
        val count: Int = mList.size
    }
}