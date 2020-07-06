# Spark

### 简介 ###

使用Kotlin语言编写的[留园网](https://www.6park.com)Android客户端。

---
### 主要功能 ###

+ 解析并加载论坛的部分文章，重新排版以适应移动设备屏幕布局
+ 自动进行繁简转换
+ 支持离线保存文章，在无网络状态下也可以阅读
+ 支持文章检索

---
### 系统要求 ###

Android 5.1 +


---
### TODO ###

+ ~~支持包含图片的文章的加载~~
+ 在线视频播放
+ 论坛登录
+ 发布文章
+ 发表评论

---
### 使用到的部分开源组件 ###

+ [Retrofit2](https://github.com/square/retrofit)
+ 繁简转换 [HanLP](https://github.com/hankcs/HanLP)
+ 使用Delegate设计模式的Recyclerview.Adapter框架[AdapterDelegates](https://github.com/sockeqwe/AdapterDelegates)
+ Google风格进度条控件 [SmoothProgressBar](https://github.com/castorflex/SmoothProgressBar)
+ Cookie持久化管理组件 [PersistentCookieJar](https://github.com/franmontiel/PersistentCookieJar)
+ 圆形ImageView控件 [CircleImageView](https://github.com/hdodenhof/CircleImageView)
+ material风格对话框控件 [material-dialogs](https://github.com/afollestad/material-dialogs)
