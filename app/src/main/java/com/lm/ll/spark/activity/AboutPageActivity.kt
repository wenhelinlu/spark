package com.lm.ll.spark.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.lm.ll.spark.R
import com.lm.ll.spark.util.AppVersionUtils
import mehdi.sakout.aboutpage.AboutPage
import mehdi.sakout.aboutpage.Element

class AboutPageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val versionElement = Element()
        versionElement.title = AppVersionUtils.getVersionName()
        val aboutPage = AboutPage(this)
                .isRTL(false)
                .setImage(R.drawable.splash_logo_vector)
                .addItem(versionElement)
                .addGroup("Connect with us")
                .addEmail("wenhelinlu@gmail.com")
                .addWebsite("https://github.com/wenhelinlu")
                .addGitHub("wenhelinlu")
                .addInstagram("wenhelinlu")
                .create()

        setContentView(aboutPage)
    }
}
