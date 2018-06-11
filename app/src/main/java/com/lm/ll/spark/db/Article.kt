package com.lm.ll.spark.db

import android.os.Parcel
import android.os.Parcelable
import com.lm.ll.spark.annotation.Poko
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


/**
 * Created by ll on 2018-05-24 17:23.
 */
@Poko
data class Article(
        @PrimaryKey var url: String? = null, //url链接
        var title: String? = null, //标题
        var author: String? = null, //作者
        var date: String? = null, //文章发表日期
        var textLength: String? = null, //文章字数
        var readCount: String? = null, //阅读数
        var text: String? = null, //文章正文
        var insertTime: String? = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), //文章收藏入库时间
        var isFavorited: Int = 0  //是否被收藏, 1表示已收藏，0表示未收藏
) : Parcelable, RealmObject() {

    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readInt())

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(url)
        dest.writeString(title)
        dest.writeString(author)
        dest.writeString(date)
        dest.writeString(textLength)
        dest.writeString(readCount)
        dest.writeString(text)
        dest.writeString(insertTime)
        dest.writeInt(isFavorited)
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

