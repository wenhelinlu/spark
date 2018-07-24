package com.lm.ll.spark.db

import android.os.Parcel
import android.os.Parcelable
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


/**
 * Created by ll on 2018-05-24 17:23.
 */
@Entity
data class Article(
        @Id var id: Long = 0, //objectbox内部主键
        var url: String? = null, //url链接
        var title: String? = null, //标题
        var author: String = "", //作者
        var date: String = "", //文章发表日期
        var textLength: String = "", //文章字数
        var readCount: String = "", //阅读数
        var text: String = "", //文章正文
        var insertTime: String = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), //文章收藏入库时间
        var favorite: Int = 0,  //是否被收藏, 1表示已收藏，0表示未收藏
        var articleFlag: Int = 0, //是否是Article，0表示Article，1表示Comment
        var classicalFlag: Int = 0, //是否是经典书库文章，0表示否，1表示是（解析正文方式不同）
        var leavePosition: Int = 0, //当前RecycelrView中第一个可见的item的位置
        var offset: Int = 0, //与该view的顶部的偏移量
        var depth: Int = 0, //列表层级，控制评论列表的缩进
        var comments: List<Comment> = listOf() //此文章的评论列表
) : Parcelable {

    constructor(parcel: Parcel) : this(
            parcel.readLong(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readInt(),
            parcel.readInt(),
            parcel.readInt(),
            parcel.readInt(),
            parcel.readInt(),
            parcel.readInt(),
            arrayListOf<Comment>().apply {
                parcel.readList(this, Comment::class.java.classLoader)
            }
    )

//
//        //实现RealmList的Parcelable处理
//        val mList = ArrayList<Comment>()
//        mList.addAll(parcel.createTypedArrayList(Comment.CREATOR))
//        comments = mList


    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeLong(id)
        dest.writeString(url)
        dest.writeString(title)
        dest.writeString(author)
        dest.writeString(date)
        dest.writeString(textLength)
        dest.writeString(readCount)
        dest.writeString(text)
        dest.writeString(insertTime)
        dest.writeInt(favorite)
        dest.writeInt(articleFlag)
        dest.writeInt(classicalFlag)
        dest.writeInt(leavePosition)
        dest.writeInt(offset)
        dest.writeInt(depth)
        dest.writeList(comments)
    }


    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Article> {
        override fun createFromParcel(parcel: Parcel): Article {
            return Article(parcel)
        }

        override fun newArray(size: Int): Array<Article?> {
            return arrayOfNulls(size)
        }
    }
}