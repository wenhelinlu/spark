package com.lm.ll.spark.application

import android.app.Application
import android.preference.PreferenceManager
import com.lm.ll.spark.BuildConfig
import com.lm.ll.spark.db.Article
import com.lm.ll.spark.util.NIGHT_MODE
import com.lm.ll.spark.util.ObjectBox
import com.lm.ll.spark.util.switchDayNightMode
import io.objectbox.android.AndroidObjectBrowser

/**
 * 作者：Created by ll on 2018-06-15 14:40.
 * 邮箱：wenhelinlu@gmail.com
 */
class InitApplication : Application() {
    private var isNightModeEnabled = false

    companion object {
        private var singleton: InitApplication? = null
        public var curArticle:Article? = null

        fun getInstance(): InitApplication {
            if (singleton == null) {
                singleton = InitApplication()
            }
            return singleton!!
        }
    }

    override fun onCreate() {
        super.onCreate()
        ObjectBox.build(this)
        if (BuildConfig.DEBUG) {
            AndroidObjectBrowser(ObjectBox.boxStore).start(this)
        }


        singleton = this
        val mPrefs = PreferenceManager.getDefaultSharedPreferences(this)
        this.isNightModeEnabled = mPrefs.getBoolean(NIGHT_MODE, false) //获取夜间模式配置

        //根据配置切换日间\夜间模式
        switchDayNightMode(this.isNightModeEnabled)
    }

    fun isNightModeEnabled(): Boolean {
        return isNightModeEnabled
    }

    /**
     * 保存夜间模式配置
     */
    fun setIsNightModeEnabled(isNightModeEnabled: Boolean) {
        this.isNightModeEnabled = isNightModeEnabled

        val mPrefs = PreferenceManager.getDefaultSharedPreferences(this)
        val editor = mPrefs.edit()
        editor.putBoolean(NIGHT_MODE, isNightModeEnabled)
        editor.apply()
    }
}