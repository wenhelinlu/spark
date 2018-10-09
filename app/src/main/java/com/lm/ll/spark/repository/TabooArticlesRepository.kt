package com.lm.ll.spark.repository

import com.lm.ll.spark.api.TabooBooksApiService
import com.lm.ll.spark.db.Article
import com.lm.ll.spark.db.Article_
import com.lm.ll.spark.db.ProfileInfo
import com.lm.ll.spark.db.SubForum
import com.lm.ll.spark.net.Spider
import com.lm.ll.spark.util.ObjectBox.getArticleBox
import com.lm.ll.spark.util.ObjectBox.getSubForumBox
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import org.jsoup.Jsoup


/**
 * 作者：Created by ll on 2018-06-25 20:29.
 * 邮箱：wenhelinlu@gmail.com
 */
class TabooArticlesRepository(private val tabooBooksApiService: TabooBooksApiService) {

    /**
     * @desc 获取论坛导航链接，首先从数据库中查询，如果无数据，则从网上获取
     * @author LL
     * @time 2018-09-08 14:43
     */
    fun getSubForumList(): Observable<ArrayList<SubForum>> {
        //从数据库中获取论坛列表
        val fromDb = Observable.create(ObservableOnSubscribe<ArrayList<SubForum>> { emitter ->
            val find = getSubForumBox().all
            if (find == null || find.size == 0) {
                emitter.onComplete()
            } else {
                val list = ArrayList<SubForum>()
                list.addAll(find)
                list.sortByDescending { x -> x.favorite }
                emitter.onNext(list)
            }
        })

        //从网络中抓取论坛列表
        val fromNetwork = tabooBooksApiService.getSubForumList()
                .retry(1)
                .flatMap {
                    val doc = Jsoup.parse(it)
                    val list = Spider.scratchSubForumList(doc)
                    Observable.just(list)
                }

        return Observable.concat(fromDb, fromNetwork)
    }

    /**
     * @desc 登录操作
     * @author lm
     * @time 2018-08-26 20:24
     */
    fun login(username: String, password: String, dologin: String = "+%B5%C7%C2%BC+"): Observable<String> {
        return tabooBooksApiService.login(username, password, dologin)
    }

    /**
     * @desc 注销操作
     * @author lm
     * @time 2018-08-26 20:25
     */
    fun logout(): Observable<String> {
        return tabooBooksApiService.logout()
    }

    /**
     * @desc 验证登录状态
     * @author LL
     * @time 2018-08-27 20:31
     */
    fun checkLoginStatus(): Observable<String> {
        return tabooBooksApiService.getProfileInfo()
    }

    /**
     * @desc 获取已收藏的文章列表
     * @author lm
     * @time 2018-07-12 21:46
     */
    fun getFavoriteArticleList(): Observable<List<Article>> {
        //注意：Article_.comments中的下划线，这个Article_是ObjectBox内部生成的properties class,即属性类，通过它可以直接获取Article类的各个属性
        val articles = getArticleBox().query().orderDesc(Article_.insertTime).build().find()
        return Observable.just(articles)
    }


    /**
     * @desc 抓取文章内容，利用缓存，首先判断数据库中是否存在数据，是则从数据库中读取，否则从网络获取（如果强制刷新则直接从网络获取）
     * @author lm
     * @time 2018-07-01 11:38
     * @param article 要抓取内容的文章
     * @param isClassicalArticle 是否是经典文章（解析正文方式不同）
     * @param isForceRefresh 是否强制刷新（总是从网上抓取，不用本地存储）
     */
    fun getArticle(article: Article, isClassicalArticle: Boolean = false, isForceRefresh: Boolean = false): Observable<Article> {
        //是否是已收藏的文章（即已保存到数据库中）
        val fromDb = Observable.create(ObservableOnSubscribe<Article> { emitter ->

            val find = getArticleBox().query().equal(Article_.url, article.url!!).build().findFirst()
            if (find == null) {
                emitter.onComplete()
            } else {
                emitter.onNext(find)
            }
        })

        //从网络中抓取文章
        val fromNetwork = tabooBooksApiService.getArticle(article.url!!)
                .retry(1)
                .flatMap {
                    val doc = Jsoup.parse(it)
                    val item = if (isClassicalArticle) {
                        Spider.scratchClassicEroticaArticleText(doc, article)

                    } else {
                        Spider.scratchText(doc, article)
                    }

                    //将从网络解析的文章作为Observable传出去
                    Observable.just(item)
                }

        return if (isForceRefresh) {
            fromNetwork
        } else {
            Observable.concat(fromDb, fromNetwork)
        }
    }

    /**
     * @desc 获取图文混排文章
     * @author ll
     * @time 2018-09-06 20:27
     */
    fun getRichTextArticle(url: String): Observable<String> {
        return tabooBooksApiService.getArticle(url)
                .retry(1)
//                .flatMap {
//                    val doc = Jsoup.parse(it)
//                    val list = Spider.scratchRichTextDataList(doc)
//                    Observable.just(list)
//                }
    }

    /**
     * @desc 获取个人信息
     * @author LL
     * @time 2018-08-27 15:27
     * @param pageStr 个人信息网页内容
     */
    fun getProfileInfo(pageStr: String): Observable<ArrayList<ProfileInfo>> {
        //如果从登录界面进入个人信息页，则解析登录操作响应内容（即pageStr）
        val fromLogin = Observable.create(ObservableOnSubscribe<ArrayList<ProfileInfo>> { emitter ->
            if (pageStr.isBlank()) {
                emitter.onComplete()
            } else {
                val doc = Jsoup.parse(pageStr)
                val list = Spider.scratchProfileInfo(doc)
                emitter.onNext(list)
            }
        })

        //使用本地存储的cookie，直接打开个人信息页
        val fromCookie = tabooBooksApiService.getProfileInfo()
                .retry(1)
                .flatMap {
                    val doc = Jsoup.parse(it)
                    val list = Spider.scratchProfileInfo(doc)
                    Observable.just(list)
                }

        return if (pageStr.isBlank()) {
            fromCookie
        } else {
            fromLogin
        }
    }

}