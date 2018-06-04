package com.lm.ll.spark.util

import android.annotation.SuppressLint
import java.text.SimpleDateFormat

@SuppressLint("SimpleDateFormat")
        /**
 * String类的扩展方法，将Stirng类型的日期转换为指定格式的日期字符串形式
 */
fun String.toDate(format: String = "yyyy-MM-dd") :String{
    val sdf = SimpleDateFormat(format)
    val date = sdf.parse(this) //this表示要转换的原日期字符串
    return date.toString()
}

//文章列表初次加载时最小行数
const val MIN_ROWS: Int = 15

//当前列表数据源URL地址（未附加页数）
const val CURRENT_BASE_URL: String = "https://www.cool18.com/bbs4/index.php?app=forum&act=cachepage&cp=tree" //禁忌书屋
//    private val URL: String = "https://site.6parker.com/chan1/index.php?app=forum&act=cachepage&cp=tree" //史海钩沉
