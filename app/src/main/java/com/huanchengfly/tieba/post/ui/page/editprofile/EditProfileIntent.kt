package com.huanchengfly.tieba.post.ui.page.editprofile

import com.huanchengfly.tieba.post.arch.UiIntent
import com.huanchengfly.tieba.post.models.ModifyNicknameResult
import java.io.File

sealed interface EditProfileIntent : UiIntent {
    data class Init(val userId: String) : EditProfileIntent

    data class Submit(
        val sex: Int,
        val birthdayTime: Long,
        val birthdayShowStatus: Boolean,
        val intro: String,
    ) : EditProfileIntent

    data object SubmitWithoutChange : EditProfileIntent

    data object ModifyNickname : EditProfileIntent

    data class ModifyNicknameFinish(val result: ModifyNicknameResult) : EditProfileIntent

    data class UploadPortrait(val file: File) : EditProfileIntent

    data object UploadPortraitStart : EditProfileIntent
}
