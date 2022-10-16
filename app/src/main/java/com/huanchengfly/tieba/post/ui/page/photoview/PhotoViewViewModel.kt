package com.huanchengfly.tieba.post.ui.page.photoview

import com.huanchengfly.tieba.post.api.TiebaApi
import com.huanchengfly.tieba.post.api.models.PicPageBean
import com.huanchengfly.tieba.post.arch.BaseViewModel
import com.huanchengfly.tieba.post.arch.PartialChange
import com.huanchengfly.tieba.post.arch.PartialChangeProducer
import com.huanchengfly.tieba.post.arch.UiEvent
import com.huanchengfly.tieba.post.arch.UiIntent
import com.huanchengfly.tieba.post.arch.UiState
import com.huanchengfly.tieba.post.models.protos.PhotoViewData
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge

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
            val flow = if (data.data_ == null) {
                flowOf(
                    PhotoViewPartialChange.Init.Success(
                        items = data.picItems.map {
                            PhotoViewItem(
                                originUrl = it.originUrl,
                                url = if (it.showOriginBtn) it.url else null,
                                overallIndex = null
                            )
                        },
                        hasNext = false,
                        hasPrev = false,
                        totalAmount = data.picItems.size,
                        initialIndex = data.index
                    )
                )
            } else {
                TiebaApi.getInstance()
                    .picPageFlow(
                        forumId = data.data_.forumId.toString(),
                        forumName = data.data_.forumName,
                        threadId = data.data_.threadId.toString(),
                        seeLz = data.data_.seeLz,
                        picId = data.data_.picId,
                        picIndex = data.data_.picIndex.toString(),
                        objType = data.data_.objType,
                        prev = false
                    )
                    .map<PicPageBean, PhotoViewPartialChange.Init> { picPageBean ->
                        val picAmount = picPageBean.picAmount.toInt()
                        val fetchedItems = picPageBean.picList.map {
                            PhotoViewItem(
                                originUrl = it.img.original.originalSrc,
                                url = if (it.showOriginalBtn) it.img.original.bigCdnSrc else null,
                                overallIndex = it.overAllIndex.toInt()
                            )
                        }
                        val firstItemIndex = fetchedItems.first().overallIndex!!
                        val localItems =
                            if (data.data_.picIndex == 1) emptyList() else data.picItems.subList(
                                0,
                                data.data_.picIndex - 1
                            ).mapIndexed { index, item ->
                                PhotoViewItem(
                                    originUrl = item.originUrl,
                                    url = if (item.showOriginBtn) item.url else null,
                                    overallIndex = firstItemIndex - (data.data_.picIndex - 1 - index)
                                )
                            }
                        val items = localItems + fetchedItems
                        val hasNext = items.last().overallIndex!! < picAmount
                        val hasPrev = items.first().overallIndex!! > 1
                        PhotoViewPartialChange.Init.Success(
                            hasPrev = hasPrev,
                            hasNext = hasNext,
                            totalAmount = picAmount,
                            items = items,
                            initialIndex = data.data_.picIndex - 1
                        )
                    }
                    .catch {
                        emit(PhotoViewPartialChange.Init.Failure(data, it))
                    }
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
                    initialIndex = initialIndex,
                    isLoading = false
                )

                is Failure -> {
                    oldState.copy(
                        data = data.picItems.map {
                            PhotoViewItem(
                                originUrl = it.originUrl,
                                url = if (it.showOriginBtn) it.url else null,
                                overallIndex = null
                            )
                        },
                        hasNext = false,
                        hasPrev = false,
                        totalAmount = data.picItems.size,
                        initialIndex = data.index,
                        isLoading = false,
                    )
                }
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