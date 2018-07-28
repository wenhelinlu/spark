package com.lm.ll.spark.util

import android.annotation.SuppressLint
import android.content.Context
import android.support.v7.app.AppCompatDelegate
import android.widget.Toast
import com.lm.ll.spark.application.InitApplication
import com.zqc.opencc.android.lib.ChineseConverter
import com.zqc.opencc.android.lib.ConversionType
import java.text.SimpleDateFormat


//region 扩展方法


/**
 * @desc String类的扩展方法，将String类型的日期转换为指定格式的日期字符串形式
 * @author ll
 * @time 2018-07-09 16:23
 * @return 包含正文的文章链接
 */
@SuppressLint("SimpleDateFormat")
fun String.toFormatedDate(format: String = "yyyy-MM-dd"): String {
    val sdf = SimpleDateFormat(format)
    val date = sdf.parse(this) //this表示要转换的原日期字符串
    return date.toString()
}

/**
 * @desc String类的扩展方法，将String转换成简体中文
 * @author ll
 * @time 2018-07-09 16:23
 * @return 简体中文
 */
fun String.convertToSimplifiedChinese(): String {
    return if (this.isBlank()) {
        this
    } else {
        ChineseConverter.convert(this, ConversionType.T2S, InitApplication.getInstance())
    }
}

//简化的Toast方法
fun Context.toast(message: CharSequence, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

//endregion

//region 全局方法


/**
 *  setDefaultNightMode()与setLocalNightMode()区别
 *  AppCompatDelegate.setDefaultNightMode()是对整个App中theme为DayNight主题生效
 *  getDelegate().setLocalNightMode()只对特定的组件生效
 *
 * @desc 切换日间\夜间模式
 * @author ll
 * @time 2018-06-15 15:28
 */
fun switchDayNightMode(isNightMode: Boolean) {
    if (isNightMode) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
    } else {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }
}

fun getPlaceholder(length: Int): String {
    val l = length * 10
    val sb = StringBuilder()
    for (i in 0..l) {
        sb.append(" ")
    }
    return sb.toString()
}


//endregion

//region 全局常量


//文章列表初次加载时最小行数
const val LIST_MIN_COUNT = 25

//禁忌书屋基地址
const val BASE_URL = "https://www.cool18.com/bbs4/"

//禁忌书屋精华区数据源URL地址（未附加页数）
const val CURRENT_ELITEAREA_BASE_URL = "index.php?app=forum&act=gold&p="

//是否是情色经典书库中的文章
const val IS_CLASSIC_ARTICLE = "classic"

//下拉刷新操作触发距离
const val PULL_REFRESH_DISTANCE = 400

//抓取网页设置的Useragent，防止被服务器阻止
const val USER_AGENT = "User-Agent,Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.87 Safari/537.36" //使用PC版User-Agent，如果使用移动版User-Agent，会导致正文解析错误

//连接超时时长
const val TIME_OUT = 50000

//存储夜间模式设置的键
const val NIGHT_MODE = "NIGHT_MODE"

//常规Log的tag
const val LOG_TAG_COMMON = "SPARK_LOG_COMMON"

//okhttp3的log tag
const val LOG_TAG_OKHTTP3 = "SPARK_LOG_OKHTTP3"


//endregion