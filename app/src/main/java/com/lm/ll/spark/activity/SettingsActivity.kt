package com.lm.ll.spark.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.lm.ll.spark.R
import com.lm.ll.spark.fragment.GeneralPreferenceFragment

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        this.supportFragmentManager.beginTransaction().replace(R.id.settingsContent, GeneralPreferenceFragment()).commit()
    }
}
