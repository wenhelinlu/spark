package com.lm.ll.spark.repository

import android.util.Log
import com.lm.ll.spark.api.TabooBooksApiService
import com.lm.ll.spark.util.Spider
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.jsoup.Jsoup


/**
 * 作者：Created by ll on 2018-06-25 20:29.
 * 邮箱：wenhelinlu@gmail.com
 */
class TabooArticlesRepository(private val tabooBooksApiService: TabooBooksApiService) {

    /**
     * @desc 获取文章列表
     * @author ll
     * @time 2018-06-26 20:51
     */
    fun getArticles(pageNo: String) {
        tabooBooksApiService.loadDataByString(pageNo)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ result ->
                    val document = Jsoup.parse(result.toString())
                    val list = Spider.scratchArticleList(document)
                    Log.d("Result", list.count().toString())

                }, { error ->
                    error.printStackTrace()
                })
    }
}