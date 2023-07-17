package com.huanchengfly.tieba.post.ui.page.settings.block

import com.huanchengfly.tieba.post.api.retrofit.exception.getErrorMessage
import com.huanchengfly.tieba.post.arch.*
import com.huanchengfly.tieba.post.models.database.Block
import com.huanchengfly.tieba.post.toJson
import com.huanchengfly.tieba.post.utils.BlockManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

class BlockSettingsViewModel :
    BaseViewModel<BlockSettingsUiIntent, BlockSettingsPartialChange, BlockSettingsUiState, BlockSettingsUiEvent>() {
    override fun createInitialState(): BlockSettingsUiState = BlockSettingsUiState()

    override fun createPartialChangeProducer(): PartialChangeProducer<BlockSettingsUiIntent, BlockSettingsPartialChange, BlockSettingsUiState> =
        BlockSettingsPartialChangeProducer

    override fun dispatchEvent(partialChange: BlockSettingsPartialChange): UiEvent? =
        when (partialChange) {
            is BlockSettingsPartialChange.Load.Failure -> CommonUiEvent.Toast(partialChange.error.getErrorMessage())
            is BlockSettingsPartialChange.Add.Failure -> CommonUiEvent.Toast(partialChange.error.getErrorMessage())
            is BlockSettingsPartialChange.Delete.Failure -> CommonUiEvent.Toast(partialChange.error.getErrorMessage())
            is BlockSettingsPartialChange.Add.Success -> BlockSettingsUiEvent.Success.Add
            is BlockSettingsPartialChange.Delete.Success -> BlockSettingsUiEvent.Success.Delete
            else -> null
        }

    private object BlockSettingsPartialChangeProducer : PartialChangeProducer<BlockSettingsUiIntent, BlockSettingsPartialChange, BlockSettingsUiState> {
        @OptIn(ExperimentalCoroutinesApi::class)
        override fun toPartialChangeFlow(intentFlow: Flow<BlockSettingsUiIntent>): Flow<BlockSettingsPartialChange> =
            merge(
                intentFlow.filterIsInstance<BlockSettingsUiIntent.Load>().flatMapConcat { produceLoadPartialChange() },
                intentFlow.filterIsInstance<BlockSettingsUiIntent.Add>().flatMapConcat { it.producePartialChange() },
                intentFlow.filterIsInstance<BlockSettingsUiIntent.Delete>().flatMapConcat { it.producePartialChange() },
            )

        private fun produceLoadPartialChange(): Flow<BlockSettingsPartialChange.Load> =
            flowOf<BlockSettingsPartialChange.Load>(
                BlockSettingsPartialChange.Load.Success(
                    BlockManager.blackList,
                    BlockManager.whiteList
                )
            )
                .onStart { BlockSettingsPartialChange.Load.Start }
                .catch { emit(BlockSettingsPartialChange.Load.Failure(it)) }

        private fun BlockSettingsUiIntent.Add.producePartialChange(): Flow<BlockSettingsPartialChange.Add> =
            flow<BlockSettingsPartialChange.Add> {
                val block = Block(
                    category = category, type = Block.TYPE_KEYWORD, keywords = keywords.toJson()
                )
                BlockManager.addBlock(block)
                emit(BlockSettingsPartialChange.Add.Success(block))
            }.catch { emit(BlockSettingsPartialChange.Add.Failure(it)) }

        private fun BlockSettingsUiIntent.Delete.producePartialChange(): Flow<BlockSettingsPartialChange.Delete> =
            flow<BlockSettingsPartialChange.Delete> {
                BlockManager.removeBlock(id)
                emit(BlockSettingsPartialChange.Delete.Success(id))
            }.catch { emit(BlockSettingsPartialChange.Delete.Failure(it)) }
    }
}

sealed interface BlockSettingsUiIntent : UiIntent {
    object Load : BlockSettingsUiIntent

    data class Add(
        val category: Int,
        val keywords: List<String>
    ) : BlockSettingsUiIntent

    data class Delete(
        val id: Long,
    ) : BlockSettingsUiIntent
}

sealed interface BlockSettingsPartialChange : PartialChange<BlockSettingsUiState> {
    sealed class Load : BlockSettingsPartialChange {
        override fun reduce(oldState: BlockSettingsUiState): BlockSettingsUiState =
            when (this) {
                Start -> oldState.copy(isLoading = true)
                is Success -> oldState.copy(isLoading = false, blackList = blackList, whiteList = whiteList)
                is Failure -> oldState.copy(isLoading = false)
            }

        object Start : Load()

        data class Success(val blackList: List<Block>, val whiteList: List<Block>) : Load()

        data class Failure(val error: Throwable) : Load()
    }

    sealed class Add : BlockSettingsPartialChange {
        override fun reduce(oldState: BlockSettingsUiState): BlockSettingsUiState =
            when (this) {
                is Success -> {
                    val newBlackList =
                        if (item.category == Block.CATEGORY_BLACK_LIST) {
                            oldState.blackList + item
                        } else {
                            oldState.blackList
                        }
                    val newWhiteList =
                        if (item.category == Block.CATEGORY_WHITE_LIST) {
                            oldState.whiteList + item
                        } else {
                            oldState.whiteList
                        }
                    oldState.copy(blackList = newBlackList, whiteList = newWhiteList)
                }
                is Failure -> oldState
            }

        data class Success(val item: Block) : Add()

        data class Failure(val error: Throwable) : Add()
    }

    sealed class Delete : BlockSettingsPartialChange {
        override fun reduce(oldState: BlockSettingsUiState): BlockSettingsUiState =
            when (this) {
                is Success -> {
                    val newBlackList = oldState.blackList.filterNot { it.id == deletedId }
                    val newWhiteList = oldState.whiteList.filterNot { it.id == deletedId }
                    oldState.copy(blackList = newBlackList, whiteList = newWhiteList)
                }
                is Failure -> oldState
            }

        data class Success(val deletedId: Long) : Delete()

        data class Failure(val error: Throwable) : Delete()
    }
}

data class BlockSettingsUiState(
    val isLoading: Boolean = true,
    val blackList: List<Block> = emptyList(),
    val whiteList: List<Block> = emptyList(),
) : UiState

sealed interface BlockSettingsUiEvent : UiEvent {
    sealed interface Success : BlockSettingsUiEvent {
        object Add : Success
        object Delete : Success
    }
}