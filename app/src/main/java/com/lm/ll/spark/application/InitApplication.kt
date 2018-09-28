package com.lm.ll.spark.application

import android.app.Application
import android.preference.PreferenceManager
import com.lm.ll.spark.BuildConfig
import com.lm.ll.spark.db.Article
import com.lm.ll.spark.util.GlobalConst.Companion.NIGHT_MODE
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

        //用于不同Activity间数据的传递，这样不需要对Article和Comment进行Parcelable处理
        var curArticle:Article? = null
        var screenWidth:Int = 0

        fun getInstance(): InitApplication {
            if (singleton == null) {
                singleton = InitApplication()
            }
            return singleton!!
        }
    }

    override fun onCreate() {
        super.onCreate()

        //初始化ObjectBox
        ObjectBox.build(this)
        //如果是DEBUG模式，则app运行时在状态栏显示data browser提示，可以通过手机浏览器查看数据库数据
        if (BuildConfig.DEBUG) {
            AndroidObjectBrowser(ObjectBox.boxStore).start(this)
        }

        singleton = this
        val mPrefs = PreferenceManager.getDefaultSharedPreferences(this)
        this.isNightModeEnabled = mPrefs.getBoolean(NIGHT_MODE, false) //获取夜间模式配置

        //根据配置切换日间\夜间模式
        switchDayNightMode(this.isNightModeEnabled)
        screenWidth = getScreenWidth()
    }

    fun isNightModeEnabled(): Boolean {
        return isNightModeEnabled
    }

    /**
     * @desc 获取屏幕宽度（dp）
     * @author ll
     * @time 2018-09-28 14:25
     */
    private fun getScreenWidth():Int{
//        return (resources.displayMetrics.widthPixels/resources.displayMetrics.density).toInt()
        return resources.displayMetrics.widthPixels
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