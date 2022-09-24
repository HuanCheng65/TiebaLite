package com.huanchengfly.tieba.post.ui.page.editprofile

import com.huanchengfly.tieba.post.arch.PartialChange
import com.huanchengfly.tieba.post.models.ModifyNicknameResult
import com.huanchengfly.tieba.post.models.database.Account

sealed class EditProfilePartialChange : PartialChange<EditProfileState> {
    sealed class UploadPortrait : EditProfilePartialChange() {
        override fun reduce(oldState: EditProfileState): EditProfileState =
            when (this) {
                Uploading -> oldState
                Start -> oldState
                is Success -> oldState
                is Fail -> oldState
            }

        object Uploading : UploadPortrait()
        object Start : UploadPortrait()
        data class Success(val message: String) : UploadPortrait()
        data class Fail(val error: String) : UploadPortrait()

    }

    sealed class ModifyNickname : EditProfilePartialChange() {
        override fun reduce(oldState: EditProfileState): EditProfileState =
            when (this) {
                Start -> oldState.copy()
                is Finish -> {
                    if (result.nickname != null) {
                        oldState.copy(nickname = result.nickname)
                    } else {
                        oldState.copy()
                    }
                }
            }

        object Start : ModifyNickname()
        data class Finish(val result: ModifyNicknameResult) : ModifyNickname()
    }

    sealed class Init : EditProfilePartialChange() {
        override fun reduce(oldState: EditProfileState): EditProfileState =
            when (this) {
                is Loading -> oldState.copy(isLoading = true)
                is Success -> oldState.copy(
                    isLoading = false,
                    portrait = account.portrait,
                    name = account.name,
                    nickname = account.nameShow ?: account.name,
                    sex = account.sex?.toInt() ?: 0,
                    birthdayTime = account.birthdayTime?.toLong()?.times(1000L) ?: 0L,
                    tbAge = account.tbAge ?: "0",
                    intro = account.intro ?: ""
                )
                is Fail -> oldState.copy(isLoading = false)
            }

        object Loading : Init()
        data class Success(val account: Account) : Init()
        data class Fail(val error: String) : Init()
    }

    sealed class Submit : EditProfilePartialChange() {
        override fun reduce(oldState: EditProfileState): EditProfileState =
            when (this) {
                is Submitting -> oldState.copy(
                    isSubmitting = true,
                    sex = sex,
                    birthdayTime = birthdayTime,
                    birthdayShowStatus = birthdayShowStatus,
                    intro = intro
                )
                Success -> oldState.copy(isSubmitting = false)
                SuccessWithoutChange -> oldState.copy(isSubmitting = false)
                is Fail -> oldState.copy(isSubmitting = false)
            }

        data class Submitting(
            val sex: Int,
            val birthdayTime: Long,
            val birthdayShowStatus: Boolean,
            val intro: String
        ) : Submit()

        object Success : Submit()
        object SuccessWithoutChange : Submit()
        data class Fail(val error: String) : Submit()
    }
}