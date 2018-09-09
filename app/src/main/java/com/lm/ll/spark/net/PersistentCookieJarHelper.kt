package com.lm.ll.spark.net

import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import com.lm.ll.spark.application.InitApplication

/**
 * @desc PersistentCookieJar的辅助工具类
 * @author ll
 * @time 2018-08-27 19:53
 */
class PersistentCookieJarHelper {
    companion object {
        private var cookie: PersistentCookieJar? = null

        /**
         * @desc 获取PersistentCookieJar实例
         * @author LL
         * @time 2018-08-27 19:48
         */
        fun getCookieJar(): PersistentCookieJar? {
            if (cookie == null) {
                cookie = PersistentCookieJar(SetCookieCache(), SharedPrefsCookiePersistor(InitApplication.getInstance()))
            }
            return cookie
        }
    }
}