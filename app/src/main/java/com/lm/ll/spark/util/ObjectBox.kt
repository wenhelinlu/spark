package com.lm.ll.spark.util

import android.content.Context
import com.lm.ll.spark.BuildConfig
import com.lm.ll.spark.db.*
import io.objectbox.Box
import io.objectbox.BoxStore
import io.objectbox.android.AndroidObjectBrowser
import io.objectbox.kotlin.boxFor


/**
 * BoxStore单例
 * 作者：Created by ll on 2018-07-24 16:59.
 * 邮箱：wenhelinlu@gmail.com
 */
object ObjectBox {
    //BoxStore实例
    lateinit var boxStore: BoxStore
        private set

    fun init(context: Context) {
        /**
         * @description
         *
         * 问题背景：
         * 因为之前都是直接调试安装apk，但是从方式安装的应用无法被覆盖安装，且钛备份也无法完整还原
         * 程序和数据（只能通过Android Studio调试安装新的apk后，再用钛备份恢复数据），MIUI自身的备份好像可以完整恢复调试安装的
         * 程序和数据，但是依然无法覆盖安装新版本，必须将原有应用卸载，所以考虑安装打包的release版本，但是如何将原有的数据转移到
         * release版本呢？直接复制ObjectBox的data.mdb文件放在新的应用目录下是启动失败的。
         *
         * 解决方式：
         * ObjectBox文件存储路径为/data/data/packagename/files/objectbox/objectbox/data.mdb，在Android Studio项目里
         * 的main目录下新建一个assets目录（ New->Folder-> Assets Folder），然后把data.mdb文件粘贴在assets目录里，并把
         * ObjectBox的初始化代码修改为下面注释的部分（主要是多了调用initialDbFile方法，打开assets里的data.db文件）。
         * 该方法在初始化的时候，会去读取该目录下的文件就行数据填充。但是要注意一点，数据模型实体必须和原来的一样，不然会有问题，运行直接崩溃。
         *
         * @date: 2021-01-29 13:47
         * @author: LuHui
         */
/*        boxStore = MyObjectBox.builder()
                .androidContext(context.applicationContext)
                .initialDbFile {
                    context.assets.open("data.mdb")
                }
                .build()*/
        boxStore = MyObjectBox.builder()
                .androidContext(context.applicationContext)
                .build()
        //如果是DEBUG模式，则app运行时在状态栏显示data browser提示，可以通过手机浏览器查看数据库数据
        if (BuildConfig.DEBUG) {
            AndroidObjectBrowser(ObjectBox.boxStore).start(context)
        }
    }

    fun build(context: Context) {
        boxStore = MyObjectBox.builder().androidContext(context.applicationContext).build()
    }

    /**
     * @desc Article的Box实例，用于ObjectBox数据库中Article表的相关操作
     * @author ll
     * @time 2018-07-26 15:45
     */
    fun getArticleBox():Box<Article>{
        return boxStore.boxFor()
    }

    /**
     * @description Comment的Box实例，用于ObjectBox数据库中Comment表的相关操作
     * @date: 2019-11-01 14:06
     * @author: LuHui
     * @param
     * @return
     */
    fun getCommentBox(): Box<Comment> {
        return boxStore.boxFor()
    }

    /**
     * @desc QueryRecord的Box实例，用于ObjectBox数据库中QueryRecord表的相关操作
     * @author ll
     * @time 2018-08-14 15:46
     */
    fun getQueryRecordBox():Box<QueryRecord>{
        return boxStore.boxFor()
    }

    /**
     * @desc SiteMap的Box实例，用于ObjectBox数据库中SiteMap表的相关操作
     * @author lm
     * @time 2018-09-09 21:02
     */
    fun getSubForumBox(): Box<SubForum> {
        return boxStore.boxFor()
    }

}