package com.lm.ll.spark.util

import com.lm.ll.spark.db.News
import org.jsoup.nodes.Document
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode
import org.jsoup.select.Elements


/**
 * Created by ll on 2018-05-24 19:29.
 */
class Spider {

    /**
     * @desc 抓取文章列表
     * @author ll
     * @time 2018-05-29 18:35
     */
    fun scratchContent(webUrl:String): ArrayList<News>{
        val mList = ArrayList<News>()
        val doc: Document = Jsoup.connect(webUrl).get()
        val titleLinks: Elements = doc.select ("div#d_list")
        println("news's count: " + titleLinks.size)
        for (e: Element in titleLinks){
            val uls: Elements = e.getElementsByTag("ul")
            for (ul: Element in uls){
                parseContent(ul,mList)
            }
        }

        print(mList.size)
        return mList
    }

    /**
     * @desc 解析抓取到的正文内容，生成标题链接
     * @author ll
     * @time 2018-05-28 10:01
     */
    private fun parseContent(ul: Element, list: ArrayList<News>){
        for(child in ul.childNodes()){
            if(child.childNodes() == null || child.childNodeSize() != 7){
                continue
            }
            val childNodes = child.childNodes()
            val news = News()
            val link: Element = childNodes[0] as Element
            var baseUri = child.baseUri()
            var uri = link.attr("href").removePrefix("index.php")
            news.url = "${baseUri}${uri}"
            news.title = link.text()
            news.author = (childNodes[1] as TextNode).text()
            news.date = (childNodes[2] as Element).text()
            news.readCount = (childNodes[4] as Element).text()
            list.add(news)
        }
    }

    /**
     * @desc 解析文章正文
     * @author ll
     * @time 2018-05-29 18:44
     */
    private fun parseText(e: Element): String{
        return e.text()
    }

    /**
     * @desc 抓取文章正文
     * @author ll
     * @time 2018-05-29 18:46
     */
    fun scratchText(news: News): News{
        val doc: Document = Jsoup.connect(news.url).get()
        val body: Elements = doc.getElementsByTag("pre")
        val comments: Elements = doc.getElementsByTag("ul")
        news.text = parseText(body[0])

        return news
    }
}