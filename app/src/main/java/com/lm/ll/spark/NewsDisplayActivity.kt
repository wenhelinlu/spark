package com.lm.ll.spark

import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.lm.ll.spark.adapter.NewsAdapter
import com.lm.ll.spark.db.News
import com.lm.ll.spark.decoration.NewsItemDecoration
import com.lm.ll.spark.util.Spider
import kotlinx.android.synthetic.main.activity_display_news.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async


/**
 * 作者：Created by ll on 2018-05-28 14:56.
 * 邮箱：wenhelinlu@gmail.com
 */
class NewsDisplayActivity: AppCompatActivity() {

    //接收从文章列表传过来的被点击的文章Model
    private var news: News? = null
    //此文章下的首层评论
    private var comments: ArrayList<News> = ArrayList()
    //底部工具栏
    private var toolbarBottomText:ConstraintLayout? = null
    //评论adapter
    private var commentsAdapter: NewsAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display_news)

        toolbarBottomText = findViewById(R.id.toolbar_bottom_text)
        news = intent.getParcelableExtra("news")

        val linearLayoutManager = LinearLayoutManager(this@NewsDisplayActivity)

        this.recyclerViewComment.addItemDecoration(NewsItemDecoration(2))
        this.recyclerViewComment.layoutManager = linearLayoutManager

        //显示或隐藏底栏
        scrollviewText.setOnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
            var isShow = true
            if (scrollY - oldScrollY > 0) {
                isShow = false
            } else if (oldScrollY - scrollY > 0) {
                isShow = true
            }
            showBottomToolbar(isShow)
        }

        loadText()
    }

    /**
     * @desc 加载文章列表
     * @author ll
     * @time 2018-05-29 19:40
     */
    private fun loadText(){
        val deferredLoad = async(CommonPool) {
            val spider = Spider()
            news = spider.scratchText(news!!)
            comments = spider.scratchComments(news!!)
        }

        async(UI) {
            deferredLoad.await()

            tvText.text = news!!.text

            commentsAdapter = NewsAdapter(this@NewsDisplayActivity, comments)
            this@NewsDisplayActivity.recyclerView.adapter = commentsAdapter
            this@NewsDisplayActivity.recyclerView.adapter.notifyDataSetChanged()
        }
    }

    /**
     * @desc 滑动正文内容时显示或隐藏底栏
     * @author ll
     * @time 2018-06-03 08:14
     */
    private fun showBottomToolbar(isShow: Boolean){
        if (isShow) {
            toolbarBottomText!!.visibility = View.VISIBLE
//            val animation = AnimationUtils.loadAnimation(this@NewsDisplayActivity, R.anim.fab_jump_from_down)
//            toolbarBottomText!!.startAnimation(animation)
        } else {
            toolbarBottomText!!.visibility = View.GONE
//            val animation = AnimationUtils.loadAnimation(this@NewsDisplayActivity, R.anim.fab_jump_to_down)
//            toolbarBottomText!!.startAnimation(animation)
        }
    }
}

