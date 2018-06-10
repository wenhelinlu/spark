package com.lm.ll.spark.util

import android.util.Log
import com.lm.ll.spark.db.News
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode
import org.jsoup.select.Elements
import java.util.*


/**
 * Created by ll on 2018-05-24 19:29.
 */
class Spider {

    private val pattern = "[0-9]+" //匹配数字

    /**
     * @desc 抓取文章列表
     * @author ll
     * @time 2018-05-29 18:35
     */
    fun scratchContent(webUrl:String): ArrayList<News>{
        val mList = ArrayList<News>()
        Log.d("加载列表", webUrl)
        val doc: Document = Jsoup.connect(webUrl).get()
        val titleLinks: Elements = doc.select ("div#d_list")
        for (e: Element in titleLinks){
            val uls: Elements = e.getElementsByTag("ul")
            for (ul: Element in uls){
                parseContent(ul,mList)
            }
        }

        return mList
    }

    /**
     * @desc 解析抓取到的正文内容，生成标题列表
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
            val uri = link.attr("href")
            news.url = "$BASE_URL$uri"
            news.title = link.text()
            val authorStr = (childNodes[1] as TextNode).text()
            val author = authorStr.substringAfter('-').substringBefore('(') //作者名称
            val wordCount = Regex(pattern).findAll(authorStr).toList().flatMap(MatchResult::groupValues).firstOrNull() //字节数
            news.textLength = "${(wordCount!!.toLong())/2}字" //字数
            news.author = "作者:$author"
            news.date = (childNodes[2] as Element).text() //日期

            val readCount = Regex(pattern).findAll((childNodes[4] as Element).text()).toList().flatMap(MatchResult::groupValues).firstOrNull()
            news.readCount = "阅读${readCount}次"
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
     * @param news 待抓取正文的文章链接
     * @param commentList 存储正文中其他章节链接的列表
     * @return 包含正文的文章链接
     */
    fun scratchText(news: News, commentList: ArrayList<News>): News{
        val doc: Document = Jsoup.connect(news.url).get()
        val body: Elements = doc.getElementsByTag("pre")
        news.text = parseText(body[0])   //TODO 繁体转简体

        //抓取文章正文中可能包含的其他章节链接
        val links: Elements = body[0].getElementsByTag("a")
        for (link in links){
            val comment = News()
            comment.url = link.attr("href")
            comment.title = link.text()
            comment.author = ""
            commentList.add(comment)
        }

        return news
    }


    /**
     * @desc 抓取正文的评论
     * @author ll
     * @time 2018-06-04 15:06
     */
    fun scratchComments(news:News):ArrayList<News>{
        val commentList = ArrayList<News>()
        val doc: Document = Jsoup.connect(news.url).get()
        val comments: Elements = doc.getElementsByTag("ul")
        parseComments(comments[0], commentList)
        return commentList
    }

    /**
     * @desc 解析文章首层评论
     * @author ll
     * @time 2018-06-04 16:34
     */
    private fun parseComments(ul: Element, list: ArrayList<News>){
        for(child in ul.childNodes()){
            val childNodes = child.childNodes()
            val news = News()
            val link: Element = childNodes[0] as Element
            val uri = link.attr("href")
            news.url = "$BASE_URL$uri"
            news.title = link.text()
            val authorStr = (childNodes[1] as TextNode).text()
            val author = authorStr.substringAfter('-').substringBefore('(') //作者名称
            val wordCount = Regex(pattern).findAll(authorStr).toList().flatMap(MatchResult::groupValues).firstOrNull() //字节数
            news.textLength = "${(wordCount!!.toLong())/2}字" //字数
            news.author = "作者:$author"
            news.date = (childNodes[2] as Element).text() //日期

            list.add(news)
        }
    }

    /**
     * @desc 抓取精华区文章列表
     * @author ll
     * @time 2018-06-05 19:39
     */
    fun scratchEliteNewsList(webUrl:String): ArrayList<News>{
        val mList = ArrayList<News>()
//        Log.d("加载列表",webUrl)
        val doc: Document = Jsoup.connect(webUrl).get()
        val children: Elements = doc.select ("ul#thread_list")
        for (e: Element in children){
            for (child in e.childNodes()){
                if(child.childNodeSize() ==0){
                    continue
                }
                val news = News()
                val link: Element = child.childNodes()[0] as Element
                val uri = link.attr("href")
                news.url = "$BASE_URL$uri"
                news.title = link.text()
                news.author = ""

                mList.add(news)
            }
        }
        return mList
    }
}