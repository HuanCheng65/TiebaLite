package com.huanchengfly.tieba.post.models

import android.os.Parcel
import android.os.Parcelable

data class PhotoViewBean @JvmOverloads constructor(
    var url: String?,
    var originUrl: String?,
    var isLongPic: Boolean = false,
    var index: String? = null,
    var isGif: Boolean = false
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readByte() != 0.toByte(),
        parcel.readString(),
        parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(url)
        parcel.writeString(originUrl)
        parcel.writeByte(if (isLongPic) 1 else 0)
        parcel.writeString(index)
        parcel.writeByte(if (isGif) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PhotoViewBean> {
        override fun createFromParcel(parcel: Parcel): PhotoViewBean {
            return PhotoViewBean(parcel)
        }

        override fun newArray(size: Int): Array<PhotoViewBean?> {
            return arrayOfNulls(size)
        }
    }
}