package com.lm.ll.spark.db

import android.os.Parcel
import android.os.Parcelable
import com.lm.ll.spark.annotation.Poko
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


/**
 * Desc: 文章评论
 * 作者：Created by ll on 2018-06-22 10:42.
 * 邮箱：wenhelinlu@gmail.com
 */
@Poko
data class Comment(
        @PrimaryKey var url: String? = null, //url链接
        var title: String? = null, //标题
        var author: String? = null, //作者
        var date: String? = null, //文章发表日期
        var textLength: String? = null, //文章字数
        var readCount: String? = null, //阅读数
        var text: String? = null, //文章正文
        var depth: Int = 0, //评论深度（用于缩进显示）
        var insertTime: String? = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) //文章收藏入库时间
) : Parcelable, RealmObject() {

    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readInt(),
            parcel.readString())

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(url)
        dest.writeString(title)
        dest.writeString(author)
        dest.writeString(date)
        dest.writeString(textLength)
        dest.writeString(readCount)
        dest.writeString(text)
        dest.writeInt(depth)
        dest.writeString(insertTime)
    }


    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Comment> {
        override fun createFromParcel(parcel: Parcel): Comment {
            return Comment(parcel)
        }

        override fun newArray(size: Int): Array<Comment?> {
            return arrayOfNulls(size)
        }
    }

    /**
     * @desc 将Comment转换成Article，在打开评论链接时使用
     * @author ll
     * @time 2018-06-22 12:31
     */
    fun toArticle(): Article{
        val article = Article()
        article.url = this.url
        article.author = this.author
        article.insertTime = this.insertTime
        article.text = this.text
        article.textLength = this.textLength
        article.readCount = this.readCount
        article.date = this.date

        return article
    }
}