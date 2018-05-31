package com.lm.ll.spark

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.ListView
import com.lm.ll.spark.adapter.NewsAdapter
import com.lm.ll.spark.db.News
import com.lm.ll.spark.util.Spider

import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import android.content.Intent
import android.util.Log
import android.view.View

import android.widget.AdapterView
import android.widget.Button


class MainActivity : AppCompatActivity() {

    private var newsList:ArrayList<News>? = null
    private var adapter: NewsAdapter? = null
    private var lv: ListView? = null
    private var btnPre: Button? = null
    private var btnNext: Button? = null
    private val URL: String = "https://www.cool18.com/bbs4/index.php?app=forum&act=cachepage&cp=tree"
    private var currentPage: Int = 1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnPre = findViewById(R.id.btn_pre)
        btnPre!!.setOnClickListener{
            if(currentPage > 1) {
                currentPage--
                loadContent()
            }
        }

        btnNext = findViewById(R.id.btn_next)
        btnNext!!.setOnClickListener {
            currentPage++
            loadContent()
        }
        newsList = ArrayList<News>()
        lv = findViewById(R.id.news_lv)

        loadContent()
    }

    /**
     * @desc 加载文章列表
     * @author ll
     * @time 2018-05-29 19:40
     */
    private fun loadContent(){
        val deferred1 = async(CommonPool) {
            val spider = Spider()
            newsList = spider.scratchContent("$URL$currentPage")
        }

        async(UI) {
            deferred1.await()
            adapter = NewsAdapter(this@MainActivity, newsList!!)
            lv!!.setAdapter(adapter)
            lv!!.setOnItemClickListener(object : AdapterView.OnItemClickListener {
                override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                    val news = newsList!![position]
                    val intent = Intent(this@MainActivity, NewsDisplayActivity::class.java)
                    Log.d("itemurl","${news.url}")
                    intent.putExtra("news", news)

                    startActivity(intent)
                }
            })
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}
