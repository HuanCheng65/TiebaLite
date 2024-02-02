package com.huanchengfly.tieba.post.ui.page.user

import com.huanchengfly.tieba.post.App
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.api.TiebaApi
import com.huanchengfly.tieba.post.api.models.CommonResponse
import com.huanchengfly.tieba.post.api.models.FollowBean
import com.huanchengfly.tieba.post.api.models.protos.User
import com.huanchengfly.tieba.post.api.models.protos.profile.ProfileResponse
import com.huanchengfly.tieba.post.api.retrofit.exception.getErrorMessage
import com.huanchengfly.tieba.post.arch.BaseViewModel
import com.huanchengfly.tieba.post.arch.CommonUiEvent
import com.huanchengfly.tieba.post.arch.ImmutableHolder
import com.huanchengfly.tieba.post.arch.PartialChange
import com.huanchengfly.tieba.post.arch.PartialChangeProducer
import com.huanchengfly.tieba.post.arch.UiEvent
import com.huanchengfly.tieba.post.arch.UiIntent
import com.huanchengfly.tieba.post.arch.UiState
import com.huanchengfly.tieba.post.arch.getOrNull
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

    override fun dispatchEvent(partialChange: UserProfilePartialChange): UiEvent? =
        when (partialChange) {
            is UserProfilePartialChange.Follow.Failure -> CommonUiEvent.Toast(
                App.INSTANCE.getString(
                    R.string.toast_like_failed,
                    partialChange.error.getErrorMessage()
                )
            )

            is UserProfilePartialChange.Unfollow.Failure -> CommonUiEvent.Toast(
                App.INSTANCE.getString(
                    R.string.toast_unlike_failed,
                    partialChange.error.getErrorMessage()
                )
            )

            else -> null
        }

    private object UserProfilePartialChangeProducer :
        PartialChangeProducer<UserProfileUiIntent, UserProfilePartialChange, UserProfileUiState> {
        @OptIn(ExperimentalCoroutinesApi::class)
        override fun toPartialChangeFlow(intentFlow: Flow<UserProfileUiIntent>): Flow<UserProfilePartialChange> =
            merge(
                intentFlow.filterIsInstance<UserProfileUiIntent.Refresh>()
                    .flatMapConcat { it.producePartialChange() },
                intentFlow.filterIsInstance<UserProfileUiIntent.Follow>()
                    .flatMapConcat { it.producePartialChange() },
                intentFlow.filterIsInstance<UserProfileUiIntent.Unfollow>()
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

        private fun UserProfileUiIntent.Follow.producePartialChange(): Flow<UserProfilePartialChange.Follow> =
            TiebaApi.getInstance()
                .followFlow(portrait, tbs)
                .map<FollowBean, UserProfilePartialChange.Follow> {
                    UserProfilePartialChange.Follow.Success
                }
                .onStart { emit(UserProfilePartialChange.Follow.Start) }
                .catch { emit(UserProfilePartialChange.Follow.Failure(it)) }

        private fun UserProfileUiIntent.Unfollow.producePartialChange(): Flow<UserProfilePartialChange.Unfollow> =
            TiebaApi.getInstance()
                .unfollowFlow(portrait, tbs)
                .map<CommonResponse, UserProfilePartialChange.Unfollow> {
                    UserProfilePartialChange.Unfollow.Success
                }
                .onStart { emit(UserProfilePartialChange.Unfollow.Start) }
                .catch { emit(UserProfilePartialChange.Unfollow.Failure(it)) }
    }
}

sealed interface UserProfileUiIntent : UiIntent {
    data class Refresh(
        val uid: Long,
    ) : UserProfileUiIntent

    data class Follow(
        val portrait: String,
        val tbs: String,
    ) : UserProfileUiIntent

    data class Unfollow(
        val portrait: String,
        val tbs: String,
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

    sealed class Follow : UserProfilePartialChange {
        override fun reduce(oldState: UserProfileUiState): UserProfileUiState = when (this) {
            is Start -> oldState.copy(
                user = oldState.user.getOrNull()?.copy(
                    has_concerned = 1
                )?.wrapImmutable(),
                disableButton = true
            )

            is Success -> oldState.copy(
                user = oldState.user.getOrNull()?.copy(
                    has_concerned = 1
                )?.wrapImmutable(),
                disableButton = false
            )

            is Failure -> oldState.copy(
                user = oldState.user.getOrNull()?.copy(
                    has_concerned = 0
                )?.wrapImmutable(),
                disableButton = false
            )
        }

        data object Start : Follow()

        data object Success : Follow()

        data class Failure(
            val error: Throwable,
        ) : Follow()
    }

    sealed class Unfollow : UserProfilePartialChange {
        override fun reduce(oldState: UserProfileUiState): UserProfileUiState = when (this) {
            is Start -> oldState.copy(
                user = oldState.user.getOrNull()?.copy(
                    has_concerned = 0
                )?.wrapImmutable(),
                disableButton = true
            )

            is Success -> oldState.copy(
                user = oldState.user.getOrNull()?.copy(
                    has_concerned = 0
                )?.wrapImmutable(),
                disableButton = false
            )

            is Failure -> oldState.copy(
                user = oldState.user.getOrNull()?.copy(
                    has_concerned = 1
                )?.wrapImmutable(),
                disableButton = false
            )
        }

        data object Start : Unfollow()

        data object Success : Unfollow()

        data class Failure(
            val error: Throwable,
        ) : Unfollow()
    }
}

data class UserProfileUiState(
    val isRefreshing: Boolean = false,
    val error: ImmutableHolder<Throwable>? = null,

    val disableButton: Boolean = false,
    val user: ImmutableHolder<User>? = null,
) : UiState