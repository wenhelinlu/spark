package com.lm.ll.spark.util

import io.reactivex.Observable
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.http.GET
import retrofit2.http.Path


/**
 * 作者：Created by ll on 2018-06-25 20:03.
 * 邮箱：wenhelinlu@gmail.com
 */
interface ApiStores {
    @GET("index.php?app=forum&act=cachepage&cp=tree{pageNo}")
    fun loadDataByString(@Path("pageNo") pageNo: String): Observable<String>

    companion object Factory{
        private const val API_SERVER_URL = "https://www.cool18.com/bbs4/"

        /**
         * @desc 创建ApiStores实例
         * @author ll
         * @time 2018-06-25 20:18
         */
        fun create(): ApiStores{
            val retrofit = Retrofit.Builder()
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .baseUrl(API_SERVER_URL)
                    .build()

            return retrofit.create(ApiStores::class.java)
        }
    }
}