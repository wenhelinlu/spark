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
import android.view.View

import android.widget.AdapterView


class MainActivity : AppCompatActivity() {

    private var newsList:ArrayList<News>? = null
    private var adapter: NewsAdapter? = null
    private var lv: ListView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        newsList = ArrayList<News>()
        lv = findViewById(R.id.news_lv) as ListView
        test()
//        handler = @SuppressLint("HandlerLeak")
//        object : Handler() {
//            override fun handleMessage(msg: Message) {
//                if (msg.what === 1) {
//                    adapter = NewsAdapter(this@MainActivity, newsList!!)
//                    lv!!.setAdapter(adapter)
//                    lv!!.setOnItemClickListener(object : AdapterView.OnItemClickListener {
//                        override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
//                            val news = newsList!![position]
//                            val intent = Intent(this@MainActivity, NewsDisplayActivity::class.java)
//                            intent.putExtra("news_url", news.url)
//                            startActivity(intent)
//                        }
//                    })
//                }
//            }
//        }
    }

    fun test(){
        val deferred1 = async(CommonPool) {
            println("hello1")
            val spider = Spider()
            newsList = spider.scratch("https://site.6parker.com/chan1/index.php")
        }

        val deferred2 = async(UI) {
            println("hello2")
            deferred1.await()

            adapter = NewsAdapter(this@MainActivity, newsList!!)
            lv!!.setAdapter(adapter)
            lv!!.setOnItemClickListener(object : AdapterView.OnItemClickListener {
                override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                    val news = newsList!![position]
                    val intent = Intent(this@MainActivity, NewsDisplayActivity::class.java)
                    println("completeurl:${news.url}")
                    intent.putExtra("news_url", news.url)
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
