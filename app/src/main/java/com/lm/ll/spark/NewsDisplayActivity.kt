package com.lm.ll.spark

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.lm.ll.spark.adapter.CommentRecyclerViewAdapter
import com.lm.ll.spark.db.News
import com.lm.ll.spark.decoration.DashlineItemDecoration
import com.lm.ll.spark.util.Spider
import kotlinx.android.synthetic.main.activity_display_news.*
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async


/**
 * 作者：Created by ll on 2018-05-28 14:56.
 * 邮箱：wenhelinlu@gmail.com
 */
class NewsDisplayActivity: AppCompatActivity() {

    //接收从文章列表传过来的被点击的文章Model
    private lateinit var news: News
    //此文章下的首层评论
    private var comments: ArrayList<News> = ArrayList()
    //评论adapter
    private lateinit var commentsAdapter: CommentRecyclerViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display_news)

        news = intent.getParcelableExtra("news")

        //跟?结合使用， let函数可以在对象不为 null 的时候执行函数内的代码，从而避免了空指针异常的出现。
        supportActionBar?.let {
            it.title = news.title
        }

        val linearLayoutManager = LinearLayoutManager(this@NewsDisplayActivity)

        this.recyclerViewComment.addItemDecoration(DashlineItemDecoration())
        this.recyclerViewComment.layoutManager = linearLayoutManager
        this.recyclerViewComment.isNestedScrollingEnabled = false

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
     * @desc 加载文章正文和评论
     * @author ll
     * @time 2018-05-29 19:40
     */
    private fun loadText(){
        val deferredLoad = async(CommonPool) {
            val spider = Spider()
            news = spider.scratchText(news!!, comments) //正文中可能也包含链接（比如精华区）
            comments.reverse() //因为在精华区中，章节链接是倒序显示，所以将其翻转
            comments.addAll(spider.scratchComments(news!!))
        }

        async(UI) {
            deferredLoad.await()

            tvText.text = news.text
            viewDivider.visibility = View.VISIBLE
            //在正文加载完成后再显示评论区提示
            tvCommentRemark.text = this@NewsDisplayActivity.getString(R.string.comment_remark)

            commentsAdapter = CommentRecyclerViewAdapter(this@NewsDisplayActivity, comments)
            recyclerViewComment.adapter = commentsAdapter
            recyclerViewComment.adapter.notifyDataSetChanged()
        }
    }

    /**
     * @desc 滑动正文内容时显示或隐藏底栏
     * @author ll
     * @time 2018-06-03 08:14
     */
    private fun showBottomToolbar(isShow: Boolean){
        if (isShow) {
            toolbar_bottom_text.visibility = View.VISIBLE
//            val animation = AnimationUtils.loadAnimation(this@NewsDisplayActivity, R.anim.fab_jump_from_down)
//            toolbarBottomText!!.startAnimation(animation)
        } else {
            toolbar_bottom_text.visibility = View.GONE
//            val animation = AnimationUtils.loadAnimation(this@NewsDisplayActivity, R.anim.fab_jump_to_down)
//            toolbarBottomText!!.startAnimation(animation)
        }
    }
}

