package com.lm.ll.spark.fragment

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.snackbar.Snackbar
import com.lm.ll.spark.R
import com.lm.ll.spark.adapter.ArticleListAdapter
import com.lm.ll.spark.db.Article
import com.lm.ll.spark.decoration.SolidLineItemDecoration
import com.lm.ll.spark.listener.MyRecyclerViewOnScrollListener
import com.lm.ll.spark.net.Spider
import com.lm.ll.spark.util.GlobalConst
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VideoFragment : Fragment() {
    /**
     * @desc 论坛列表数据源
     * @author ll
     * @time 2018-08-14 9:53
     */
    private var videoList: ArrayList<Article> = ArrayList()

    /**
     * @desc RecyclerView的adapter
     * @author ll
     * @time 2018-08-14 9:53
     */
    private lateinit var mAdapter: ArticleListAdapter

    /**
     * @desc RecyclerView的LayoutManager
     * @author ll
     * @time 2018-08-14 9:52
     */
    private lateinit var linearLayoutManager: LinearLayoutManager

    /**
     * @desc 当前加载的页数
     * @author ll
     * @time 2018-08-14 9:53
     */
    private var currentPage: Int = 1

    /**
     * @desc 子论坛基地址
     * @author lm
     * @time 2018-10-06 11:47
     */
    private var baseUri = "https://mv.6park.com/index.php"
    
    /**
     * @desc 列表控件
     * @author ll
     * @time 2018-10-10 16:51
     */
    private lateinit var mRecyclerView: RecyclerView

    /**
     * @desc 下拉刷新控件
     * @author ll
     * @time 2018-10-10 16:51
     */
    private lateinit var mSwipeRefreshLayout: SwipeRefreshLayout

    private var mActivity: AppCompatActivity? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Activity) {
            mActivity = context as AppCompatActivity
        }
    }

    override fun onDetach() {
        super.onDetach()
        mActivity = null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.activity_article_list, container, false)

        //SwipeRefreshLayout设置
        mSwipeRefreshLayout = view.findViewById(R.id.swipeRefreshTitles)
        //下拉刷新进度条颜色
        mSwipeRefreshLayout.setColorSchemeResources(R.color.md_teal_500, R.color.md_orange_500, R.color.md_light_blue_500)
        //触发刷新的下拉距离
        mSwipeRefreshLayout.setDistanceToTriggerSync(GlobalConst.PULL_REFRESH_DISTANCE)
        //下拉刷新监听
        mSwipeRefreshLayout.setOnRefreshListener {
            loadData(::getArticleList)
        }
        //RecyclerView设置
        mRecyclerView = view.findViewById(R.id.recyclerViewTitles)
        mRecyclerView.addItemDecoration(SolidLineItemDecoration(mActivity!!))
        linearLayoutManager = LinearLayoutManager(mActivity!!)
        mRecyclerView.layoutManager = linearLayoutManager
        mAdapter = ArticleListAdapter(mActivity!!, videoList)
        mRecyclerView.adapter = mAdapter

        //上拉加载更多
        mRecyclerView.addOnScrollListener(object : MyRecyclerViewOnScrollListener(linearLayoutManager) {
            override fun loadMoreData() {
                currentPage++
                loadData(::getArticleList, true)

            }
        })
        loadData(::getArticleList)

        return view
    }


    /**
     * @desc 加载数据
     * @author ll
     * @time 2018-08-13 20:59
     * @param download 函数类型参数，实际的下载方法
     * @param isLoadMore 是否是加载更多数据
     */
    private fun loadData(download: (page: Int) -> ArrayList<Article>, isLoadMore: Boolean = false) {
        val currentPos: Int = videoList.size

        GlobalScope.launch(Dispatchers.Main) {
            showProgress(true)
            withContext(Dispatchers.IO) {
                //如果下拉刷新，则只抓取第一页内容，否则加载下一页内容
                var pageIndex = if (isLoadMore) currentPage else 1
                val list = download(pageIndex)

                //Log.d(LOG_TAG_COMMON, "isLoadMore = $isLoadMore, pageIndex = $pageIndex, list'size = ${list.size}")

                if (isLoadMore) {
                    videoList.addAll(list) //如果是上拉加载更多，则直接将新获取的数据源添加到已有集合中
                } else {
                    /**
                     *  如果不是第一次加载，即当前已存在数据，则在新获取的列表中找出和当前已存在的数据列表第一条数据相同
                     *  的数据位置（如果没有找到，则说明新获取的数据列表数据都为新数据，可直接添加当已有集合中），然后将新获取数据列表中
                     *  这个位置之前的数据添加到已有集合中
                     */
                    if (videoList.count() > 0) {
                        val firstNews = list.findLast { x -> x.url == videoList[0].url }
                        if (firstNews != null) {
                            val firstIndex = list.indexOf(firstNews)
                            if (firstIndex > 0) {
                                val latest = list.take(firstIndex)
                                videoList.addAll(latest)
                            } else {
                            }
                        } else {
                        }
                    } else {
                        videoList.clear()
                        videoList.addAll(list)
                        //如果此时获取的集合数据不超过预定值，则继续加载数据
                        while (videoList.size < GlobalConst.LIST_MIN_COUNT) {
                            pageIndex = ++currentPage
                            val tmpList = download(pageIndex)
                            videoList.addAll(tmpList)
                        }
                    }
                }
            }
            refreshData()

            //上拉加载后，默认将新获取的数据源的上一行显示在最上面位置
            if (isLoadMore) {
                linearLayoutManager.scrollToPositionWithOffset(currentPos - 1, 0)
            }

            showProgress(false)
        }
    }

    /**
     * @desc 根据页码获取文章列表，注意：11页之前（不包含第11页）的url和第11页及之后的url不同
     * @author lm
     * @time 2018-07-28 15:50
     * @param pageIndex 页码
     */
    private fun getArticleList(pageIndex: Int): ArrayList<Article> {

        return try {
            //11页之前（不包含第11页）的url和第11页及之后的url不同
            val url = if (pageIndex <= 10) {
                "$baseUri${GlobalConst.CURRENT_BASE_URL}$pageIndex"
            } else {
                "$baseUri?app=forum&act=list&pre=55764&nowpage=$pageIndex&start=55764"
            }
//        Log.d(LOG_TAG_COMMON, url)
            Spider.scratchOnlineVideoList(url)
        } catch (ex: Exception) {
            //toast(ex.message!!)
            Snackbar.make(mSwipeRefreshLayout, ex.message!!, Snackbar.LENGTH_LONG)
                    .setAction("确定") { }.show()
            ArrayList()
        }
    }

    /**
     * @desc 刷新数据
     * @author ll
     * @time 2018-08-14 9:44
     */
    private fun refreshData() {
        /**
         * 注意，如果此adapter绑定的数据源articleList重新赋值了，则表示此数据源在内存中的地址改变，adapter会认为原数据源没有改变，
         * 此时调用notifyDataSetChanged()方法不起作用，必须重新绑定数据源才可以。
         * 解决方法是不直接给articleList赋新值，而是调用articleList的addAll()方法（视情况而定，可以先clear），这样adapter的
         * notifyDataSetChanged()方法就会起作用，列表可以正常刷新
         */
        mRecyclerView.adapter!!.notifyDataSetChanged()
    }


    /**
     * @desc 显示进度条
     * @author ll
     * @time 2018-07-10 17:48
     */
    private fun showProgress(show: Boolean) {
        mSwipeRefreshLayout.isRefreshing = show
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.

         * @return A new instance of fragment NewsFragment.
         */
        @JvmStatic
        fun newInstance() = VideoFragment()
    }
}
