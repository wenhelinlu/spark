package com.lm.ll.spark.api

import android.util.Log
import com.lm.ll.spark.BuildConfig
import io.reactivex.Observable
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url
import java.nio.charset.Charset
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
        private const val TIMEOUT: Long = 50000 //超时时长

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
                    .readTimeout(TIMEOUT, TimeUnit.MILLISECONDS)
                    .writeTimeout(TIMEOUT, TimeUnit.MILLISECONDS)
                    .addInterceptor { chain ->
                        val request = chain.request()
                                .newBuilder()
                                .addHeader("Content-Type", "text/html; charset=UTF-8")
                                .addHeader("Content-Type", "text/html; charset=gbk")
                                .addHeader("Content-Type", "text/html; charset=gb2312")
                                .addHeader("Connection", "keep-alive")
                                .addHeader("Accept", "*/*")
                                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.87 Safari/537.36")
                                .build()
                        chain.proceed(request)
                    }
                    .addInterceptor { chain ->

                        /**
                         *                      ----------使用此Interceptor的目的--------------
                         *
                         *
                         *  经典文库中的文字返回的Response Headers中的Content-Type信息不包含字符编码信息（实际应该是gb2312或者是gbk）
                         *  okhttp3没有检测到字符编码信息，自动以utf-8方式解析，导致最终结果是乱码。
                         *  所以添加此interceptor，将responsebody中的字符以gbk编码解析，然后重新组装成新的Response                     *
                         *
                         */
                        val response = chain.proceed(chain.request())

                        /**
                         * 注意：response.body().string(); 方法只能被调用一次，如果多次调用 response.body().string();
                         * 则会抛出异常Java.lang.IllegalStateException:closed
                         * 因为在执行完读取数据之后，IO 流被关闭，如果再次调用此方法，就会抛出上面的异常。
                         * 而且此方法将响应报文中的主体全部都读到了内存中，如果响应报文主体较大，可能会导致 OOM 异常。
                         * 所以更推荐使用流的方式获取响应体的内容
                         */
                        val source = response.body()!!.source()
                        source.request(java.lang.Long.MAX_VALUE)
                        val buffer = source.buffer()
                        val content = buffer.clone().readString(Charset.forName("gbk"))  //将ResponseBody中的字符以gbk编码解析，重新组装（解决经典文库中文章显示乱码的问题）

                        val contentType = response.body()!!.contentType()
                        val body = ResponseBody.create(contentType,content)
                        response.newBuilder().body(body).build()
                    }
                    .addInterceptor(getLoggingInterceptor())
                    .build()
        }

        private fun getLoggingInterceptor(): HttpLoggingInterceptor {
            val interceptor = HttpLoggingInterceptor(HttpLoggingInterceptor.Logger {
                Log.d("SPARK_LOG", it)
            })
            interceptor.level =
                    if (BuildConfig.DEBUG) {
                        HttpLoggingInterceptor.Level.HEADERS
                    } else {
                        HttpLoggingInterceptor.Level.NONE
                    }

            return interceptor
        }
    }
}

//TODO：Keep in mind that if you use Retrofit you shouldn't need defer(), as retrofit will re-initiate the call when a new subscription happens.