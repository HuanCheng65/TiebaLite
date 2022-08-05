package com.huanchengfly.tieba.post.models

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

data class ModifyNicknameResult(
    @SerializedName("isclose")
    val isClose: Int,
    val nickname: String?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(isClose)
        parcel.writeString(nickname)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ModifyNicknameResult> {
        override fun createFromParcel(parcel: Parcel): ModifyNicknameResult {
            return ModifyNicknameResult(parcel)
        }

        override fun newArray(size: Int): Array<ModifyNicknameResult?> {
            return arrayOfNulls(size)
        }
    }

}
