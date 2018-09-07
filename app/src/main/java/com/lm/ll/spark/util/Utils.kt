package com.lm.ll.spark.util

import android.annotation.SuppressLint
import android.content.Context
import android.database.MatrixCursor
import android.net.ConnectivityManager
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatDelegate
import android.widget.Toast
import com.hankcs.hanlp.HanLP
import com.lm.ll.spark.application.InitApplication
import com.lm.ll.spark.db.QueryRecord
import com.lm.ll.spark.db.QueryRecord_
import com.lm.ll.spark.enum.ForumType
import com.lm.ll.spark.util.ObjectBox.getQueryRecordBox
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


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
//        ChineseConverter.convert(this, ConversionType.T2S, InitApplication.getInstance())
        HanLP.convertToSimplifiedChinese(this)
    }
//    return this
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

    var canSwitch = false
    //获取自动切换夜间模式设置
    val autoSwitchNightMode = PreferenceManager.getDefaultSharedPreferences(InitApplication.getInstance()).getBoolean("auto_night_mode_switch", false)
    //如果开启自动切换，则判断当前时间，在22:00到06:00之间使用夜间模式
    if (autoSwitchNightMode) {
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)  //24小时制，如果是Calendar.HOUR，则为12小时制
        if (currentHour >= NIGHT_MODE_START_HOUR || currentHour <= NIGHT_MODE_END_HOUR) {
            InitApplication.getInstance().setIsNightModeEnabled(true)
            canSwitch = true
        }
    }
    if (isNightMode || canSwitch) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
    } else {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }
}

/**
 * @desc 获取用于展现文章评论列表父子层次关系的占位符
 * @author LL
 * @time 2018-09-06 14:01
 */
fun getPlaceholder(length: Int): String {
    val l = length * 10
    val sb = StringBuilder()
    for (i in 0..l) {
        sb.append(" ")
    }
    return sb.toString()
}

/**
 * @desc 获取设置的正文字体大小
 * @author LL
 * @time 2018-09-06 14:03
 */
fun getArticleTextSize(): Float {
    return PreferenceManager.getDefaultSharedPreferences(InitApplication.getInstance()).getString("font_size_list", "16")!!.toFloat()
}

/**
 * 判断网络是否连接
 */
fun isNetworkConnected(): Boolean {
    val connMgr = InitApplication.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val networkInfo = connMgr.activeNetworkInfo
    return networkInfo != null && networkInfo.isConnected
}

/**
 *
 * 判断网络是否连通（通过ping google网站判断）
 */
fun isNetworkOnline(): Boolean {
    val runtime = Runtime.getRuntime()
    try {
        val ipProcess = runtime.exec("ping -c 3 www.baidu.com")
        val exitValue = ipProcess.waitFor()
        return exitValue == 0
    } catch (e: IOException) {
        e.printStackTrace()
    } catch (e: InterruptedException) {
        e.printStackTrace()
    }

    return false
}

/**
 * @desc 将查询记录转换成SearchView需要的Cursor
 * @author ll
 * @time 2018-08-15 10:52
 * @param records 数据库中保存的查询历史记录
 */
fun getQueryRecordCursor(records: List<QueryRecord>): MatrixCursor {
    val cursor = MatrixCursor(arrayOf("_id", "text"))
    for (r in records) {
        val row = arrayOf(r.id, r.keyword)
        cursor.addRow(row)
    }
    return cursor
}

/**
 * @desc 根据条件获取查询记录
 * @author ll
 * @time 2018-08-15 15:19
 * @param keyword 查询条件
 */
fun getQueryRecord(keyword: String = "", forumType: ForumType = ForumType.TABOO_BOOK): List<QueryRecord> {
    return if (keyword.isBlank()) getQueryRecordBox().all else getQueryRecordBox().query().equal(QueryRecord_.queryType, forumType.ordinal.toLong()).contains(QueryRecord_.keyword, keyword).orderDesc(QueryRecord_.insertTime).build().find()
}

//endregion

//region 全局常量


//文章列表初次加载时最小行数
const val LIST_MIN_COUNT = 25

//禁忌书屋基地址
const val BASE_URL = "https://www.cool18.com/bbs4/"

//当前列表数据源URL地址（未附加页数）(在11页之前是这个地址，不包括第11页)
const val CURRENT_BASE_URL = "index.php?app=forum&act=cachepage&cp=tree" //禁忌书屋

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

//如果文本中段落标记（\r\n\r\n）个数大于此值，则按照规则清除换行标记，保留段落标记
const val PARAGRAPH_FLAG_COUNT_LIMIT = 20

//用于登录时用Intent传递个人档案页面文字的key
const val PROFILE_INFO_KEY = "profile"

//已登录状态标记
const val LOGINING_STATUS = "欢迎您"

//自动夜间模式开始时间
const val NIGHT_MODE_START_HOUR = 22

//自动夜间模式结束时间
const val NIGHT_MODE_END_HOUR = 6


//endregion