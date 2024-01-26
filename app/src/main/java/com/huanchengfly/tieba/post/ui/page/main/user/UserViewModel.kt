package com.huanchengfly.tieba.post.ui.page.main.user

import androidx.compose.runtime.Stable
import com.huanchengfly.tieba.post.api.TiebaApi
import com.huanchengfly.tieba.post.api.models.protos.profile.ProfileResponse
import com.huanchengfly.tieba.post.api.retrofit.exception.getErrorMessage
import com.huanchengfly.tieba.post.arch.BaseViewModel
import com.huanchengfly.tieba.post.arch.CommonUiEvent
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
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

@Stable
@HiltViewModel
class UserViewModel @Inject constructor() : BaseViewModel<UserUiIntent, UserPartialChange, UserUiState, UserUiEvent>() {
    override fun createInitialState(): UserUiState =
        UserUiState()

    override fun createPartialChangeProducer(): PartialChangeProducer<UserUiIntent, UserPartialChange, UserUiState> =
        UserPartialChangeProducer

    override fun dispatchEvent(partialChange: UserPartialChange): UiEvent? =
        when (partialChange) {
            is UserPartialChange.Refresh.Failure -> CommonUiEvent.Toast(partialChange.errorMessage)
            else -> null
        }

    object UserPartialChangeProducer :PartialChangeProducer<UserUiIntent, UserPartialChange, UserUiState> {
        @OptIn(ExperimentalCoroutinesApi::class)
        override fun toPartialChangeFlow(intentFlow: Flow<UserUiIntent>): Flow<UserPartialChange> =
            merge(
                intentFlow.filterIsInstance<UserUiIntent.Refresh>().flatMapConcat { it.toPartialChangeFlow() }
            )

        private fun UserUiIntent.Refresh.toPartialChangeFlow(): Flow<UserPartialChange> {
            val account = AccountUtil.currentAccount
            return if (account == null) {
                listOf(UserPartialChange.Refresh.NotLogin).asFlow()
            } else {
                TiebaApi.getInstance()
                    .userProfileFlow(account.uid.toLong())
                    .map<ProfileResponse, UserPartialChange> { profile ->
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
                            loadSuccess = true
                            updateAll("uid = ?", uid)
                        }
                        UserPartialChange.Refresh.Success(account = account)
                    }
                    .onStart {
                        emit(UserPartialChange.Refresh.Start)
                        if (account.loadSuccess) {
                            emit(
                                UserPartialChange.Refresh.Success(
                                    account = account,
                                    isLocal = true
                                )
                            )
                        }
                    }
                    .catch {
                        it.printStackTrace()
                        emit(UserPartialChange.Refresh.Failure(errorMessage = it.getErrorMessage()))
                    }
            }
        }
    }
}

sealed interface UserUiIntent : UiIntent {
    data object Refresh : UserUiIntent
}

sealed interface UserPartialChange : PartialChange<UserUiState> {
    sealed class Refresh : UserPartialChange {
        override fun reduce(oldState: UserUiState): UserUiState =
            when (this) {
                Start -> oldState.copy(isLoading = true)
                NotLogin -> oldState.copy(isLoading = false, account = null)
                is Success -> {
                    if (isLocal) {
                        oldState.copy(account = account)
                    } else {
                        oldState.copy(isLoading = false, account = account)
                    }
                }
                is Failure -> oldState.copy(isLoading = false)
            }

        data object Start : Refresh()

        data object NotLogin : Refresh()

        data class Success(val account: Account, val isLocal: Boolean = false) : Refresh()

        data class Failure(val errorMessage: String) : Refresh()
    }
}

data class UserUiState(
    val isLoading: Boolean = false,
    val account: Account? = null
) : UiState

sealed interface UserUiEvent : UiEvent