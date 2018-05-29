package com.lm.ll.spark.db

import android.os.Parcel
import android.os.Parcelable


/**
 * Created by ll on 2018-05-24 17:23.
 */
data class News(
        var id: Int = 0, //id
        var title: String? = null, //标题
        var url: String? = null, //url链接
        var author: String? = null, //作者
        var date: String? = null, //日期
        var readCount: String? = null, //阅读数
        var text: String? = null //文章正文
):Parcelable {

    constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString())

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(id)
        dest.writeString(title)
        dest.writeString(url)
        dest.writeString(author)
        dest.writeString(date)
        dest.writeString(readCount)
        dest.writeString(text)
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

