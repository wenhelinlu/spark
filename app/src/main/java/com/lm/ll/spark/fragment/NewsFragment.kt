package com.lm.ll.spark.fragment

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.lm.ll.spark.R
import com.lm.ll.spark.adapter.SubForumItemListAdapter
import com.lm.ll.spark.api.TabooBooksApiService
import com.lm.ll.spark.db.SubForum
import com.lm.ll.spark.decoration.SolidLineItemDecoration
import com.lm.ll.spark.repository.TabooArticlesRepository
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider
import com.uber.autodispose.kotlin.autoDisposable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_data_list.*
import retrofit2.HttpException
import java.net.ConnectException
import java.util.concurrent.TimeoutException
import javax.net.ssl.SSLHandshakeException

class NewsFragment : Fragment() {
    /**
     * @desc 论坛列表数据源
     * @author ll
     * @time 2018-08-14 9:53
     */
    private var subForumList: ArrayList<SubForum> = ArrayList()

    /**
     * @desc RecyclerView的adapter
     * @author ll
     * @time 2018-08-14 9:53
     */
    private lateinit var mAdapter: SubForumItemListAdapter

    /**
     * @desc RecyclerView的LayoutManager
     * @author ll
     * @time 2018-08-14 9:52
     */
    private lateinit var linearLayoutManager: LinearLayoutManager

    private lateinit var mRecyclerView: RecyclerView

    //使用AutoDispose解除RxJava2订阅
    private val scopeProvider by lazy { AndroidLifecycleScopeProvider.from(this) }

    private var mActivity: AppCompatActivity? = null

    override fun onAttach(context: Context?) {
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
        val view = inflater.inflate(R.layout.fragment_data_list, container, false)
        //RecyclerView设置
        mRecyclerView = view.findViewById(R.id.dataRecyclerView)
        mRecyclerView.addItemDecoration(SolidLineItemDecoration(mActivity!!))
        linearLayoutManager = LinearLayoutManager(mActivity!!)
        mRecyclerView.layoutManager = linearLayoutManager
        mAdapter = SubForumItemListAdapter(mActivity!!, subForumList)
        mRecyclerView.adapter = mAdapter

        loadData()

        return view
    }


    /**
     * @desc 加载数据
     * @author ll
     * @time 2018-07-10 17:23
     */
    private fun loadData() {
        loadTextWithRx()
    }

    /**
     * @desc 使用RxJava+Retrofit实现异步读取数据
     * @author lm
     * @time 2018-07-01 17:21
     */
    private fun loadTextWithRx() {
        val repository = TabooArticlesRepository(TabooBooksApiService.create())
        repository.getSubForumList()
                .firstElement()
                .subscribeOn(Schedulers.io())
                .doOnSubscribe {
                    showProgress(true)
                }
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doAfterTerminate {
                    showProgress(false)
                }
                .doOnDispose { Log.i("AutoDispose", "Disposing subscription from onCreate()") }
                .autoDisposable(scopeProvider) //使用AutoDispose解除RxJava2订阅
                .subscribe({ result ->
                    subForumList.clear()
                    subForumList.addAll(result)
                    refreshData()
                }, { error ->
                    //异常处理
                    val msg =
                            when (error) {
                                is HttpException, is SSLHandshakeException, is ConnectException -> "网络连接异常"
                                is TimeoutException -> "网络连接超时"
                                is IndexOutOfBoundsException, is ClassCastException -> "解析异常"
                                else -> error.toString()
                            }
                    Snackbar.make(dataListLayout, msg, Snackbar.LENGTH_LONG)
                            .setAction("重试") { loadData() }.show()
                })
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
        this.pb_loadData.visibility = if (show) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.

         * @return A new instance of fragment NewsFragment.
         */
        @JvmStatic
        fun newInstance() = NewsFragment()
    }
}
