package com.lm.ll.spark.net

import com.lm.ll.spark.db.Article
import com.lm.ll.spark.db.Comment
import com.lm.ll.spark.db.ProfileInfo
import com.lm.ll.spark.db.SiteMap
import com.lm.ll.spark.util.GlobalConst.Companion.PARAGRAPH_FLAG_COUNT_LIMIT
import com.lm.ll.spark.util.GlobalConst.Companion.TEXT_IMAGE_SPLITER
import com.lm.ll.spark.util.GlobalConst.Companion.TIME_OUT
import com.lm.ll.spark.util.GlobalConst.Companion.USER_AGENT
import com.lm.ll.spark.util.ObjectBox.getSiteMapBox
import com.lm.ll.spark.util.convertToSimplifiedChinese
import io.reactivex.exceptions.Exceptions
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
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
        private const val emptyLineFlagPattern = " (\\s*)\\n" //匹配空行标记符的正则表达式的模式串
        private const val replacerWord = "REPLACER_FLAG" //用于字符串替换的标记

        private var depth = 0

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
                val baseUri = ul.baseUri().substringBefore("index")
                for (child in ul.childNodes()) {
                    if (child.childNodes() == null || child.childNodeSize() == 0) {
                        continue
                    }
                    when {
                        child.childNodeSize() == 7 -> {
                            val childNodes = child.childNodes()
                            val article = Article()
                            val link: Element = childNodes[0] as Element
                            val uri = link.attr("href")
                            article.url = "$baseUri$uri"
                            article.title = link.text().convertToSimplifiedChinese() //标题也将繁体转为简体
                            val authorStr = (childNodes[1] as TextNode).text()
                            val author = authorStr.substringAfter('-').substringBefore('(').trim() //作者名称
                            val wordCount = Regex(pattern).findAll(authorStr).toList().flatMap(MatchResult::groupValues).lastOrNull() //字节数
                            article.textLength = "${(wordCount!!.toLong()) / 2}字" //字数
                            article.author = "作者:$author"
                            article.date = (childNodes[2] as Element).text() //日期

                            val readCount = Regex(pattern).findAll((childNodes[4] as Element).text()).toList().flatMap(MatchResult::groupValues).firstOrNull()
                            article.readCount = "阅读${readCount}次"
                            list.add(article)
                        }
                        child.childNodeSize() == 9 -> {
                            val childNodes = child.childNodes()
                            val article = Article()
                            val link: Element = childNodes[0] as Element
                            val uri = link.attr("href")
                            article.url = "$baseUri$uri"
                            article.title = link.text().convertToSimplifiedChinese() //标题也将繁体转为简体
                            val author = (childNodes[2] as Element).text() //作者名称
                            val wordCount = Regex(pattern).findAll((childNodes[3] as TextNode).text()).toList().flatMap(MatchResult::groupValues).lastOrNull() //字节数
                            article.textLength = "${(wordCount!!.toLong()) / 2}字" //字数
                            article.author = "作者:$author"
                            article.date = (childNodes[4] as Element).text() //日期
                            val readCount = Regex(pattern).findAll((childNodes[6] as Element).text()).toList().flatMap(MatchResult::groupValues).firstOrNull()
                            article.readCount = "阅读${readCount}次"
                            list.add(article)
                        }
                        else -> {
                            val childNodes = child.childNodes()
                            val article = Article()
                            val link: Element = childNodes[0] as Element
                            val uri = link.attr("href")
                            article.url = "$baseUri$uri"
                            article.title = link.text().convertToSimplifiedChinese() //标题也将繁体转为简体
                            list.add(article)
                        }
                    }

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
            return formatText(e.text().convertToSimplifiedChinese())
        }

        /**
         * @desc 抓取正文的评论
         * @author ll
         * @time 2018-06-04 15:06
         */
        private fun scratchComments(article: Article): ArrayList<Comment> {
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
        private fun parseComments(ul: Element, baseUri:String = ""): ArrayList<Comment> {
            try {
                val list = ArrayList<Comment>()
                for (child in ul.childNodes()) {
                    depth = 0  //每个评论初始层级都置为0

                    val subList = ArrayList<Comment>()
                    parseCommentsRecursive(child, subList,baseUri)
                    subList.reverse()
                    list.addAll(subList)
                }
                return list
            } catch (t: Throwable) {
                throw Exceptions.propagate(t)
            }
        }

        /**
         * @desc 递归解析文章评论
         * @author lm
         * @time 2018-07-21 22:20
         */
        private fun parseCommentsRecursive(ul: Node, list: ArrayList<Comment>, baseUri: String) {
            if (ul.childNodes()[3].childNodes().count() > 0) {
                for (sub in ul.childNodes()[3].childNodes()) {
                    depth++   //子评论层级加1
                    parseCommentsRecursive(sub, list,baseUri)
                    depth--  //从子评论返回上一级评论时，层级减1
                }
            }

            val childNodes = ul.childNodes()
            val comment = Comment()
            val link: Element = childNodes[0] as Element
            val uri = link.attr("href")
            comment.url = "$baseUri$uri"
            comment.title = link.text().convertToSimplifiedChinese()
            val authorStr = (childNodes[1] as TextNode).text()
            val author = authorStr.substringAfter('-').substringBefore('(').trim() //作者名称

            //某些author名称中带有数字，所以使用Regex筛选出的数字有多组，取最后一组数字为字节数（例如authorStr：  - frost1224 (0 bytes)，字节数为0，而不是前面的1224）
            val wordCount = Regex(pattern).findAll(authorStr).toList().flatMap(MatchResult::groupValues).lastOrNull()
//            Log.d(LOG_TAG_COMMON,"wordCount = $wordCount , authorStr = $authorStr")
            comment.textLength = "${(wordCount!!.toLong()) / 2}字" //字数
            comment.author = "作者:$author"
            comment.date = (childNodes[2] as Element).text() //日期
            comment.depth = depth

//            Log.d(LOG_TAG_COMMON,"depth = $depth , ${getPlaceholder(depth)}${comment.title}")

            list.add(comment)
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
                        article.url = "${child.baseUri().substringBefore("index")}$uri"
                        article.title = link.text().trimStart('.').convertToSimplifiedChinese()

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
                val links = (element as Element).getElementsByTag("a")
                for (link in links) {
                    val article = Article()
                    article.url = "${link.baseUri().substringBefore("md")}${link.attr("href")}"
                    article.title = link.text().convertToSimplifiedChinese()
                    article.classicalFlag = 1

                    mList.add(article)
                }
                return mList
            } catch (t: Throwable) {
                throw Exceptions.propagate(t)
            }
        }


        /**
         * @desc 抓取并解析根据关键词查询到的文章列表
         * @author ll
         * @time 2018-08-13 20:16
         */
        fun scratchQueryArticles(webUrl: String): ArrayList<Article> {
            try {
                val doc = getDocument(webUrl)
                val articles: Elements = doc.getElementsByClass("t_l")
                val list = ArrayList<Article>()
                for (e in articles) {
                    val article = Article()
                    val link = e.getElementsByTag("a").first()
                    article.title = link.text().convertToSimplifiedChinese()
                    article.url = "${e.baseUri().substringBefore("index")}${link.attr("href")}"
                    article.author = e.getElementsByClass("t_author").first().text()
                    article.date = e.getElementsByTag("i").first().text()

                    list.add(article)
                }
                return list
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
                val originalText = body[0].outerHtml()

                //如果网页文本中包含图片信息，则使用抓取图文混排的方法，否则直接获取文本
                article.text = if (originalText.contains("<img") && originalText.contains("src=")) {
                    scratchRichTextData(doc)
                } else {
                    parseText(body[0])
                }

                val commentList = ArrayList<Comment>()
                //抓取文章正文中可能包含的其他章节链接（比如精华区中的正文）
                val links: Elements = body[0].getElementsByTag("a")
                for (link in links) {
                    val comment = Comment()
                    comment.url = link.attr("href")
                    comment.title = link.text().convertToSimplifiedChinese()

                    commentList.add(comment)
                }
                //因为在精华区中，章节链接是倒序显示，所以将其翻转
                commentList.reverse()

                //抓取对正文的评论列表
                commentList.addAll(scratchComments(doc, article.url!!.substringBefore("index")))
                article.comments.addAll(commentList)

                return article
            } catch (t: Throwable) {
                throw Exceptions.propagate(t)
            }
        }

        /**
         * @desc 格式化文本
         * @author ll
         * @time 2018-09-20 16:16
         */
        private fun formatText(originalText: String): String {
            /**
             *
             * 去除\r\n，保留\r\n\r\n，保留段落格式，去除段落内不需要的换行显示
             * \s* 表示若干个空格（可以是0个），\s+ 表示一个或多个空格
             * 因为不同的文章可能段落符号不一致，两个\r\n之间可能有0到多个空格，影响下一步的替换处理。所以先将\r\n和\r\n之间的空格去掉再匹配，统一将段落转换成\r\n\r\n形式
             */
            //去除文本中的6park.com
            val puredText = originalText.replace("6park.com", "", true)
            //先去除空行标记（某些文章（如【只贴精品-马艳丽1-4）会因为空行标记导致误判断为含段落标记，从而清除换行标记后，排版混乱）
            val removedEmptyLineText = Regex(emptyLineFlagPattern).replace(puredText, "")
            //判断文本中段落标记（\r\n\r\n）个数，大于某个值，则处理，否则不处理
            val pCount = Regex(paragraphFlagPattern).findAll(removedEmptyLineText).count()
//                Log.d(LOG_TAG_COMMON,"段落标记数量 = $pCount")
            //判断是否需要按照规则清除换行标记，保留段落标记
            return if (pCount > PARAGRAPH_FLAG_COUNT_LIMIT) {
                val text = Regex(paragraphFlagPattern).replace(removedEmptyLineText, replacerWord)
                //原字符串中用于换行的\r\n两侧可能会有空格，如果不处理会导致将\r\n替换成空字符后，原有位置的空格仍然存在，所以使用正则将\r\n及两侧可能有的空格都替换成空字符
                Regex(newlineFlagPattern).replace(text, "").replace(replacerWord, paragraphFlag, false)
            } else {
                puredText //如果文章不包含段落标记（如琼明神女录第33章），则不处理
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
                    if (e.childNodeSize() == 2 && e.childNodes()[0] is TextNode) {
                        stringBuilder.appendln((e.childNodes()[0] as TextNode).text())
                    }
                }
                article.text = stringBuilder.toString().convertToSimplifiedChinese()
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
        private fun scratchComments(doc: Document, baseUri: String): ArrayList<Comment> {
            try {
                val comments: Elements = doc.getElementsByTag("ul")
                return parseComments(comments[0],baseUri)
            } catch (t: Throwable) {
                throw Exceptions.propagate(t)
            }
        }


        /**
         * @desc 抓取并解析根据关键词查询到的文章列表
         * @author ll
         * @time 2018-08-13 20:16
         */
        fun scratchQueryArticles(doc: Document): ArrayList<Article> {
            try {
                val articles: Elements = doc.getElementsByClass("t_l")
                val list = ArrayList<Article>()
                for (e in articles) {
                    val article = Article()
                    val link = e.getElementsByTag("a").first()
                    article.title = link.text().convertToSimplifiedChinese()
                    article.url = "${e.baseUri().substringBefore("index")}${link.attr("href")}"
                    article.author = e.getElementsByClass("t_author").first().text()
                    article.date = e.getElementsByTag("i").first().text()

                    list.add(article)
                }
                return list
            } catch (t: Throwable) {
                throw Exceptions.propagate(t)
            }
        }


        /**
         * @desc 抓取个人信息
         * @author LL
         * @time 2018-08-27 15:18
         */
        fun scratchProfileInfo(doc: Document): ArrayList<ProfileInfo> {
            try {
                val list = ArrayList<ProfileInfo>()
                val masthead = doc.select("div.content_list")
                val docTrs = masthead[0].selectFirst("tbody").select("tr")
                for (e in docTrs) {
                    val info = ProfileInfo((e.childNode(1) as Element).text(), (e.childNode(2) as Element).text())
                    list.add(info)
                }

                val accountTrs = masthead[3].selectFirst("tbody").select("tr")
                for (e in accountTrs) {
                    if (e.childNodeSize() != 4) {
                        continue
                    }
                    val info = ProfileInfo((e.childNode(1) as Element).text(), (e.childNode(2) as Element).text())
                    list.add(info)
                }
                return list
            } catch (t: Throwable) {
                throw  Exceptions.propagate(t)
            }
        }

        /**
         * @desc 抓取图文混排文章
         * @author lm
         * @time 2018-09-22 9:58
         */
        private fun scratchRichTextData(doc: Document): String {
            try {
                val list = ArrayList<String>()
                val body: Elements = doc.getElementsByTag("pre")
                for (node in body[0].childNodes()) {
                    scratchRichTextDataRecursively(node, list)
                }

                val composedText = StringBuilder()
                list.forEach {

                    //相邻的纯文本（即不包含图片链接）合并在一起（即占用一个RecyclerView的item显示）
                    if (it.contains("<img")) {
                        composedText.append("$TEXT_IMAGE_SPLITER$it$TEXT_IMAGE_SPLITER")
                    } else {
                        composedText.appendln(it)
                    }
                }
                return composedText.toString()
            } catch (t: Throwable) {
                throw Exceptions.propagate(t)
            }
        }

        /**
         * @desc 递归解析网页图文混排文章结构
         * @author lm
         * @time 2018-09-16 22:25
         */
        private fun scratchRichTextDataRecursively(e: Node, list: ArrayList<String>) {
            if (e.childNodes() != null && e.childNodeSize() > 0) {
                for (node in e.childNodes()) {
                    scratchRichTextDataRecursively(node, list)
                }
            }
            if (e is TextNode && e.text().isNotBlank()) {
                list.add(formatText(e.text()))
            } else if (e is Element) {
                if (e.outerHtml().contains("<img") && e.childNodeSize() == 0) {  //需要同时判断childNodeSize等于0，否则会重复添加
                    list.add(e.outerHtml())
                }
            }
        }

        /**
         * @desc 获取留园网社区导航链接集合
         * @author LL
         * @time 2018-09-08 14:39
         */
        fun scratchSiteMapTab(doc: Document): ArrayList<SiteMap> {
            try {
                val siteMap = doc.getElementById("site_map_tab")
                val links = siteMap.getElementsByTag("a")
                val list = ArrayList<SiteMap>()
                for (link in links) {
                    val item = SiteMap()

                    item.title = link.text().convertToSimplifiedChinese()
                    item.url = link.attr("href")

                    //插入数据库中
                    getSiteMapBox().put(item)

                    list.add(item)
                }
                return list
            } catch (t: Throwable) {
                throw Exceptions.propagate(t)
            }
        }


        //endregion
    }
}

//TODO：使用Retrofit + Jsoup抓取分析

//TODO： 选中要折叠的代码后，按Ctrl + Alt + T 实现类似VS中Region的功能
