package com.lm.ll.spark

import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.lm.ll.spark.adapter.NewsAdapter
import com.lm.ll.spark.db.News
import com.lm.ll.spark.decoration.NewsItemDecoration
import com.lm.ll.spark.util.Spider
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async


class MainActivity : AppCompatActivity(), SwipeRefreshLayout.OnRefreshListener {
    override fun onRefresh() {
        this.swipeRefresh.isRefreshing = false
    }

    private var newsList:ArrayList<News> = ArrayList()
    private var adapter: NewsAdapter? = null

//    private val URL: String = "https://www.cool18.com/bbs4/index.php?app=forum&act=cachepage&cp=tree" //禁忌书屋
    private val URL: String = "https://site.6parker.com/chan1/index.php?app=forum&act=cachepage&cp=tree" //史海钩沉

    private var currentPage: Int = 1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
//            var layoutParam = window.attributes
//            layoutParam.flags = (WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS or  layoutParam.flags)
//        }

        val toolbar:Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationIcon(R.drawable.ic_menu)
        toolbar.setNavigationOnClickListener {
            Toast.makeText(this@MainActivity,"Hello",Toast.LENGTH_SHORT).show()
        }

        swipeRefresh.setColorSchemeResources(R.color.colorAccent)
        swipeRefresh.setDistanceToTriggerSync(300)

        swipeRefresh.setOnRefreshListener({
            loadContent()
        })

        this.recyclerView.addItemDecoration(NewsItemDecoration(2))
        this.recyclerView.layoutManager =LinearLayoutManager(this@MainActivity)

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
            var list = spider.scratchContent("$URL$currentPage")
            if (newsList != null && newsList.count() > 0) {
                newsList.addAll(list)
            } else {
                newsList = list
            }
        }

        async(UI) {
            deferred1.await()
            adapter = NewsAdapter(this@MainActivity, newsList)
            this@MainActivity.recyclerView.adapter = adapter
            this@MainActivity.recyclerView.adapter.notifyDataSetChanged()
            //停止刷新
            swipeRefresh.isRefreshing = false
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
            R.id.action_search -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}
