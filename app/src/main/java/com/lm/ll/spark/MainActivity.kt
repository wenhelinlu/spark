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
import com.lm.ll.spark.util.MyRecyclerViewOnScrollListener
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

    private val URL: String = "https://www.cool18.com/bbs4/index.php?app=forum&act=cachepage&cp=tree" //禁忌书屋
//    private val URL: String = "https://site.6parker.com/chan1/index.php?app=forum&act=cachepage&cp=tree" //史海钩沉

    private var currentPage: Int = 1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar:Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationIcon(R.drawable.ic_menu)
        toolbar.setNavigationOnClickListener {
            Toast.makeText(this@MainActivity,"Hello",Toast.LENGTH_SHORT).show()
        }

        swipeRefresh.setColorSchemeResources(R.color.colorAccent, R.color.colorPrimaryDark)
        swipeRefresh.setDistanceToTriggerSync(300)

        swipeRefresh.setOnRefreshListener({
            loadContent(true)
        })


        val linearLayoutManager = LinearLayoutManager(this@MainActivity)

        this.recyclerView.addItemDecoration(NewsItemDecoration(2))
        this.recyclerView.layoutManager = linearLayoutManager

        //上拉加载更多
        recyclerView.addOnScrollListener(object : MyRecyclerViewOnScrollListener(linearLayoutManager) {
            override fun loadMoreData() {
                currentPage++
                loadContent()
            }
        })

        loadContent()
    }

    /**
     * @desc 加载文章列表
     * @author ll
     * @time 2018-05-29 19:40
     */
    private fun loadContent(isRefresh: Boolean = false) {

        val currentPos: Int = newsList.size

        val deferred1 = async(CommonPool) {
            val spider = Spider()
            //如果下拉刷新，则只抓取第一页内容，否则加载下一页内容
            val pageIndex = if (isRefresh) 1 else currentPage
            var list = spider.scratchContent("$URL$pageIndex")

            /**
             *  如果不是第一次加载，即当前已存在数据，则在新获取的列表中找出和当前已存在的数据列表第一条数据相同
             *  的数据位置（如果没有找到，则说明新获取的数据列表数据都为新数据，可直接添加当已有集合中），然后将新获取数据列表中
             *  这个位置之前的数据添加到已有集合中             *
             */
            if (isRefresh) {
                if (newsList.count() > 0) {
                    var firstNews = list.findLast { x -> x.url == newsList[0].url }
                    if (firstNews != null) {
                        var firstIndex = list.indexOf(firstNews)
                        if (firstIndex > 0) {
                            var latest = list.take(firstIndex)

                            newsList.addAll(latest)
                        } else {
                        }
                    } else {
                    }
                } else {
                    newsList = list
                }
            } else {
                newsList.addAll(list) //如果是上拉加载更多，则直接将新获取的数据源添加到已有集合中
            }
        }

        async(UI) {
            swipeRefresh.isRefreshing = true
            deferred1.await()
            adapter = NewsAdapter(this@MainActivity, newsList)
            this@MainActivity.recyclerView.adapter = adapter
            this@MainActivity.recyclerView.adapter.notifyDataSetChanged()

            if (isRefresh) {
                this@MainActivity.recyclerView.layoutManager.scrollToPosition(currentPos - 1)
            }

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
