package com.lm.ll.spark.db

import android.os.Parcel
import android.os.Parcelable
import com.lm.ll.spark.annotation.Poko
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey


/**
 * Created by ll on 2018-05-24 17:23.
 */
@Poko
data class News(
        @PrimaryKey var id: String? = null, //id
        var title: String? = null, //标题
        var url: String? = null, //url链接
        var author: String? = null, //作者
        var date: String? = null, //日期
        var textLength: String? = null, //文章字数
        var readCount: String? = null, //阅读数
        var text: String? = null, //文章正文
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
        dest.writeString(id)
        dest.writeString(title)
        dest.writeString(url)
        dest.writeString(author)
        dest.writeString(date)
        dest.writeString(textLength)
        dest.writeString(readCount)
        dest.writeString(text)
        dest.writeInt(isFavorited)
    }


    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<News> {
        override fun createFromParcel(parcel: Parcel): News {
            return News(parcel)
        }

        override fun newArray(size: Int): Array<News?> {
            return arrayOfNulls(size)
        }
    }
}

