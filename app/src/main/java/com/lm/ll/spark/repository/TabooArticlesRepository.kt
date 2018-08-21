package com.lm.ll.spark.repository

import com.lm.ll.spark.api.TabooBooksApiService
import com.lm.ll.spark.db.Article
import com.lm.ll.spark.db.Article_
import com.lm.ll.spark.util.ObjectBox.getArticleBox
import com.lm.ll.spark.util.Spider
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import org.jsoup.Jsoup


/**
 * 作者：Created by ll on 2018-06-25 20:29.
 * 邮箱：wenhelinlu@gmail.com
 */
class TabooArticlesRepository(private val tabooBooksApiService: TabooBooksApiService) {

    fun login(username: String, password: String, dologin: String = "+%B5%C7%C2%BC+"): Observable<String> {
        return tabooBooksApiService.login(username, password, dologin)
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
}