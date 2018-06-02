package com.lm.ll.spark.util

import java.text.SimpleDateFormat

fun String.toDate(format: String = "yyyy-MM-dd") :String{
    val sdf = SimpleDateFormat(format)
    val date = sdf.parse(this)
    return date.toString()
}