package com.huanchengfly.tieba.post.ui.page.photoview

import com.huanchengfly.tieba.post.api.TiebaApi
import com.huanchengfly.tieba.post.api.models.PicPageBean
import com.huanchengfly.tieba.post.arch.*
import com.huanchengfly.tieba.post.models.protos.PhotoViewData
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*

class PhotoViewViewModel :
    BaseViewModel<PhotoViewUiIntent, PhotoViewPartialChange, PhotoViewUiState, PhotoViewUiEvent>() {
    override fun createInitialState(): PhotoViewUiState  = PhotoViewUiState()

    override fun createPartialChangeProducer(): PartialChangeProducer<PhotoViewUiIntent, PhotoViewPartialChange, PhotoViewUiState> = PhotoViewPartialChangeProducer

    private object PhotoViewPartialChangeProducer : PartialChangeProducer<PhotoViewUiIntent, PhotoViewPartialChange, PhotoViewUiState> {
        @OptIn(FlowPreview::class)
        override fun toPartialChangeFlow(intentFlow: Flow<PhotoViewUiIntent>): Flow<PhotoViewPartialChange> =
            merge(
                intentFlow.filterIsInstance<PhotoViewUiIntent.Init>().flatMapConcat { it.producePartialChange() }
            )

        private fun PhotoViewUiIntent.Init.producePartialChange(): Flow<PhotoViewPartialChange.Init> {
            val flow: Flow<PhotoViewPartialChange.Init> = if (data.data_ == null) {
                flowOf(
                    PhotoViewPartialChange.Init.Success(
                        items = data.picItems.map { PhotoViewItem(originUrl = it.originUrl, url = if (it.showOriginBtn) it.url else null, overallIndex = null) },
                        hasNext = false,
                        hasPrev = false,
                        totalAmount = data.picItems.size,
                        initialIndex = data.index
                    )
                )
            } else {
                TiebaApi.getInstance().picPageFlow(
                    forumId = data.data_.forumId.toString(),
                    forumName = data.data_.forumName,
                    threadId = data.data_.threadId.toString(),
                    seeLz = data.data_.seeLz,
                    picId = data.data_.picId,
                    picIndex = data.data_.picIndex.toString(),
                    objType = data.data_.objType,
                    prev = false
                ).map<PicPageBean, PhotoViewPartialChange.Init> { picPageBean ->
                    val picAmount = picPageBean.picAmount.toInt()
                    val hasNext = picPageBean.picList.last().overAllIndex.toInt() < picAmount
                    val hasPrev = picPageBean.picList.first().overAllIndex.toInt() > 1
                    PhotoViewPartialChange.Init.Success(
                        hasPrev = hasPrev,
                        hasNext = hasNext,
                        totalAmount = picAmount,
                        items = picPageBean.picList.map {
                            PhotoViewItem(
                                originUrl = it.img.original.originalSrc,
                                url = if (it.showOriginalBtn) it.img.original.bigCdnSrc else null,
                                overallIndex = it.overAllIndex.toInt()
                            )
                        },
                        initialIndex = data.data_.picIndex
                    )
                }.catch { emit(PhotoViewPartialChange.Init.Failure(data, it)) }
            }
            return flow
        }
    }
}

sealed interface PhotoViewUiIntent : UiIntent {
    data class Init(val data: PhotoViewData) : PhotoViewUiIntent
}

sealed interface PhotoViewPartialChange : PartialChange<PhotoViewUiState> {
    sealed class Init : PhotoViewPartialChange {
        override fun reduce(oldState: PhotoViewUiState): PhotoViewUiState =
            when (this) {
                is Success -> oldState.copy(
                    data = items,
                    hasNext = hasNext,
                    hasPrev = hasPrev,
                    totalAmount = totalAmount,
                    isLoading = false
                )
                is Failure -> oldState.copy(
                    data = listOf(PhotoViewItem(data.url, null, null)),
                    totalAmount = 1,
                    isLoading = false,
                    hasPrev = false,
                    hasNext = false,
                )
            }

        data class Success(
            val items: List<PhotoViewItem>,
            val hasNext: Boolean,
            val hasPrev: Boolean,
            val totalAmount: Int,
            val initialIndex: Int,
        ) : Init()

        data class Failure(
            val data: PhotoViewData,
            val error: Throwable
        ) : Init()
    }
}

data class PhotoViewUiState(
    val isLoading: Boolean = false,
    val data: List<PhotoViewItem> = emptyList(),
    val totalAmount: Int = 0,
    val hasNext: Boolean = false,
    val hasPrev: Boolean = false,
    val initialIndex: Int = 0,
) : UiState

sealed interface PhotoViewUiEvent : UiEvent

data class PhotoViewItem(
    val originUrl: String,
    val url: String?,
    val overallIndex: Int?
)