package com.lm.ll.spark.util

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.lm.ll.spark.application.InitApplication
import java.text.SimpleDateFormat
import java.util.*

/**
 * @desc Firebase工具类
 * @author LL
 * @time 2018-08-27 14:21
 */
class FirebaseLogUtils {
    companion object {
        private var mFirebaseAnalytics: FirebaseAnalytics? = null
        private var dateFormat: SimpleDateFormat? = null

        /**
         * @desc 统计数据
         * @author LL
         * @time 2018-08-27 14:21
         * @param key 数据标记
         * @param value 统计数据内容
         */
        fun log(key: String, value: Bundle?) {
//            if (BuildConfig.DEBUG) {
//                return
//            }
            if (mFirebaseAnalytics == null) {
                mFirebaseAnalytics = FirebaseAnalytics.getInstance(InitApplication.getInstance())
            }
            if (dateFormat == null) {
                dateFormat = SimpleDateFormat("yyyy/MM/dd-hh:mm", Locale.getDefault())
            }
            var bundle = value
            if (bundle == null) {
                bundle = Bundle()
            }
            val time = dateFormat!!.format(System.currentTimeMillis())
            value!!.putString("time", time)
            mFirebaseAnalytics!!.logEvent(key, bundle)
        }

        fun log(key: String) {
            log(key, null)
        }
    }
}