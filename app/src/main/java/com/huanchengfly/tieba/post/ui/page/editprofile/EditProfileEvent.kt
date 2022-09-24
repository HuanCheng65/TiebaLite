package com.huanchengfly.tieba.post.ui.page.editprofile

import com.huanchengfly.tieba.post.arch.UiEvent
import com.huanchengfly.tieba.post.models.ModifyNicknameResult

sealed interface EditProfileEvent : UiEvent {
    sealed interface Init : EditProfileEvent {
        data class Fail(val toast: String) : Init
    }

    sealed interface Submit : EditProfileEvent {
        data class Result(
            val success: Boolean,
            val changed: Boolean = success,
            val message: String = ""
        ) : Submit
    }

    sealed interface ModifyNickname : EditProfileEvent {
        object Start : ModifyNickname

        data class Finish(val result: ModifyNicknameResult) : ModifyNickname
    }

    sealed interface UploadPortrait : EditProfileEvent {
        object Pick : UploadPortrait
        data class Success(val message: String) : UploadPortrait
        data class Fail(val error: String) : UploadPortrait
    }
}