package com.lm.ll.spark.util

import android.util.Log
import com.hankcs.hanlp.HanLP
import com.lm.ll.spark.db.Article
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode
import org.jsoup.select.Elements


/**
 * Created by ll on 2018-05-24 19:29.
 */
class Spider {

    private val pattern = "[0-9]+" //匹配数字


    /**
     * @desc 获取jsoup的Document实例
     * @author ll
     * @time 2018-06-11 19:40
     * @param url 网络地址
     */
    private fun getDocument(url: String): Document {
        return Jsoup.connect(url).userAgent(USER_AGENT).get()
    }


    /**
     * @desc 抓取文章列表
     * @author ll
     * @time 2018-05-29 18:35
     */
    fun scratchArticleList(webUrl: String): ArrayList<Article> {
        val mList = ArrayList<Article>()
        Log.d("加载列表", webUrl)
        val doc = getDocument(webUrl)
        val titleLinks: Elements = doc.select ("div#d_list")
        for (e: Element in titleLinks){
            val uls: Elements = e.getElementsByTag("ul")
            for (ul: Element in uls){
                parseArticleList(ul, mList)
            }
        }

        return mList
    }

    /**
     * @desc 解析抓取到的网页内容，生成标题列表
     * @author ll
     * @time 2018-05-28 10:01
     */
    private fun parseArticleList(ul: Element, list: ArrayList<Article>) {
        for(child in ul.childNodes()){
            if(child.childNodes() == null || child.childNodeSize() != 7){
                continue
            }
            val childNodes = child.childNodes()
            val article = Article()
            val link: Element = childNodes[0] as Element
            val uri = link.attr("href")
            article.url = "$BASE_URL$uri"
            article.title = HanLP.convertToSimplifiedChinese(link.text()) //标题也将繁体转为简体
            val authorStr = (childNodes[1] as TextNode).text()
            val author = authorStr.substringAfter('-').substringBefore('(') //作者名称
            val wordCount = Regex(pattern).findAll(authorStr).toList().flatMap(MatchResult::groupValues).firstOrNull() //字节数
            article.textLength = "${(wordCount!!.toLong()) / 2}字" //字数
            article.author = "作者:$author"
            article.date = (childNodes[2] as Element).text() //日期

            val readCount = Regex(pattern).findAll((childNodes[4] as Element).text()).toList().flatMap(MatchResult::groupValues).firstOrNull()
            article.readCount = "阅读${readCount}次"
            list.add(article)
        }
    }

    /**
     * @desc 解析文章正文
     * @author ll
     * @time 2018-05-29 18:44
     */
    private fun parseText(e: Element): String{
        return HanLP.convertToSimplifiedChinese(e.text())    //繁体转简体
    }

    /**
     * @desc 抓取文章正文
     * @author ll
     * @time 2018-05-29 18:46
     * @param article 待抓取正文的文章链接
     * @param commentList 存储正文中其他章节链接的列表
     * @return 包含正文的文章链接
     */
    fun scratchText(article: Article, commentList: ArrayList<Article>): Article {
        val doc = getDocument(article.url!!)
        val body: Elements = doc.getElementsByTag("pre") //TODO 图文混排
        article.text = parseText(body[0])

        //抓取文章正文中可能包含的其他章节链接
        val links: Elements = body[0].getElementsByTag("a")
        for (link in links){
            val comment = Article()
            comment.url = link.attr("href")
            comment.title = HanLP.convertToSimplifiedChinese(link.text())
            comment.author = ""
            commentList.add(comment)
        }

        return article
    }


    /**
     * @desc 抓取正文的评论
     * @author ll
     * @time 2018-06-04 15:06
     */
    fun scratchComments(article: Article): ArrayList<Article> {
        val commentList = ArrayList<Article>()
        val doc: Document = Jsoup.connect(article.url).get()
        val comments: Elements = doc.getElementsByTag("ul")
        parseComments(comments[0], commentList)
        return commentList
    }

    /**
     * @desc 解析文章首层评论
     * @author ll
     * @time 2018-06-04 16:34
     */
    private fun parseComments(ul: Element, list: ArrayList<Article>) {
        for(child in ul.childNodes()){
            val childNodes = child.childNodes()
            val article = Article()
            val link: Element = childNodes[0] as Element
            val uri = link.attr("href")
            article.url = "$BASE_URL$uri"
            article.title = HanLP.convertToSimplifiedChinese(link.text())
            val authorStr = (childNodes[1] as TextNode).text()
            val author = authorStr.substringAfter('-').substringBefore('(') //作者名称
            val wordCount = Regex(pattern).findAll(authorStr).toList().flatMap(MatchResult::groupValues).firstOrNull() //字节数
            article.textLength = "${(wordCount!!.toLong()) / 2}字" //字数
            article.author = "作者:$author"
            article.date = (childNodes[2] as Element).text() //日期

            list.add(article)
        }
    }

    /**
     * @desc 抓取精华区文章列表
     * @author ll
     * @time 2018-06-05 19:39
     */
    fun scratchEliteArticleList(webUrl: String): ArrayList<Article> {
        val mList = ArrayList<Article>()
//        Log.d("加载列表",webUrl)
        val doc = getDocument(webUrl)
        val children: Elements = doc.select ("ul#thread_list")
        for (e: Element in children){
            for (child in e.childNodes()){
                if(child.childNodeSize() ==0){
                    continue
                }
                val article = Article()
                val link: Element = child.childNodes()[0] as Element
                val uri = link.attr("href")
                article.url = "$BASE_URL$uri"
                article.title = HanLP.convertToSimplifiedChinese(link.text().trimStart('.'))
                article.author = ""

                mList.add(article)
            }
        }
        return mList
    }

    /**
     * @desc 抓取禁忌书屋经典书库文章列表
     * @author ll
     * @time 2018-06-11 17:11
     */
    fun scratchClassicEroticaArticleList(webUrl: String): ArrayList<Article> {
        val mList = ArrayList<Article>()
//        Log.d("加载列表",webUrl)
        val doc = getDocument(webUrl)
        val children: Elements = doc.getElementsByClass("dc_bar")

        val element = children[1].childNodes()[1]
        for (node in element.childNodes()) {
            val efficientNode = node.childNodes()[1]
            val link = efficientNode.childNodes()[1].childNodes()[0] as Element

            val article = Article()
            article.url = "${efficientNode.baseUri().substringBefore("md")}${link.attr("href")}"
            article.title = HanLP.convertToSimplifiedChinese(link.text())
            article.author = ""

            mList.add(article)
        }

        return mList
    }

    /**
     * @desc 抓取经典书库的文章正文
     * @author ll
     * @time 2018-06-11 19:53
     */
    fun scratchClassicEroticaArticleText(article: Article): Article {
        val doc = getDocument(article.url!!)
        val elements = doc.getElementsByTag("p")
        val stringBuilder = StringBuilder()
        for (e in elements) {
            if (e.childNodeSize() == 2) {
                stringBuilder.appendln((e.childNodes()[0] as TextNode).text())
            }
        }
        article.text = stringBuilder.toString()

        return article
    }
}