package com.lm.ll.spark.util

import android.util.Log
import com.hankcs.hanlp.HanLP
import com.lm.ll.spark.db.Article
import com.lm.ll.spark.db.Comment
import io.reactivex.exceptions.Exceptions
import io.realm.RealmList
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode
import org.jsoup.select.Elements


/**
 * Created by ll on 2018-05-24 19:29.
 */
class Spider {

    companion object {
        private const val pattern = "[0-9]+" //匹配数字正则表达式模式串
        private const val paragraphFlagPattern = "\\r\\n\\s*?\\r\\n" //匹配段落标记符的正则表达式模式串，可匹配\r\n\r\n,\r\n \r\n, \r\n   \r\n等两个\r\n之间包含0到多个空格的情况
        private const val paragraphFlag = "\r\n\r\n" //段落标记符
        private const val newlineFlagPattern = "\\s*?\\r\\n\\s*?" //匹配换行标记符的正则表达式的模式串，可匹配\r\n, \r\n ,\r\n 等\r\n两边有0到多个空格的情况
        private const val replacerWord = "REPLACER_FLAG" //用于字符串替换的标记

        //region 使用Jsoup直接解析网页
        /**
         * @desc 获取jsoup的Document实例
         * @author ll
         * @time 2018-06-11 19:40
         * @param url 网络地址
         */
        private fun getDocument(url: String): Document {
            try {
                return Jsoup.connect(url).userAgent(USER_AGENT).timeout(TIME_OUT).get()
            } catch (t: Throwable) {
                throw Exceptions.propagate(t)
            }

        }


        /**
         * @desc 抓取文章列表
         * @author ll
         * @time 2018-05-29 18:35
         */
        fun scratchArticleList(webUrl: String): ArrayList<Article> {
            try {
                val mList = ArrayList<Article>()
                Log.d("加载列表", webUrl)
                val doc = getDocument(webUrl)
                val titleLinks: Elements = doc.select("div#d_list")
                for (e: Element in titleLinks) {
                    val uls: Elements = e.getElementsByTag("ul")
                    for (ul: Element in uls) {
                        parseArticleList(ul, mList)
                    }
                }
                return mList
            } catch (t: Throwable) {
                throw Exceptions.propagate(t)
            }

        }

        /**
         * @desc 解析抓取到的网页内容，生成标题列表
         * @author ll
         * @time 2018-05-28 10:01
         */
        private fun parseArticleList(ul: Element, list: ArrayList<Article>) {
            try {
                for (child in ul.childNodes()) {
                    if (child.childNodes() == null || child.childNodeSize() != 7) {
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
            } catch (t: Throwable) {
                throw Exceptions.propagate(t)
            }
        }

        /**
         * @desc 解析文章正文
         * @author ll
         * @time 2018-05-29 18:44
         */
        private fun parseText(e: Element): String {
            return HanLP.convertToSimplifiedChinese(e.text())    //繁体转简体
        }

        /**
         * @desc 抓取文章正文
         * @author ll
         * @time 2018-05-29 18:46
         * @param article 待抓取正文的文章链接
         * @return 包含正文的文章链接
         */
        fun scratchText(article: Article): Article {
            try {
                val doc = getDocument(article.url!!)
                val body: Elements = doc.getElementsByTag("pre") //TODO 图文混排

                /**
                 *
                 * 去除\r\n，保留\r\n\r\n，保留段落格式，去除段落内不需要的换行显示
                 *
                 * \s* 表示若干个空格（可以是0个），\s+ 表示一个或多个空格
                 *
                 * 因为不同的文章可能段落符号不一致，两个\r\n之间可能有0到多个空格，影响下一步的替换处理。所以先将\r\n和\r\n之间的空格去掉再匹配，统一将段落转换成\r\n\r\n形式
                 */

                val originalText = parseText(body[0])
                val containsParagraphFlag = Regex(paragraphFlagPattern).containsMatchIn(originalText) //是否包含段落标记（\r\n\r\n）
                //如果包含段落标记，则按照规则清除换行标记，保留段落标记
                if (containsParagraphFlag) {
                    val text = Regex(paragraphFlagPattern).replace(parseText(body[0]), replacerWord)
                    //原字符串中用于换行的\r\n两侧可能会有空格，如果不处理会导致将\r\n替换成空字符后，原有位置的空格仍然存在，所以使用正则将\r\n及两侧可能有的空格都替换成空字符
                    article.text = Regex(newlineFlagPattern).replace(text, "").replace(replacerWord, paragraphFlag, false)
                } else {
                    article.text = originalText //如果文章不包含段落标记（如琼明神女录第33章），则不处理
                }


                val commentList = RealmList<Comment>()
                //抓取文章正文中可能包含的其他章节链接（比如精华区中的正文）
                val links: Elements = body[0].getElementsByTag("a")
                for (link in links) {
                    val comment = Comment()
                    comment.url = link.attr("href")
                    comment.title = HanLP.convertToSimplifiedChinese(link.text())
                    comment.author = ""

                    commentList.add(comment)
                }
                //因为在精华区中，章节链接是倒序显示，所以将其翻转
                commentList.reverse()

                //抓取对正文的评论列表
                commentList.addAll(scratchComments(article))
                article.comments = commentList

                return article
            } catch (t: Throwable) {
                throw Exceptions.propagate(t)
            }
        }


        /**
         * @desc 抓取正文的评论
         * @author ll
         * @time 2018-06-04 15:06
         */
        private fun scratchComments(article: Article): RealmList<Comment> {
            try {
                val doc: Document = Jsoup.connect(article.url).get()
                val comments: Elements = doc.getElementsByTag("ul")
                return parseComments(comments[0])
            } catch (t: Throwable) {
                throw Exceptions.propagate(t)
            }
        }

        /**
         * @desc 解析文章首层评论
         * @author ll
         * @time 2018-06-04 16:34
         */
        private fun parseComments(ul: Element): RealmList<Comment> {
            try {
                val list = RealmList<Comment>()
                for (child in ul.childNodes()) {
                    val childNodes = child.childNodes()
                    val comment = Comment()
                    val link: Element = childNodes[0] as Element
                    val uri = link.attr("href")
                    comment.url = "$BASE_URL$uri"
                    comment.title = HanLP.convertToSimplifiedChinese(link.text())
                    val authorStr = (childNodes[1] as TextNode).text()
                    val author = authorStr.substringAfter('-').substringBefore('(') //作者名称
                    val wordCount = Regex(pattern).findAll(authorStr).toList().flatMap(MatchResult::groupValues).firstOrNull() //字节数
                    comment.textLength = "${(wordCount!!.toLong()) / 2}字" //字数
                    comment.author = "作者:$author"
                    comment.date = (childNodes[2] as Element).text() //日期
                    comment.readCount = ""
                    comment.text = ""

                    list.add(comment)
                }
                return list
            } catch (t: Throwable) {
                throw Exceptions.propagate(t)
            }
        }

        /**
         * @desc 抓取精华区文章列表
         * @author ll
         * @time 2018-06-05 19:39
         */
        fun scratchEliteArticleList(webUrl: String): ArrayList<Article> {
            try {
                val mList = ArrayList<Article>()
                val doc = getDocument(webUrl)
                val children: Elements = doc.select("ul#thread_list")
                for (e: Element in children) {
                    for (child in e.childNodes()) {
                        if (child.childNodeSize() == 0) {
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
            } catch (t: Throwable) {
                throw Exceptions.propagate(t)
            }
        }

        /**
         * @desc 抓取禁忌书屋经典书库文章列表
         * @author ll
         * @time 2018-06-11 17:11
         */
        fun scratchClassicEroticaArticleList(webUrl: String): ArrayList<Article> {
            try {
                val mList = ArrayList<Article>()
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
            } catch (t: Throwable) {
                throw Exceptions.propagate(t)
            }
        }

        /**
         * @desc 抓取经典书库的文章正文
         * @author ll
         * @time 2018-06-11 19:53
         */
        fun scratchClassicEroticaArticleText(article: Article): Article {
            try {
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
            } catch (t: Throwable) {
                throw Exceptions.propagate(t)
            }
        }
        //endregion


        //region 使用Retrofit2 + RxJava + Jsoup
        /**
         * @desc 抓取文章列表
         * @author ll
         * @time 2018-05-29 18:35
         * @param doc Jsoup的Document文档（由Retrofit2获取网页内容，然后加载成Jsoup的Document文档）
         */
        fun scratchArticleList(doc: Document): ArrayList<Article> {
            try {
                val mList = ArrayList<Article>()
                val titleLinks: Elements = doc.select("div#d_list")
                for (e: Element in titleLinks) {
                    val uls: Elements = e.getElementsByTag("ul")
                    for (ul: Element in uls) {
                        parseArticleList(ul, mList)
                    }
                }

                return mList
            } catch (t: Throwable) {
                throw Exceptions.propagate(t)
            }
        }

        /**
         * @desc 抓取文章正文
         * @author ll
         * @time 2018-05-29 18:46
         * @param article 待抓取正文的文章链接
         * @return 包含正文的文章链接
         */
        fun scratchText(doc: Document, article: Article): Article {
            try {
                val body: Elements = doc.getElementsByTag("pre") //TODO 图文混排

                /**
                 *
                 * 去除\r\n，保留\r\n\r\n，保留段落格式，去除段落内不需要的换行显示
                 * \s* 表示若干个空格（可以是0个），\s+ 表示一个或多个空格
                 * 因为不同的文章可能段落符号不一致，两个\r\n之间可能有0到多个空格，影响下一步的替换处理。所以先将\r\n和\r\n之间的空格去掉再匹配，统一将段落转换成\r\n\r\n形式
                 */

                val originalText = parseText(body[0])
                val containsParagraphFlag = Regex(paragraphFlagPattern).containsMatchIn(originalText) //是否包含段落标记（\r\n\r\n）
                //如果包含段落标记，则按照规则清除换行标记，保留段落标记
                if (containsParagraphFlag) {
                    val text = Regex(paragraphFlagPattern).replace(parseText(body[0]), replacerWord)
                    //原字符串中用于换行的\r\n两侧可能会有空格，如果不处理会导致将\r\n替换成空字符后，原有位置的空格仍然存在，所以使用正则将\r\n及两侧可能有的空格都替换成空字符
                    article.text = Regex(newlineFlagPattern).replace(text, "").replace(replacerWord, paragraphFlag, false)
                } else {
                    article.text = originalText //如果文章不包含段落标记（如琼明神女录第33章），则不处理
                }


                val commentList = RealmList<Comment>()
                //抓取文章正文中可能包含的其他章节链接（比如精华区中的正文）
                val links: Elements = body[0].getElementsByTag("a")
                for (link in links) {
                    val comment = Comment()
                    comment.url = link.attr("href")
                    comment.title = HanLP.convertToSimplifiedChinese(link.text())
                    comment.author = ""

                    commentList.add(comment)
                }
                //因为在精华区中，章节链接是倒序显示，所以将其翻转
                commentList.reverse()

                //抓取对正文的评论列表
                commentList.addAll(scratchComments(doc))
                article.comments = commentList

                return article
            } catch (t: Throwable) {
                throw Exceptions.propagate(t)
            }
        }

        /**
         * @desc 抓取经典书库的文章正文
         * @author ll
         * @time 2018-06-11 19:53
         */
        fun scratchClassicEroticaArticleText(doc: Document, article: Article): Article {
            try {
                val elements = doc.getElementsByTag("p")
                val stringBuilder = StringBuilder()
                for (e in elements) {
                    if (e.childNodeSize() == 2) {
                        stringBuilder.appendln((e.childNodes()[0] as TextNode).text())
                    }
                }
                article.text = stringBuilder.toString()
                return article
            } catch (t: Throwable) {
                throw Exceptions.propagate(t)
            }
        }

        /**
         * @desc 抓取正文的评论
         * @author ll
         * @time 2018-06-04 15:06
         */
        private fun scratchComments(doc: Document): RealmList<Comment> {
            try {
                val comments: Elements = doc.getElementsByTag("ul")
                return parseComments(comments[0])
            } catch (t: Throwable) {
                throw Exceptions.propagate(t)
            }
        }


        //endregion
    }
}

//TODO：使用Retrofit + Jsoup抓取分析

//TODO： 选中要折叠的代码后，按Ctrl + Alt + T 实现类似VS中Region的功能
