package com.huanchengfly.tieba.post.ui.page.reply

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.Stable
import com.huanchengfly.tieba.post.App
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.api.TiebaApi
import com.huanchengfly.tieba.post.api.models.UploadPictureResultBean
import com.huanchengfly.tieba.post.api.models.protos.addPost.AddPostResponse
import com.huanchengfly.tieba.post.api.retrofit.exception.TiebaUnknownException
import com.huanchengfly.tieba.post.api.retrofit.exception.getErrorCode
import com.huanchengfly.tieba.post.api.retrofit.exception.getErrorMessage
import com.huanchengfly.tieba.post.arch.BaseViewModel
import com.huanchengfly.tieba.post.arch.CommonUiEvent
import com.huanchengfly.tieba.post.arch.PartialChange
import com.huanchengfly.tieba.post.arch.PartialChangeProducer
import com.huanchengfly.tieba.post.arch.UiEvent
import com.huanchengfly.tieba.post.arch.UiIntent
import com.huanchengfly.tieba.post.arch.UiState
import com.huanchengfly.tieba.post.components.ImageUploader
import com.huanchengfly.tieba.post.utils.FileUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

enum class ReplyPanelType {
    NONE,
    EMOJI,
    IMAGE,
    VOICE
}

@Stable
@HiltViewModel
class ReplyViewModel @Inject constructor() :
    BaseViewModel<ReplyUiIntent, ReplyPartialChange, ReplyUiState, ReplyUiEvent>() {
    override fun createInitialState() = ReplyUiState()

    override fun createPartialChangeProducer(): PartialChangeProducer<ReplyUiIntent, ReplyPartialChange, ReplyUiState> =
        ReplyPartialChangeProducer

    override fun dispatchEvent(partialChange: ReplyPartialChange): UiEvent? = when (partialChange) {
        is ReplyPartialChange.Send.Success -> ReplyUiEvent.ReplySuccess(
            partialChange.threadId,
            partialChange.postId,
            partialChange.expInc
        )

        is ReplyPartialChange.UploadImages.Success -> ReplyUiEvent.UploadSuccess(partialChange.resultList)

        is ReplyPartialChange.Send.Failure -> CommonUiEvent.Toast(
            App.INSTANCE.getString(
                R.string.toast_reply_failed,
                partialChange.errorCode,
                partialChange.errorMessage
            )
        )

        is ReplyPartialChange.UploadImages.Failure -> CommonUiEvent.Toast(
            App.INSTANCE.getString(
                R.string.toast_upload_image_failed,
                partialChange.errorMessage
            )
        )

        else -> null
    }

    private object ReplyPartialChangeProducer :
        PartialChangeProducer<ReplyUiIntent, ReplyPartialChange, ReplyUiState> {
        @OptIn(ExperimentalCoroutinesApi::class)
        override fun toPartialChangeFlow(intentFlow: Flow<ReplyUiIntent>): Flow<ReplyPartialChange> =
            merge(
                intentFlow.filterIsInstance<ReplyUiIntent.UploadImages>()
                    .flatMapConcat { it.producePartialChange() },
                intentFlow.filterIsInstance<ReplyUiIntent.Send>()
                    .flatMapConcat { it.producePartialChange() },
                intentFlow.filterIsInstance<ReplyUiIntent.SwitchPanel>()
                    .flatMapConcat { it.producePartialChange() },
                intentFlow.filterIsInstance<ReplyUiIntent.AddImage>()
                    .flatMapConcat { it.producePartialChange() },
                intentFlow.filterIsInstance<ReplyUiIntent.RemoveImage>()
                    .flatMapConcat { it.producePartialChange() },
                intentFlow.filterIsInstance<ReplyUiIntent.ToggleIsOriginImage>()
                    .flatMapConcat { it.producePartialChange() },
            )

        private fun ReplyUiIntent.Send.producePartialChange(): Flow<ReplyPartialChange.Send> {
            return TiebaApi.getInstance()
                .addPostFlow(
                    content,
                    forumId.toString(),
                    forumName,
                    threadId.toString(),
                    tbs,
                    postId = postId?.toString(),
                    subPostId = subPostId?.toString(),
                    replyUserId = replyUserId?.toString()
                )
                .map<AddPostResponse, ReplyPartialChange.Send> {
                    if (it.data_ == null) throw TiebaUnknownException
                    ReplyPartialChange.Send.Success(
                        threadId = it.data_.tid,
                        postId = it.data_.pid,
                        expInc = it.data_.exp?.inc.orEmpty()
                    )
                }
                .onStart { emit(ReplyPartialChange.Send.Start) }
                .catch {
                    Log.i("ReplyViewModel", "failure: ${it.message}")
                    it.printStackTrace()
                    emit(ReplyPartialChange.Send.Failure(it.getErrorCode(), it.getErrorMessage()))
                }
        }

        private fun ReplyUiIntent.UploadImages.producePartialChange() =
            ImageUploader(forumName)
                .uploadImages(
                    imageUris.map {
                        FileUtil.getRealPathFromUri(
                            App.INSTANCE,
                            Uri.parse(it)
                        )
                    },
                    isOriginImage
                )
                .map<List<UploadPictureResultBean>, ReplyPartialChange.UploadImages> {
                    ReplyPartialChange.UploadImages.Success(it)
                }
                .onStart { emit(ReplyPartialChange.UploadImages.Start) }
                .catch {
                    it.printStackTrace()
                    emit(
                        ReplyPartialChange.UploadImages.Failure(
                            it.getErrorCode(),
                            it.getErrorMessage()
                        )
                    )
                }

        private fun ReplyUiIntent.SwitchPanel.producePartialChange() =
            flowOf(ReplyPartialChange.SwitchPanel(panelType))

        private fun ReplyUiIntent.AddImage.producePartialChange() =
            flowOf(ReplyPartialChange.AddImage(imageUris))

        private fun ReplyUiIntent.RemoveImage.producePartialChange() =
            flowOf(ReplyPartialChange.RemoveImage(imageIndex))

        private fun ReplyUiIntent.ToggleIsOriginImage.producePartialChange() =
            flowOf(ReplyPartialChange.ToggleIsOriginImage(isOriginImage))
    }
}

sealed interface ReplyUiIntent : UiIntent {
    data class UploadImages(
        val forumName: String,
        val imageUris: List<String>,
        val isOriginImage: Boolean
    ) : ReplyUiIntent

    data class Send(
        val content: String,
        val forumId: Long,
        val forumName: String,
        val threadId: Long,
        val tbs: String,
        val postId: Long? = null,
        val subPostId: Long? = null,
        val replyUserId: Long? = null,
    ) : ReplyUiIntent

    data class SwitchPanel(val panelType: ReplyPanelType) : ReplyUiIntent

    data class AddImage(val imageUris: List<String>) : ReplyUiIntent

    data class RemoveImage(val imageIndex: Int) : ReplyUiIntent

    data class ToggleIsOriginImage(val isOriginImage: Boolean) : ReplyUiIntent
}

sealed interface ReplyPartialChange : PartialChange<ReplyUiState> {
    sealed class UploadImages : ReplyPartialChange {
        override fun reduce(oldState: ReplyUiState): ReplyUiState = when (this) {
            is Start -> oldState.copy(isUploading = true)
            is Success -> oldState.copy(
                isUploading = false,
                uploadImageResultList = resultList.toImmutableList()
            )

            is Failure -> oldState.copy(isUploading = false)
        }

        object Start : UploadImages()

        data class Success(val resultList: List<UploadPictureResultBean>) : UploadImages()

        data class Failure(
            val errorCode: Int,
            val errorMessage: String
        ) : UploadImages()
    }

    sealed class Send : ReplyPartialChange {
        override fun reduce(oldState: ReplyUiState): ReplyUiState {
            return when (this) {
                is Start -> oldState.copy(isSending = true)
                is Success -> oldState.copy(isSending = false, replySuccess = true)
                is Failure -> oldState.copy(isSending = false, replySuccess = false)
            }
        }

        object Start : Send()

        data class Success(
            val threadId: String,
            val postId: String,
            val expInc: String
        ) : Send()

        data class Failure(
            val errorCode: Int,
            val errorMessage: String
        ) : Send()
    }

    data class SwitchPanel(val panelType: ReplyPanelType) : ReplyPartialChange {
        override fun reduce(oldState: ReplyUiState): ReplyUiState =
            oldState.copy(replyPanelType = panelType)
    }

    data class AddImage(val imageUris: List<String>) : ReplyPartialChange {
        override fun reduce(oldState: ReplyUiState): ReplyUiState =
            oldState.copy(selectedImageList = (oldState.selectedImageList + imageUris).toImmutableList())
    }

    data class RemoveImage(val imageIndex: Int) : ReplyPartialChange {
        override fun reduce(oldState: ReplyUiState): ReplyUiState =
            oldState.copy(selectedImageList = (oldState.selectedImageList - oldState.selectedImageList[imageIndex]).toImmutableList())
    }

    data class ToggleIsOriginImage(val isOriginImage: Boolean) : ReplyPartialChange {
        override fun reduce(oldState: ReplyUiState): ReplyUiState =
            oldState.copy(isOriginImage = isOriginImage)
    }
}

data class ReplyUiState(
    val isSending: Boolean = false,
    val replySuccess: Boolean = false,
    val replyPanelType: ReplyPanelType = ReplyPanelType.NONE,

    val isUploading: Boolean = false,
    val isOriginImage: Boolean = false,
    val selectedImageList: ImmutableList<String> = persistentListOf(),
    val uploadImageResultList: ImmutableList<UploadPictureResultBean> = persistentListOf(),
) : UiState

sealed interface ReplyUiEvent : UiEvent {
    data class UploadSuccess(val resultList: List<UploadPictureResultBean>) : ReplyUiEvent

    data class ReplySuccess(
        val threadId: String,
        val postId: String,
        val expInc: String
    ) : ReplyUiEvent
}