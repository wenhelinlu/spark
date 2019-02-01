package com.lm.ll.spark.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
 * @desc  实现了Kotlin1.3之后的新协程接口CoroutineScope，这样在启动协程时不需要使用GlobalScope，避免资源过度消耗
 * @author Administrator
 * @time 2019-02-01 11:49
 */

abstract class CoroutineScopeActivity : AppCompatActivity(), CoroutineScope {
    lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        job = SupervisorJob()
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineContext.cancelChildren()
    }
}