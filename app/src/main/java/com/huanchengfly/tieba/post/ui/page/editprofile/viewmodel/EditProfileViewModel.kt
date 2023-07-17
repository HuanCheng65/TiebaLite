package com.huanchengfly.tieba.post.ui.page.editprofile.viewmodel

import androidx.compose.runtime.Stable
import com.huanchengfly.tieba.post.api.TiebaApi
import com.huanchengfly.tieba.post.api.interfaces.ITiebaApi
import com.huanchengfly.tieba.post.api.retrofit.exception.getErrorCode
import com.huanchengfly.tieba.post.api.retrofit.exception.getErrorMessage
import com.huanchengfly.tieba.post.arch.BaseViewModel
import com.huanchengfly.tieba.post.arch.PartialChangeProducer
import com.huanchengfly.tieba.post.arch.UiEvent
import com.huanchengfly.tieba.post.ui.page.editprofile.EditProfileEvent
import com.huanchengfly.tieba.post.ui.page.editprofile.EditProfileIntent
import com.huanchengfly.tieba.post.ui.page.editprofile.EditProfilePartialChange
import com.huanchengfly.tieba.post.ui.page.editprofile.EditProfileState
import com.huanchengfly.tieba.post.utils.AccountUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
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
                intentFlow.filterIsInstance<EditProfileIntent.ModifyNickname>()
                    .flatMapConcat { it.toPartialChangeFlow() },
                intentFlow.filterIsInstance<EditProfileIntent.ModifyNicknameFinish>()
                    .flatMapConcat { it.toPartialChangeFlow() },
                intentFlow.filterIsInstance<EditProfileIntent.UploadPortrait>()
                    .flatMapConcat { it.toPartialChangeFlow() },
                intentFlow.filterIsInstance<EditProfileIntent.UploadPortraitStart>()
                    .flatMapConcat { it.toPartialChangeFlow() }
            )

        private fun EditProfileIntent.Init.toPartialChangeFlow() =
            (flow {
                emit(AccountUtil.getAccountInfoByUid(userId))
            }.map {
                if (it != null) EditProfilePartialChange.Init.Success(account = it) else EditProfilePartialChange.Init.Fail(
                    "account does not exist"
                )
            }).onStart { emit(EditProfilePartialChange.Init.Loading) }
                .catch { emit(EditProfilePartialChange.Init.Fail(it.getErrorMessage())) }

        private fun EditProfileIntent.Submit.toPartialChangeFlow() =
            tiebaApi.profileModifyFlow(birthdayShowStatus, "${birthdayTime / 1000L}", intro, "$sex")
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
                            intro
                        )
                    )
                }
                .catch { emit(EditProfilePartialChange.Submit.Fail(it.getErrorMessage())) }

        private fun EditProfileIntent.SubmitWithoutChange.toPartialChangeFlow() =
            flow { emit(EditProfilePartialChange.Submit.SuccessWithoutChange) }

        private fun EditProfileIntent.ModifyNickname.toPartialChangeFlow() =
            flow { emit(EditProfilePartialChange.ModifyNickname.Start) }

        private fun EditProfileIntent.UploadPortraitStart.toPartialChangeFlow() =
            flow { emit(EditProfilePartialChange.UploadPortrait.Start) }

        private fun EditProfileIntent.ModifyNicknameFinish.toPartialChangeFlow() =
            flow { emit(EditProfilePartialChange.ModifyNickname.Finish(result)) }

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

            EditProfilePartialChange.ModifyNickname.Start -> EditProfileEvent.ModifyNickname.Start
            is EditProfilePartialChange.ModifyNickname.Finish -> EditProfileEvent.ModifyNickname.Finish(
                partialChange.result
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