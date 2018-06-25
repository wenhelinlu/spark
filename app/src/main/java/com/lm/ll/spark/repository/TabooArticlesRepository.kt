package com.lm.ll.spark.repository

import android.util.Log
import com.lm.ll.spark.db.Article
import com.lm.ll.spark.util.ApiStores
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers


/**
 * 作者：Created by ll on 2018-06-25 20:29.
 * 邮箱：wenhelinlu@gmail.com
 */
class TabooArticlesRepository(private val apiStores: ApiStores) {
    fun getArticles(pageNo: String): Observable<Article>? {
        apiStores.loadDataByString(pageNo = pageNo).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    Log.d("Result", result.toString())
                }, { error ->
                    error.printStackTrace()
                })
        return null
    }
}