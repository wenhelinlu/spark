package com.lm.ll.spark.util

import com.lm.ll.spark.db.News
import kotlinx.coroutines.experimental.NonCancellable.children
import org.jsoup.nodes.Document
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode
import org.jsoup.select.Elements


/**
 * Created by ll on 2018-05-24 19:29.
 */
class Spider {
    fun scratch(webUrl:String){
        val mList = ArrayList<News>()
        val doc: Document = Jsoup.connect(webUrl).get()
        val titleLinks: Elements = doc.select ("div#d_list")
        println("news's count: " + titleLinks.size)
        for (e: Element in titleLinks){
            val uls: Elements = e.getElementsByTag("ul")
            for (ul: Element in uls){
                for (child: Element in ul.children()){
                    for (node in child.childNodes()){
                        parseContent(node.childNodes(),mList)
                    }
                }
            }

//                val title = link.text()
//                println("news's title：" + title)
//                val uri = link.attr("href")
//                println("news's uri: " +uri)
//                val news = News()
//                news.id = index++
//                news.newsTitle = title
//                news.newsUrl = uri
//                mList.add(news)
//            }
        }
        print(mList.size)
    }


    fun parseContent(nodes: List<Node>, list: ArrayList<News>){
        val news = News()
        val link: Element = nodes[0] as Element
        news.url = link.attr("href")
        news.title = link.text()
        news.author = (nodes[1] as TextNode).text()
        news.date = (nodes[2] as Element).text()
        news.readCount = (nodes[4] as Element).text()

        if((nodes[5] as Element).childNodes().count() > 0){
            parseContent((nodes[5] as Element).childNodes(), list)
        }
        list.add(news)
    }
}