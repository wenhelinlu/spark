package com.lm.ll.spark.util

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.database.MatrixCursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import com.github.liuyueyi.quick.transfer.ChineseUtils
import com.lm.ll.spark.application.InitApplication
import com.lm.ll.spark.db.Article
import com.lm.ll.spark.db.Comment
import com.lm.ll.spark.db.QueryRecord
import com.lm.ll.spark.db.QueryRecord_
import com.lm.ll.spark.enum.ForumType
import com.lm.ll.spark.util.GlobalConst.Companion.NIGHT_MODE_END_HOUR
import com.lm.ll.spark.util.GlobalConst.Companion.NIGHT_MODE_START_HOUR
import com.lm.ll.spark.util.ObjectBox.getQueryRecordBox
import io.objectbox.kotlin.query
import io.reactivex.exceptions.Exceptions
import retrofit2.HttpException
import java.io.IOException
import java.net.ConnectException
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeoutException
import java.util.regex.Pattern
import javax.net.ssl.SSLHandshakeException

//region 扩展方法

//是否设置了自动进行繁简转换
val autoTranslate = PreferenceManager.getDefaultSharedPreferences(InitApplication.getInstance()).getBoolean("auto_t2s", false)

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
 * @param forceConvert 强制进行繁简转换
 * @return 简体中文
 */
fun String.convertToSimplifiedChinese(forceConvert: Boolean = false): String {
    return if (this.isBlank()) {
        this
    } else {
        //根据设置中是否启用自动繁简转换来操作
//        Log.d(LOG_TAG_COMMON, " auto_t2s = $autoTranslate")
        if (autoTranslate || forceConvert) {
            //ChineseConverter.convert(this, ConversionType.T2S, InitApplication.getInstance())  //opencc-android转换库的用法
            //HanLP.convertToSimplifiedChinese(this)
            ChineseUtils.t2s(this) //使用com.github.liuyueyi.quick-chinese-transfer库进行繁简转换
        } else {
            this
        }
    }
}

/**
 * @description 替换多个关键词
 * @date: 2020-03-28 15:29
 * @author: LuHui
 * @param
 * @return
 */
fun String.replace(vararg replacements: Pair<String, String>): String {
    var result = this
    replacements.forEach { (l, r) -> result = result.replace(l, r) }
    return result
}

/**
 * @description 简化的Toast方法
 * @date: 2020-03-28 15:33
 * @author: LuHui
 * @param
 * @return
 */
fun Context.toast(message: CharSequence, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

/**
 * @desc Article类的深拷贝
 * @author Administrator
 * @time 2019-01-29 17:44
 */

fun Article.deepCopy(): Article {
    var newArticle = Article()
    newArticle.text = this.text
    newArticle.title = this.title
    newArticle.articleFlag = this.articleFlag
    newArticle.author = this.author
    newArticle.classicalFlag = this.classicalFlag
    newArticle.date = this.date
    newArticle.depth = this.depth
    newArticle.favorite = this.favorite
    newArticle.id = this.id
    newArticle.insertTime = this.insertTime
    newArticle.leavePosition = this.leavePosition
    newArticle.offset = this.offset
    newArticle.readCount = this.readCount
    newArticle.textLength = this.textLength
    newArticle.url = this.url

    val commentList = ArrayList<Comment>()
    commentList.addAll(this.comments)

    return newArticle
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
    return PreferenceManager.getDefaultSharedPreferences(InitApplication.getInstance()).getString("font_size_list", "18")!!.toFloat()
}

/**
 * @description 获取设置的正文行距
 * @date: 2021-02-08 16:57
 * @author: LuHui
 * @param
 * @return
 */
fun getArticleLineSpace(): Float {
    return PreferenceManager.getDefaultSharedPreferences(InitApplication.getInstance()).getString("line_space_list", "1.2")!!.toFloat()
}

/**
 * @description 判断是否有网络连接
 * @date: 2021-01-28 19:00
 * @author: LuHui
 * @param
 * @return
 */
private fun isNetworkAvailable(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val nw = connectivityManager.activeNetwork ?: return false
        val actNw = connectivityManager.getNetworkCapabilities(nw) ?: return false
        return when {
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            //for other device how are able to connect with Ethernet
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            //for check internet over Bluetooth
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> true
            else -> false
        }
    } else {
        return connectivityManager.activeNetworkInfo?.isConnected ?: false
    }
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
//    return if (keyword.isBlank()) getQueryRecordBox().all else getQueryRecordBox().query().equal(QueryRecord_.queryType, forumType.ordinal.toLong()).contains(QueryRecord_.keyword, keyword).orderDesc(QueryRecord_.insertTime).build().find()
    return if (keyword.isBlank()) getQueryRecordBox().all else getQueryRecordBox().query {
        equal(QueryRecord_.queryType, forumType.ordinal.toLong())
        contains(QueryRecord_.keyword, keyword)
        orderDesc(QueryRecord_.insertTime)
    }.find()

}

/**
 * 获取img标签中的src值
 * @param content
 * @return
 */
fun getImgSrc(content: String): String? {
    var imgSrc: String? = null
    //目前img标签标示有3种表达式
    //<img alt="" src="1.jpg"/>   <img alt="" src="1.jpg"></img>     <img alt="" src="1.jpg">
    //开始匹配content中的<img />标签
    val pImg = Pattern.compile("<(img|IMG)(.*?)(/>|></img>|>)")
    val mImg = pImg.matcher(content)
    var resultImg = mImg.find()
    if (resultImg) {
        while (resultImg) {
            //获取到匹配的<img />标签中的内容
            val strImg = mImg.group(2)

            //开始匹配<img />标签中的src
            val pSrc = Pattern.compile("(src|SRC)=(\"|\')(.*?)(\"|\')")
            val mSrc = pSrc.matcher(strImg)
            if (mSrc.find()) {
                imgSrc = mSrc.group(3)
            }
            //结束匹配<img />标签中的src

            //匹配content中是否存在下一个<img />标签，有则继续以上步骤匹配<img />标签中的src
            resultImg = mImg.find()
        }
    }
    return imgSrc
}

/**
 * 获取网络图像尺寸
 * @param imageUrl 网络图像路径
 * @return
 */
fun getImageSizeAhead(imageUrl: String): IntArray {
    try {
        val options = BitmapFactory.Options()
        options.inPreferredConfig = Bitmap.Config.RGB_565 //压缩图片
        options.inJustDecodeBounds = true //仅返回宽高，减少内存占用
        //这里返回的Bitmap是null，因为options的inJustDecodeBounds属性设为true
        val bitmap = BitmapFactory.decodeStream(URL(imageUrl).openStream(), null, options)

        return intArrayOf(options.outWidth, options.outHeight)
    } catch (t: Throwable) {
        throw Exceptions.propagate(t)
    }
}

/**
 * 将具体异常信息转成可读的提醒
 */
fun getExceptionDesc(error: Throwable): String {
    return when (error) {
        is HttpException, is SSLHandshakeException, is ConnectException -> "网络连接异常"
        is TimeoutException -> "网络连接超时"
        is IndexOutOfBoundsException, is ClassCastException -> "解析异常"
        else -> error.toString()
    }
}

/**
 * 判断Activity是否Destroy
 * @param activity
 * @return
 */
fun isDestroy(mActivity: Activity?): Boolean {
    return mActivity == null || mActivity.isFinishing || Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && mActivity.isDestroyed
}

//endregion