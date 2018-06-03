package com.lm.ll.spark

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.ScrollView
import android.widget.TextView
import com.lm.ll.spark.db.News
import com.lm.ll.spark.util.Spider
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import android.support.constraint.ConstraintLayout
import android.view.View
import android.view.animation.AnimationUtils
import android.view.animation.Animation
import android.R.attr.scrollY




/**
 * 作者：Created by ll on 2018-05-28 14:56.
 * 邮箱：wenhelinlu@gmail.com
 */
class NewsDisplayActivity: AppCompatActivity() {

    private var news: News? = null
    private var tvText: TextView? = null
    private var scrollviewText: ScrollView? = null
    private var toolbarBottomText:ConstraintLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display_news)

        toolbarBottomText = findViewById(R.id.toolbar_bottom_text)
        scrollviewText = findViewById(R.id.scrollview_text)
        tvText = findViewById(R.id.tv_text)
        news = intent.getParcelableExtra("news")

//        tvText!!.setOnClickListener {
//            showBottomToolbar()
//        }
        scrollviewText!!.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            if (scrollY - oldScrollY > 0) {
                toolbarBottomText!!.visibility = View.GONE
                val animation = AnimationUtils.loadAnimation(this@NewsDisplayActivity, R.anim.fab_jump_to_down)
                toolbarBottomText!!.startAnimation(animation)
            } else if (oldScrollY - scrollY > 0) {
                toolbarBottomText!!.visibility = View.VISIBLE
                val animation = AnimationUtils.loadAnimation(this@NewsDisplayActivity, R.anim.fab_jump_from_down)
                toolbarBottomText!!.startAnimation(animation)
            }
        }

        loadText()
    }

    /**
     * @desc 加载文章列表
     * @author ll
     * @time 2018-05-29 19:40
     */
    private fun loadText(){
        val deferred1 = async(CommonPool) {
            val spider = Spider()
            news = spider.scratchText(news!!)
        }

        async(UI) {
            deferred1.await()
            tvText!!.text = news!!.text
        }
    }

    /**
     * @desc 点击正文内容时显示或隐藏底栏
     * @author ll
     * @time 2018-06-03 08:14
     */
    private fun showBottomToolbar(){
        async(UI) {
            if(toolbarBottomText!!.visibility == View.VISIBLE){
                toolbarBottomText!!.visibility = View.INVISIBLE
            }
            else{
                toolbarBottomText!!.visibility = View.VISIBLE
            }
        }
    }
}

