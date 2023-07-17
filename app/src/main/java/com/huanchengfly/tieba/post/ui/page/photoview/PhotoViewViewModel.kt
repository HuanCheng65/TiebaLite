package com.huanchengfly.tieba.post.ui.page.photoview

import com.huanchengfly.tieba.post.api.TiebaApi
import com.huanchengfly.tieba.post.api.models.PicPageBean
import com.huanchengfly.tieba.post.arch.BaseViewModel
import com.huanchengfly.tieba.post.arch.PartialChange
import com.huanchengfly.tieba.post.arch.PartialChangeProducer
import com.huanchengfly.tieba.post.arch.UiEvent
import com.huanchengfly.tieba.post.arch.UiIntent
import com.huanchengfly.tieba.post.arch.UiState
import com.huanchengfly.tieba.post.models.protos.LoadPicPageData
import com.huanchengfly.tieba.post.models.protos.PhotoViewData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onStart

class PhotoViewViewModel :
    BaseViewModel<PhotoViewUiIntent, PhotoViewPartialChange, PhotoViewUiState, PhotoViewUiEvent>() {
    override fun createInitialState(): PhotoViewUiState  = PhotoViewUiState()

    override fun createPartialChangeProducer(): PartialChangeProducer<PhotoViewUiIntent, PhotoViewPartialChange, PhotoViewUiState> = PhotoViewPartialChangeProducer

    private object PhotoViewPartialChangeProducer : PartialChangeProducer<PhotoViewUiIntent, PhotoViewPartialChange, PhotoViewUiState> {
        @OptIn(ExperimentalCoroutinesApi::class)
        override fun toPartialChangeFlow(intentFlow: Flow<PhotoViewUiIntent>): Flow<PhotoViewPartialChange> =
            merge(
                intentFlow.filterIsInstance<PhotoViewUiIntent.Init>()
                    .flatMapConcat { it.producePartialChange() },
                intentFlow.filterIsInstance<PhotoViewUiIntent.LoadPrev>()
                    .flatMapConcat { it.producePartialChange() },
                intentFlow.filterIsInstance<PhotoViewUiIntent.LoadMore>()
                    .flatMapConcat { it.producePartialChange() },
            )

        private fun PhotoViewUiIntent.LoadPrev.producePartialChange(): Flow<PhotoViewPartialChange.LoadPrev> =
            TiebaApi.getInstance()
                .picPageFlow(
                    forumId = data.forumId.toString(),
                    forumName = data.forumName,
                    threadId = data.threadId.toString(),
                    seeLz = data.seeLz,
                    picId = picId,
                    picIndex = overallIndex.toString(),
                    objType = data.objType,
                    prev = true
                )
                .map<PicPageBean, PhotoViewPartialChange.LoadPrev> { picPageBean ->
                    val items = picPageBean.picList.map {
                        PhotoViewItem(
                            picId = it.img.original.id,
                            originUrl = it.img.original.originalSrc,
                            url = if (it.showOriginalBtn) it.img.original.bigCdnSrc else null,
                            overallIndex = it.overAllIndex.toInt()
                        )
                    }
                    val hasPrev = items.first().overallIndex > 1
                    PhotoViewPartialChange.LoadPrev.Success(
                        hasPrev = hasPrev,
                        items = items
                    )
                }
                .onStart { emit(PhotoViewPartialChange.LoadPrev.Start) }
                .catch {
                    emit(PhotoViewPartialChange.LoadPrev.Failure(it))
                }

        private fun PhotoViewUiIntent.LoadMore.producePartialChange(): Flow<PhotoViewPartialChange.LoadMore> =
            TiebaApi.getInstance()
                .picPageFlow(
                    forumId = data.forumId.toString(),
                    forumName = data.forumName,
                    threadId = data.threadId.toString(),
                    seeLz = data.seeLz,
                    picId = picId,
                    picIndex = overallIndex.toString(),
                    objType = data.objType,
                    prev = false
                )
                .map<PicPageBean, PhotoViewPartialChange.LoadMore> { picPageBean ->
                    val items = picPageBean.picList.map {
                        PhotoViewItem(
                            picId = it.img.original.id,
                            originUrl = it.img.original.originalSrc,
                            url = if (it.showOriginalBtn) it.img.original.bigCdnSrc else null,
                            overallIndex = it.overAllIndex.toInt()
                        )
                    }
                    val hasNext = items.last().overallIndex < picPageBean.picAmount.toInt()
                    PhotoViewPartialChange.LoadMore.Success(
                        hasNext = hasNext,
                        items = items
                    )
                }
                .onStart { emit(PhotoViewPartialChange.LoadMore.Start) }
                .catch {
                    emit(PhotoViewPartialChange.LoadMore.Failure(it))
                }

        private fun PhotoViewUiIntent.Init.producePartialChange(): Flow<PhotoViewPartialChange.Init> {
            val flow = if (data.data_ == null) {
                flowOf(
                    PhotoViewPartialChange.Init.Success(
                        items = data.picItems.mapIndexed { index, item ->
                            PhotoViewItem(
                                picId = item.picId,
                                originUrl = item.originUrl,
                                url = if (item.showOriginBtn) item.url else null,
                                overallIndex = index + 1
                            )
                        },
                        hasNext = false,
                        hasPrev = false,
                        totalAmount = data.picItems.size,
                        initialIndex = data.index,
                        loadPicPageData = null
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
                                picId = it.img.original.id,
                                originUrl = it.img.original.originalSrc,
                                url = if (it.showOriginalBtn) it.img.original.bigCdnSrc else null,
                                overallIndex = it.overAllIndex.toInt()
                            )
                        }
                        val firstItemIndex = fetchedItems.first().overallIndex
                        val localItems =
                            if (data.data_.picIndex == 1) emptyList() else data.picItems.subList(
                                0,
                                data.data_.picIndex - 1
                            ).mapIndexed { index, item ->
                                PhotoViewItem(
                                    picId = item.picId,
                                    originUrl = item.originUrl,
                                    url = if (item.showOriginBtn) item.url else null,
                                    overallIndex = firstItemIndex - (data.data_.picIndex - 1 - index)
                                )
                            }
                        val items = localItems + fetchedItems
                        val hasNext = items.last().overallIndex < picAmount
                        val hasPrev = items.first().overallIndex > 1
                        PhotoViewPartialChange.Init.Success(
                            hasPrev = hasPrev,
                            hasNext = hasNext,
                            totalAmount = picAmount,
                            items = items,
                            initialIndex = data.data_.picIndex - 1,
                            loadPicPageData = data.data_
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

    data class LoadMore(
        val picId: String,
        val overallIndex: Int,
        val data: LoadPicPageData
    ) : PhotoViewUiIntent

    data class LoadPrev(
        val picId: String,
        val overallIndex: Int,
        val data: LoadPicPageData
    ) : PhotoViewUiIntent
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
                    loadPicPageData = loadPicPageData,
                    isLoading = false
                )

                is Failure -> {
                    oldState.copy(
                        data = data.picItems.mapIndexed { index, item ->
                            PhotoViewItem(
                                picId = item.picId,
                                originUrl = item.originUrl,
                                url = if (item.showOriginBtn) item.url else null,
                                overallIndex = index + 1
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
            val loadPicPageData: LoadPicPageData?,
        ) : Init()

        data class Failure(
            val data: PhotoViewData,
            val error: Throwable
        ) : Init()
    }

    sealed class LoadPrev : PhotoViewPartialChange {
        override fun reduce(oldState: PhotoViewUiState): PhotoViewUiState =
            when (this) {
                Start -> oldState.copy(isLoading = true)

                is Success -> oldState.copy(
                    data = items.filterNot { item -> oldState.data.any { item.picId == it.picId } } + oldState.data,
                    hasPrev = hasPrev,
                    isLoading = false
                )

                is Failure -> oldState.copy(isLoading = false)
            }

        object Start : LoadPrev()

        data class Success(
            val items: List<PhotoViewItem>,
            val hasPrev: Boolean,
        ) : LoadPrev()

        data class Failure(
            val error: Throwable
        ) : LoadPrev()
    }

    sealed class LoadMore : PhotoViewPartialChange {
        override fun reduce(oldState: PhotoViewUiState): PhotoViewUiState =
            when (this) {
                Start -> oldState.copy(isLoading = true)

                is Success -> oldState.copy(
                    data = oldState.data + items.filterNot { item -> oldState.data.any { item.picId == it.picId } },
                    hasNext = hasNext,
                    isLoading = false
                )

                is Failure -> oldState.copy(isLoading = false)
            }

        object Start : LoadMore()

        data class Success(
            val items: List<PhotoViewItem>,
            val hasNext: Boolean,
        ) : LoadMore()

        data class Failure(
            val error: Throwable
        ) : LoadMore()
    }
}

data class PhotoViewUiState(
    val isLoading: Boolean = false,
    val data: List<PhotoViewItem> = emptyList(),
    val totalAmount: Int = 0,
    val hasNext: Boolean = false,
    val hasPrev: Boolean = false,
    val initialIndex: Int = 0,
    val loadPicPageData: LoadPicPageData? = null,
) : UiState

sealed interface PhotoViewUiEvent : UiEvent

data class PhotoViewItem(
    val picId: String,
    val originUrl: String,
    val url: String?,
    val overallIndex: Int
)