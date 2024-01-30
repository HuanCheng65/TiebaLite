package com.huanchengfly.tieba.post.ui.page.user.edit

import androidx.compose.runtime.Stable
import com.huanchengfly.tieba.post.api.TiebaApi
import com.huanchengfly.tieba.post.api.interfaces.ITiebaApi
import com.huanchengfly.tieba.post.api.models.protos.profile.ProfileResponse
import com.huanchengfly.tieba.post.api.retrofit.exception.getErrorCode
import com.huanchengfly.tieba.post.api.retrofit.exception.getErrorMessage
import com.huanchengfly.tieba.post.arch.BaseViewModel
import com.huanchengfly.tieba.post.arch.PartialChange
import com.huanchengfly.tieba.post.arch.PartialChangeProducer
import com.huanchengfly.tieba.post.arch.UiEvent
import com.huanchengfly.tieba.post.arch.UiIntent
import com.huanchengfly.tieba.post.arch.UiState
import com.huanchengfly.tieba.post.models.database.Account
import com.huanchengfly.tieba.post.utils.AccountUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onStart
import java.io.File
import javax.inject.Inject

@Stable
@HiltViewModel
class EditProfileViewModel @Inject constructor() :
    BaseViewModel<EditProfileIntent, EditProfilePartialChange, EditProfileState, EditProfileEvent>() {
    override fun createInitialState(): EditProfileState = EditProfileState()

    override fun createPartialChangeProducer(): PartialChangeProducer<EditProfileIntent, EditProfilePartialChange, EditProfileState> =
        EditProfilePartialChangeProducer(TiebaApi.getInstance())

    class EditProfilePartialChangeProducer(
        private val tiebaApi: ITiebaApi
    ) : PartialChangeProducer<EditProfileIntent, EditProfilePartialChange, EditProfileState> {
        @OptIn(ExperimentalCoroutinesApi::class)
        override fun toPartialChangeFlow(intentFlow: Flow<EditProfileIntent>): Flow<EditProfilePartialChange> =
            merge(
                intentFlow.filterIsInstance<EditProfileIntent.Init>()
                    .flatMapConcat { it.toPartialChangeFlow() },
                intentFlow.filterIsInstance<EditProfileIntent.Submit>()
                    .flatMapConcat { it.toPartialChangeFlow() },
                intentFlow.filterIsInstance<EditProfileIntent.SubmitWithoutChange>()
                    .flatMapConcat { it.toPartialChangeFlow() },
                intentFlow.filterIsInstance<EditProfileIntent.UploadPortrait>()
                    .flatMapConcat { it.toPartialChangeFlow() },
                intentFlow.filterIsInstance<EditProfileIntent.UploadPortraitStart>()
                    .flatMapConcat { it.toPartialChangeFlow() }
            )

        private fun EditProfileIntent.Init.toPartialChangeFlow(): Flow<EditProfilePartialChange.Init> {
            val account = AccountUtil.currentAccount
            return if (account == null) {
                flowOf<EditProfilePartialChange.Init>(EditProfilePartialChange.Init.Fail("not logged in!"))
            } else {
                TiebaApi.getInstance()
                    .userProfileFlow(account.uid.toLong())
                    .map<ProfileResponse, EditProfilePartialChange.Init> { profile ->
                        val user = checkNotNull(profile.data_?.user)
                        account.apply {
                            nameShow = user.nameShow
                            portrait = user.portrait
                            intro = user.intro
                            sex = user.sex.toString()
                            fansNum = user.fans_num.toString()
                            postNum = user.post_num.toString()
                            threadNum = user.thread_num.toString()
                            concernNum = user.concern_num.toString()
                            tbAge = user.tb_age
                            age = user.birthday_info?.age?.toString()
                            birthdayShowStatus =
                                user.birthday_info?.birthday_show_status?.toString()
                            birthdayTime = user.birthday_info?.birthday_time?.toString()
                            constellation = user.birthday_info?.constellation
                            tiebaUid = user.tieba_uid
                            loadSuccess = true
                            updateAll("uid = ?", uid)
                        }
                        EditProfilePartialChange.Init.Success(account = account)
                    }
                    .onStart { emit(EditProfilePartialChange.Init.Loading) }
                    .catch { emit(EditProfilePartialChange.Init.Fail(it.getErrorMessage())) }
            }
        }

        private fun EditProfileIntent.Submit.toPartialChangeFlow() =
            tiebaApi.profileModifyFlow(
                birthdayShowStatus,
                "${birthdayTime / 1000L}",
                intro,
                "$sex",
                nickName
            )
                .map {
                    if (it.errorCode == 0) EditProfilePartialChange.Submit.Success else EditProfilePartialChange.Submit.Fail(
                        it.errorMsg
                    )
                }
                .onStart {
                    emit(
                        EditProfilePartialChange.Submit.Submitting(
                            sex,
                            birthdayTime,
                            birthdayShowStatus,
                            intro,
                            nickName
                        )
                    )
                }
                .catch { emit(EditProfilePartialChange.Submit.Fail(it.getErrorMessage())) }

        private fun EditProfileIntent.SubmitWithoutChange.toPartialChangeFlow() =
            flow { emit(EditProfilePartialChange.Submit.SuccessWithoutChange) }

        private fun EditProfileIntent.UploadPortraitStart.toPartialChangeFlow() =
            flow { emit(EditProfilePartialChange.UploadPortrait.Start) }

        private fun EditProfileIntent.UploadPortrait.toPartialChangeFlow(): Flow<EditProfilePartialChange.UploadPortrait> =
            tiebaApi.imgPortrait(file)
                .map {
                    if (it.errorCode == 0 || it.errorCode == 300003)
                        EditProfilePartialChange.UploadPortrait.Success(it.errorMsg)
                    else
                        EditProfilePartialChange.UploadPortrait.Fail(it.errorMsg)
                }
                .onStart { EditProfilePartialChange.UploadPortrait.Uploading }
                .catch {
                    if (it.getErrorCode() == 300003) {
                        emit(EditProfilePartialChange.UploadPortrait.Success(it.getErrorMessage()))
                    } else {
                        emit(EditProfilePartialChange.UploadPortrait.Fail(it.getErrorMessage()))
                    }
                }
    }

    override fun dispatchEvent(partialChange: EditProfilePartialChange): UiEvent? {
        return when (partialChange) {
            is EditProfilePartialChange.Init.Fail -> EditProfileEvent.Init.Fail(partialChange.error)
            is EditProfilePartialChange.Submit.Fail -> EditProfileEvent.Submit.Result(
                false,
                message = partialChange.error
            )

            EditProfilePartialChange.Submit.Success -> EditProfileEvent.Submit.Result(
                true,
                changed = false
            )

            EditProfilePartialChange.Submit.SuccessWithoutChange -> EditProfileEvent.Submit.Result(
                true
            )

            EditProfilePartialChange.UploadPortrait.Start -> EditProfileEvent.UploadPortrait.Pick
            is EditProfilePartialChange.UploadPortrait.Success -> EditProfileEvent.UploadPortrait.Success(
                partialChange.message
            )

            is EditProfilePartialChange.UploadPortrait.Fail -> EditProfileEvent.UploadPortrait.Fail(
                partialChange.error
            )

            else -> null
        }
    }
}

sealed interface EditProfileEvent : UiEvent {
    sealed interface Init : EditProfileEvent {
        data class Fail(val toast: String) : Init
    }

    sealed interface Submit : EditProfileEvent {
        data class Result(
            val success: Boolean,
            val changed: Boolean = success,
            val message: String = "",
        ) : Submit
    }

    sealed interface UploadPortrait : EditProfileEvent {
        data object Pick : UploadPortrait
        data class Success(val message: String) : UploadPortrait
        data class Fail(val error: String) : UploadPortrait
    }
}

sealed interface EditProfileIntent : UiIntent {
    data class Init(val userId: String) : EditProfileIntent

    data class Submit(
        val sex: Int,
        val birthdayTime: Long,
        val birthdayShowStatus: Boolean,
        val intro: String,
        val nickName: String,
    ) : EditProfileIntent

    data object SubmitWithoutChange : EditProfileIntent

    data class UploadPortrait(val file: File) : EditProfileIntent

    data object UploadPortraitStart : EditProfileIntent
}

sealed class EditProfilePartialChange : PartialChange<EditProfileState> {
    sealed class UploadPortrait : EditProfilePartialChange() {
        override fun reduce(oldState: EditProfileState): EditProfileState =
            when (this) {
                Uploading -> oldState
                Start -> oldState
                is Success -> oldState
                is Fail -> oldState
            }

        data object Uploading : UploadPortrait()
        data object Start : UploadPortrait()
        data class Success(val message: String) : UploadPortrait()
        data class Fail(val error: String) : UploadPortrait()

    }

    sealed class Init : EditProfilePartialChange() {
        override fun reduce(oldState: EditProfileState): EditProfileState =
            when (this) {
                is Loading -> oldState.copy(isLoading = true)
                is Success -> oldState.copy(
                    isLoading = false,
                    portrait = account.portrait,
                    name = account.name,
                    nickName = account.nameShow ?: account.name,
                    sex = account.sex?.toInt() ?: 0,
                    birthdayTime = account.birthdayTime?.toLong()?.times(1000L) ?: 0L,
                    tbAge = account.tbAge ?: "0",
                    intro = account.intro ?: ""
                )

                is Fail -> oldState.copy(isLoading = false)
            }

        data object Loading : Init()
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
            val intro: String,
            val nickName: String,
        ) : Submit()

        data object Success : Submit()
        data object SuccessWithoutChange : Submit()
        data class Fail(val error: String) : Submit()
    }
}

data class EditProfileState(
    val portrait: String = "",
    val name: String = "",
    val nickName: String = "",
    val sex: Int = 0,
    val birthdayShowStatus: Boolean = false,
    val birthdayTime: Long = 0L,
    val tbAge: String = "0",
    val intro: String? = null,
    val isLoading: Boolean = true,
    val isSubmitting: Boolean = false,
) : UiState