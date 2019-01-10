package com.lm.ll.spark.util

/**
 * @desc 全局常量
 * @author lm
 * @time 2018-09-22 9:51
 * @email: wenhelinlu@gmail.com
 * @version: 0.1
 */
class GlobalConst {
    companion object {
        //文章列表初次加载时最小行数
        const val LIST_MIN_COUNT = 25

        //文章列表上滑加载时一次加载最少行数
        const val LIST_MIN_UP_REFRESH = 15

        //禁忌书屋基地址
        const val BASE_URL = "https://www.cool18.com/bbs4/index.php"

        //当前列表数据源URL地址（未附加页数）(在11页之前是这个地址，不包括第11页)
        const val CURRENT_BASE_URL = "?app=forum&act=cachepage&cp=tree" //禁忌书屋

        //禁忌书屋精华区数据源URL地址（未附加页数）
        const val CURRENT_ELITEAREA_BASE_URL = "?app=forum&act=gold&p="

        //是否是情色经典书库中的文章
        const val IS_CLASSIC_ARTICLE = "classic"

        //下拉刷新操作触发距离
        const val PULL_REFRESH_DISTANCE = 400

        //抓取网页设置的Useragent，防止被服务器阻止
        const val USER_AGENT = "User-Agent,Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.87 Safari/537.36" //使用PC版User-Agent，如果使用移动版User-Agent，会导致正文解析错误

        //连接超时时长
        const val TIME_OUT = 50000

        //存储夜间模式设置的键
        const val NIGHT_MODE = "NIGHT_MODE"

        //常规Log的tag
        const val LOG_TAG_COMMON = "SPARK_LOG_COMMON"

        //okhttp3的log tag
        const val LOG_TAG_OKHTTP3 = "SPARK_LOG_OKHTTP3"

        //如果文本中段落标记（\r\n\r\n）个数大于此值，则按照规则清除换行标记，保留段落标记
        const val PARAGRAPH_FLAG_COUNT_LIMIT = 20

        //用于登录时用Intent传递个人档案页面文字的key
        const val PROFILE_INFO_KEY = "profile"

        //Intent中传递子论坛url的key
        const val SUB_FORUM_URL = "sub_forum_url"

        //Intent中传递子论坛标题的key
        const val SUB_FORUM_TITLE = "sub_forum_title"

        //已登录状态标记
        const val LOGINING_STATUS = "欢迎您"

        //自动夜间模式开始时间
        const val NIGHT_MODE_START_HOUR = 22

        //自动夜间模式结束时间
        const val NIGHT_MODE_END_HOUR = 6

        //文章正文中文本和图片之间分隔符
        const val TEXT_IMAGE_SPLITER = "@@@"

        //链接是否来源于正常列表，即不是文章中的列表的key
        const val FROM_NORMAL_LIST = "from_normal_list"
    }
}