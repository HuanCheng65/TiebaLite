package com.huanchengfly.tieba.post.ui.page.user

import com.huanchengfly.tieba.post.api.TiebaApi
import com.huanchengfly.tieba.post.api.models.protos.User
import com.huanchengfly.tieba.post.api.models.protos.profile.ProfileResponse
import com.huanchengfly.tieba.post.arch.BaseViewModel
import com.huanchengfly.tieba.post.arch.ImmutableHolder
import com.huanchengfly.tieba.post.arch.PartialChange
import com.huanchengfly.tieba.post.arch.PartialChangeProducer
import com.huanchengfly.tieba.post.arch.UiEvent
import com.huanchengfly.tieba.post.arch.UiIntent
import com.huanchengfly.tieba.post.arch.UiState
import com.huanchengfly.tieba.post.arch.wrapImmutable
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

@HiltViewModel
class UserProfileViewModel @Inject constructor() :
    BaseViewModel<UserProfileUiIntent, UserProfilePartialChange, UserProfileUiState, UiEvent>() {
    override fun createInitialState(): UserProfileUiState = UserProfileUiState()

    override fun createPartialChangeProducer(): PartialChangeProducer<UserProfileUiIntent, UserProfilePartialChange, UserProfileUiState> =
        UserProfilePartialChangeProducer

    private object UserProfilePartialChangeProducer :
        PartialChangeProducer<UserProfileUiIntent, UserProfilePartialChange, UserProfileUiState> {
        @OptIn(ExperimentalCoroutinesApi::class)
        override fun toPartialChangeFlow(intentFlow: Flow<UserProfileUiIntent>): Flow<UserProfilePartialChange> =
            merge(
                intentFlow.filterIsInstance<UserProfileUiIntent.Refresh>()
                    .flatMapConcat { it.producePartialChange() },
            )

        private fun UserProfileUiIntent.Refresh.producePartialChange(): Flow<UserProfilePartialChange.Refresh> =
            TiebaApi.getInstance()
                .userProfileFlow(uid)
                .map<ProfileResponse, UserProfilePartialChange.Refresh> {
                    checkNotNull(it.data_)
                    checkNotNull(it.data_.user)
                    UserProfilePartialChange.Refresh.Success(it.data_.user)
                }
                .onStart { emit(UserProfilePartialChange.Refresh.Start) }
                .catch { emit(UserProfilePartialChange.Refresh.Failure(it)) }
    }
}

sealed interface UserProfileUiIntent : UiIntent {
    data class Refresh(
        val uid: Long,
    ) : UserProfileUiIntent
}

sealed interface UserProfilePartialChange : PartialChange<UserProfileUiState> {
    sealed class Refresh : UserProfilePartialChange {
        override fun reduce(oldState: UserProfileUiState): UserProfileUiState = when (this) {
            is Start -> oldState.copy(
                isRefreshing = true,
            )

            is Success -> oldState.copy(
                isRefreshing = false,
                error = null,
                user = user.wrapImmutable(),
            )

            is Failure -> oldState.copy(
                isRefreshing = false,
                error = error.wrapImmutable(),
            )
        }

        data object Start : Refresh()

        data class Success(
            val user: User,
        ) : Refresh()

        data class Failure(
            val error: Throwable,
        ) : Refresh()
    }
}

data class UserProfileUiState(
    val isRefreshing: Boolean = false,
    val error: ImmutableHolder<Throwable>? = null,

    val user: ImmutableHolder<User>? = null,
) : UiState