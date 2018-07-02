package com.lm.ll.spark.api

import io.reactivex.Observable
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url
import java.util.concurrent.TimeUnit


/**
 * 作者：Created by ll on 2018-06-25 20:03.
 * 邮箱：wenhelinlu@gmail.com
 */
interface TabooBooksApiService {

    @GET("index.php?app=forum&act=cachepage")
    fun getArticleList(@Query("cp") pageNo: String): Observable<String>

    @GET
    fun getArticle(@Url url: String): Observable<String>

    companion object Factory {
        private const val API_SERVER_URL = "https://www.cool18.com/bbs4/"
        private const val TIMEOUT: Long = 30000 //超时时长

        /**
         * @desc 创建ApiStores实例
         * @author ll
         * @time 2018-06-25 20:18
         */
        fun create(): TabooBooksApiService {
            val retrofit = Retrofit.Builder()
                    .baseUrl(API_SERVER_URL)
                    .client(genericClient())
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build()

            return retrofit.create(TabooBooksApiService::class.java)
        }

        //配置okHttpCient
        private fun genericClient(): OkHttpClient {
            return OkHttpClient.Builder()
                    .retryOnConnectionFailure(true)
                    .connectTimeout(TIMEOUT, TimeUnit.MILLISECONDS)
                    .readTimeout(TIMEOUT,TimeUnit.MILLISECONDS)
                    .writeTimeout(TIMEOUT,TimeUnit.MILLISECONDS)
                    .addInterceptor { chain ->
                        val request = chain.request()
                                .newBuilder()
                                .addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                                .addHeader("Connection", "keep-alive")
                                .addHeader("Accept", "*/*")
                                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.87 Safari/537.36")
                                .build()
                        chain.proceed(request)
                    }
                    .build()
        }

    }
}

//TODO：Keep in mind that if you use Retrofit you shouldn't need defer(), as retrofit will re-initiate the call when a new subscription happens.