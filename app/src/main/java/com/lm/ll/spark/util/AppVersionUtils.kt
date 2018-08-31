package com.lm.ll.spark.util

import com.lm.ll.spark.application.InitApplication


/**
 * 作者：Created by ll on 2018-08-31 17:49.
 * 邮箱：wenhelinlu@gmail.com
 */
class AppVersionUtils {
    companion object {

        /**
         * @desc 获取App版本号
         * @author ll
         * @time 2018-08-31 17:53
         */
        fun getVersionCode():Int{
            var versionCode = 0
            try {
                versionCode = InitApplication.getInstance().packageManager.getPackageInfo(InitApplication.getInstance().packageName,0).versionCode
            }catch (e:Exception){
                e.printStackTrace()
            }
            return versionCode
        }

        /**
         * @desc 获取App版本名称
         * @author ll
         * @time 2018-08-31 17:58
         */
        fun getVersionName():String{
            var versionName = ""
            try {
                versionName = InitApplication.getInstance().packageManager.getPackageInfo(InitApplication.getInstance().packageName,0).versionName
            }catch (e:Exception){
                e.printStackTrace()
            }
            return versionName
        }
    }
}