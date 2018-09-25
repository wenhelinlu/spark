package com.lm.ll.spark.util

import android.annotation.SuppressLint
import android.content.Context
import android.database.MatrixCursor
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatDelegate
import android.widget.Toast
import com.hankcs.hanlp.HanLP
import com.lm.ll.spark.application.InitApplication
import com.lm.ll.spark.db.QueryRecord
import com.lm.ll.spark.db.QueryRecord_
import com.lm.ll.spark.enum.ForumType
import com.lm.ll.spark.util.GlobalConst.Companion.NIGHT_MODE_END_HOUR
import com.lm.ll.spark.util.GlobalConst.Companion.NIGHT_MODE_START_HOUR
import com.lm.ll.spark.util.ObjectBox.getQueryRecordBox
import java.io.IOException
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern


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
    return PreferenceManager.getDefaultSharedPreferences(InitApplication.getInstance()).getString("font_size_list", "18")!!.toFloat()
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
fun getImageSizeAhead(imageUrl:String):IntArray{
    val options = BitmapFactory.Options()
    options.inJustDecodeBounds = true
    val bitmap = BitmapFactory.decodeStream(URL(imageUrl).openStream(),null, options)
    return intArrayOf(bitmap!!.width, bitmap.height)
}

//endregion