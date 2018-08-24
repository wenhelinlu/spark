package com.lm.ll.spark.activity

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import com.afollestad.materialdialogs.MaterialDialog
import com.lm.ll.spark.R
import com.lm.ll.spark.adapter.ProfileInfoAdapter
import com.lm.ll.spark.db.ProfileInfo
import com.lm.ll.spark.decoration.SolidLineItemDecoration
import com.lm.ll.spark.util.PROFILE_INFO_KEY
import com.lm.ll.spark.util.toast
import kotlinx.android.synthetic.main.activity_person_profile.*
import kotlinx.android.synthetic.main.content_main.*
import org.jsoup.Jsoup

class PersonProfileActivity : AppCompatActivity() {
    /**
     * @desc 文章列表数据源
     * @author ll
     * @time 2018-08-14 9:53
     */
    private var profileInfoList: ArrayList<ProfileInfo> = ArrayList()

    /**
     * @desc RecyclerView的adapter
     * @author ll
     * @time 2018-08-14 9:53
     */
    private lateinit var mAdapter: ProfileInfoAdapter

    /**
     * @desc 登录成功后的个人档案网页文本
     * @author LL
     * @time 2018-08-24 10:18
     */
    private lateinit var profileInfoText: String

    /**
     * @desc RecyclerView的LayoutManager
     * @author ll
     * @time 2018-08-14 9:52
     */
    private val mLinearLayoutManager = LinearLayoutManager(this@PersonProfileActivity)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_person_profile)

        if (intent.hasExtra(PROFILE_INFO_KEY)) {
            profileInfoText = intent.getStringExtra(PROFILE_INFO_KEY)
        }


        val layoutParams = window.attributes
        layoutParams.flags = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS or layoutParams.flags

        setSupportActionBar(toolbar)
        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }

        initView()
        loadData()
    }

    /**
     * @desc 初始化视图
     * @author LL
     * @time 2018-08-24 10:14
     */
    private fun initView() {
        //RecyclerView设置
        this.recyclerViewTitles.addItemDecoration(SolidLineItemDecoration(this@PersonProfileActivity))
        this.recyclerViewTitles.layoutManager = mLinearLayoutManager
        mAdapter = ProfileInfoAdapter(this@PersonProfileActivity, profileInfoList)
        this.recyclerViewTitles.adapter = mAdapter
    }

    /**
     * @desc 加载数据
     * @author LL
     * @time 2018-08-24 10:16
     */
    private fun loadData() {
        val doc = Jsoup.parse(profileInfoText)
        val elements = doc.getElementsByClass("td_first")

        var info = ProfileInfo("笔名", "markherd")
        profileInfoList.add(info)
        info = ProfileInfo("性别", "男")
        profileInfoList.add(info)
        info = ProfileInfo("地区", "")
        profileInfoList.add(info)
        info = ProfileInfo("注册时间", "14-12-28")
        profileInfoList.add(info)
        info = ProfileInfo("上次登录时间", "18-08-20")
        profileInfoList.add(info)
        info = ProfileInfo("上次登录IP", "45.78.*.* (美国)")
        profileInfoList.add(info)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_person_profile,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when(item!!.itemId){
            R.id.action_logout -> {
                MaterialDialog(this).show {
                    title(text = "确认注销")
                    message(text = "确定要注销登录吗？")
                    positiveButton(text = "注销") {
                        toast("注销成功！")
                    }
                    negativeButton(text = "取消")
                }
                return true
            }else -> super.onOptionsItemSelected(item)
        }
    }
}
