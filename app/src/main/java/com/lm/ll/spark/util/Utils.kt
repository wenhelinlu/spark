package com.lm.ll.spark.util

import java.text.SimpleDateFormat

/**
 * String类的扩展方法，将Stirng类型的日期转换为指定格式的日期字符串形式
 */
fun String.toDate(format: String = "yyyy-MM-dd") :String{
    val sdf = SimpleDateFormat(format)
    val date = sdf.parse(this) //this表示要转换的原日期字符串
    return date.toString()
}