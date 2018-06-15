package com.lm.ll.spark.application

import android.app.Application
import android.preference.PreferenceManager
import com.lm.ll.spark.util.NIGHT_MODE


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
        singleton = this
        val mPrefs = PreferenceManager.getDefaultSharedPreferences(this)
        this.isNightModeEnabled = mPrefs.getBoolean(NIGHT_MODE, false)
    }

    fun isNightModeEnabled(): Boolean {
        return isNightModeEnabled
    }

    fun setIsNightModeEnabled(isNightModeEnabled: Boolean) {
        this.isNightModeEnabled = isNightModeEnabled

        val mPrefs = PreferenceManager.getDefaultSharedPreferences(this)
        val editor = mPrefs.edit()
        editor.putBoolean(NIGHT_MODE, isNightModeEnabled)
        editor.apply()
    }
}