package com.lm.ll.spark.http

import android.content.Context
import android.util.Base64
import okhttp3.Cookie
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.util.*
import kotlin.collections.ArrayList


/**
 * 作者：Created by ll on 2018-08-20 17:16.
 * 邮箱：wenhelinlu@gmail.com
 */
class PersistentCookieHelper(val context: Context) {
    private val COOKIE_PREFS = "cookie_prefs"
    private val cache = HashMap<String, MutableList<Cookie>>()

    /**
     * @desc 存储Cookies
     *
     * 首先将Cookies存入当前缓存对象cache中，然后再将序列化后的数据存入 SharedPreferences 文件。
     *
     * @author ll
     * @time 2018-08-20 17:37
     * @param host 站点域名（或IP地址）
     * @param cookies Cookie列表
     */
    operator fun set(host: String, cookies: MutableList<Cookie>) {
        cache[host] = cookies

        val set = HashSet<String>()
        cookies.map {
            encodeBase64(it)
        }.forEach { set.add(it) }

        val prefs = context.getSharedPreferences(COOKIE_PREFS, Context.MODE_PRIVATE)
        val edit = prefs.edit()
        edit.putStringSet(host,set)
        edit.apply()
    }

    /**
     * @desc 获取Cookies
     *
     * 首先，从缓存中查询是否有可用的Cookies，如果没有再从SharedPreferences文件中查找
     *
     * @author ll
     * @time 2018-08-20 19:33
     * @param host 站点域名（或IP地址）
     * @return Cookies
     */
    operator fun get(host: String):MutableList<Cookie>?{
        val cookies = cache[host]
        return if(cookies != null && cookies.isNotEmpty()){
            cookies
        }else{
            val prefs = context.getSharedPreferences(COOKIE_PREFS,Context.MODE_PRIVATE)
            val set = prefs.getStringSet(host, null)
            if(set == null){
                null
            }else{
                val list = ArrayList<Cookie>()
                set.map { decodeBase64(it) }
                        .forEach { list.add(it) }
                cache[host] = list
                list
            }
        }
    }

    /**
     * @desc 移除某一站点的Cookies，将其从缓存和SharedPreferences文件中删除
     * @author ll
     * @time 2018-08-20 19:36
     * @param host 站点域名（或IP地址）
     */
    fun remove(host: String){
        cache.remove(host)
        val prefs = context.getSharedPreferences(COOKIE_PREFS,Context.MODE_PRIVATE)
        prefs.edit().remove(host).apply()
    }

    /**
     * @desc 从缓存和SharedPreferences文件中清空全部站点的Cookies
     * @author ll
     * @time 2018-08-20 19:37
     */
    fun clear(){
        cache.clear()
        val prefs = context.getSharedPreferences(COOKIE_PREFS,Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }

    /**
     * @desc 将一个Cookie对象序列化为字符串
     *
     * 1. 将Cookie对象转换为可序列化的SerializableCookie对象
     * 2. 将SerializableCookie序列化为ByteArray
     * 3. 将ByteArray使用Base64编码并生成字符串
     *
     * @author ll
     * @time 2018-08-20 17:27
     * @param cookie 需要序列化的 Cookie对象
     * @return 序列化之后的字符串
     */
    private fun encodeBase64(cookie: Cookie): String {
        var objectBuffer: ObjectOutputStream? = null

        try {
            val buffer = ByteArrayOutputStream()
            objectBuffer = ObjectOutputStream(buffer)
            objectBuffer.writeObject(SerializableCookie(cookie))
            val bytes = buffer.toByteArray()
            val code = Base64.encode(bytes, Base64.DEFAULT)
            return String(code)
        } catch (e: Exception) {
            throw e
        } finally {
            if (objectBuffer != null) {
                try {
                    objectBuffer.close()
                } catch (e: Exception) {
                }
            }
        }
    }


    /**
     * @desc 将一个编码后的字符串反序列化为Cookie对象
     *
     * 1. 将该字符串使用Base64解码为字节数组
     * 2. 将字节数组反序列化为SerializableCookie对象
     * 3. 从SerializableCookie对象中获取Cookie对象并返回
     *
     * @author ll
     * @time 2018-08-20 17:33
     * @param code 被编码后的序列化数据
     * @return 解码后的Cookie对象
     */
    private fun decodeBase64(code: String): Cookie {
        var objectBuffer: ObjectInputStream? = null
        try {
            val bytes = Base64.decode(code, Base64.DEFAULT)
            val buffer = ByteArrayInputStream(bytes)
            objectBuffer = ObjectInputStream(buffer)
            return (objectBuffer.readObject() as SerializableCookie).cookie()
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        } finally {
            if (objectBuffer != null) {
                try {
                    objectBuffer.close()
                } catch (e: Exception) {
                }
            }
        }
    }
}