package com.lm.ll.spark.api

import android.util.Log
import com.lm.ll.spark.BuildConfig
import com.lm.ll.spark.http.PersistentCookieJarHelper
import com.lm.ll.spark.util.LOG_TAG_OKHTTP3
import io.reactivex.Observable
import okhttp3.CookieJar
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.*
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit


/**
 * 作者：Created by ll on 2018-06-25 20:03.
 * 邮箱：wenhelinlu@gmail.com
 */
interface TabooBooksApiService {

    /**
     * @desc 获取文章列表接口
     * @author ll
     * @time 2018-08-20 20:01
     */
    @GET("index.php?app=forum&act=cachepage")
    fun getArticleList(@Query("cp") pageNo: String): Observable<String>

    /**
     * @desc 查询文章接口
     * @author ll
     * @time 2018-08-20 20:01
     */
    @GET("index.php?action=search&bbsdr=life6&act=threadsearch&app=forum&submit=%B2%E9%D1%AF")
    fun queryArticle(@Query("keywords") keywords: String): Observable<String>

    /**
     * @desc 抓取正文接口
     * @author ll
     * @time 2018-08-20 20:00
     */
    @GET
    fun getArticle(@Url url: String): Observable<String>

    /**
     * @desc 网站登录接口
     * @author ll
     * @time 2018-08-20 20:00
     */
    @FormUrlEncoded
    @POST("https://home.6park.com/index.php?app=login&act=dologin")
    fun login(@Field("username") username: String, @Field("password") password: String, @Field("dologin") dologin: String):Observable<String>

    /**
     * @desc 注销接口
     * @author lm
     * @time 2018-08-26 20:24
     */
    @GET("https://home.6park.com/index.php?app=login&act=logout")
    fun logout(): Observable<String>

    /**
     * @desc 获取个人信息接口
     * @author LL
     * @time 2018-08-27 14:32
     */
    @GET("https://home.6park.com/index.php")
    fun getProfileInfo(): Observable<String>

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

        /**
         * @desc 配置okHttpCient
         * @author lm
         * @time 2018-07-13 20:59
         */
        private fun genericClient(): OkHttpClient {
            return OkHttpClient.Builder().cookieJar(PersistentCookieJarHelper.getCookieJar() as CookieJar)
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
                         *  okhttp3没有检测到字符编码信息，自动以UTF-8方式解析，导致最终结果是乱码。
                         *  所以添加此interceptor，将responsebody中的字符以gbk编码解析，然后重新组装成新的Response                     *
                         *
                         */
                        val response = chain.proceed(chain.request())

                        //获取Response Headers中的ContentType信息
                        val contentType = response.body()!!.contentType()
                        //获取ContentType中的charset
                        val charset = contentType!!.charset()
                        //如果charset为null，即Response Headers中的Content-Type信息不包含字符编码信息，则以gbk解析
                        if (charset == null) {
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

                            //重新生成ResponseBody
                            val body = ResponseBody.create(contentType, content)
                            response.newBuilder().body(body).build()
                        } else {
                            chain.proceed(chain.request())
                        }
                    }
                    .addInterceptor(getLoggingInterceptor())
                    .build()
        }

        /**
         * @desc 日志拦截器
         * @author LL
         * @time 2018-08-27 14:31
         */
        private fun getLoggingInterceptor(): HttpLoggingInterceptor {
            val interceptor = HttpLoggingInterceptor(HttpLoggingInterceptor.Logger {
                Log.d(LOG_TAG_OKHTTP3, it)
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