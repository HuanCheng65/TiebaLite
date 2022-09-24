package com.huanchengfly.tieba.post.ui.page.editprofile

import com.huanchengfly.tieba.post.arch.UiState

data class EditProfileState(
    val portrait: String = "",
    val name: String = "",
    val nickname: String = "",
    val sex: Int = 0,
    val birthdayShowStatus: Boolean = false,
    val birthdayTime: Long = 0L,
    val tbAge: String = "0",
    val intro: String? = null,
    val isLoading: Boolean = true,
    val isSubmitting: Boolean = false
) : UiState
