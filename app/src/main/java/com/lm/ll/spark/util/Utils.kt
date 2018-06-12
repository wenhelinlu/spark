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
const val MIN_ROWS = 15

//禁忌书屋基地址
const val BASE_URL = "https://www.cool18.com/bbs4/"
//当前列表数据源URL地址（未附加页数）
const val CURRENT_BASE_URL = "index.php?app=forum&act=cachepage&cp=tree" //禁忌书屋

//禁忌书屋精华区数据源URL地址（未附加页数）
const val CURRENT_ELITEAREA_BASE_URL = "index.php?app=forum&act=gold&p="

//正文接收的intent传递内容的key
const val ARTICLE_TEXT_INTENT_KEY = "article"

//是否是情色经典书库中的文章
const val IS_CLASSIC_ARTICLE = "classic"

//下拉刷新操作触发距离
const val PULL_REFRESH_DISTANCE = 400

//抓取网页设置的Useragent，防止被服务器阻止
const val USER_AGENT = "User-Agent,Mozilla/5.0 (Linux; Android 7.1.1; MI MAX 2 Build/NMF26F) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.83 Mobile Safari/537.36"