package com.lm.ll.spark.repository

import android.util.Log
import com.lm.ll.spark.db.Article
import com.lm.ll.spark.api.TabooBooksApiService
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers


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
    fun getArticles(pageNo: Int) {
        tabooBooksApiService.loadDataByString(pageNo)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ result ->
                    Log.d("Result", result.toString())
                }, { error ->
                    error.printStackTrace()
                })
    }
}