package com.lm.ll.spark.application

import android.app.Application
import android.preference.PreferenceManager
import com.lm.ll.spark.util.NIGHT_MODE
import com.lm.ll.spark.util.switchDayNightMode
import io.realm.Realm

/**
 * 作者：Created by ll on 2018-06-15 14:40.
 * 邮箱：wenhelinlu@gmail.com
 */
class InitApplication : Application() {
    private var isNightModeEnabled = false

    companion object {
        private var singleton: InitApplication? = null

        fun getInstance(): InitApplication {
            if (singleton == null) {
                singleton = InitApplication()
            }
            return singleton!!
        }
    }

    override fun onCreate() {
        super.onCreate()

        //初始化Realm
        Realm.init(this)

        //删除默认的数据库和自定义的数据库
//        val userAddressConfig = RealmConfiguration.Builder().name("spark-db").schemaVersion(1).deleteRealmIfMigrationNeeded().build()
//        deleteRealm(Realm.getDefaultConfiguration())
//        Realm.deleteRealm(userAddressConfig)

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