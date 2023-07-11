package com.huanchengfly.tieba.post.ui.page.main.user

import androidx.compose.runtime.Stable
import com.huanchengfly.tieba.post.api.TiebaApi
import com.huanchengfly.tieba.post.api.models.Profile
import com.huanchengfly.tieba.post.api.retrofit.exception.getErrorMessage
import com.huanchengfly.tieba.post.arch.*
import com.huanchengfly.tieba.post.models.database.Account
import com.huanchengfly.tieba.post.utils.AccountUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
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
        @OptIn(FlowPreview::class)
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
                    .profileFlow(account.uid)
                    .map<Profile, UserPartialChange> { profile ->
                        account.apply {
                            profile.anti?.tbs?.let {
                                tbs = it
                            }
                            portrait = profile.user.portrait
                            intro = profile.user.intro
                            sex = profile.user.sex
                            fansNum = profile.user.fansNum
                            postNum = profile.user.postNum
                            threadNum = profile.user.threadNum
                            concernNum = profile.user.concernNum
                            tbAge = profile.user.tbAge
                            age = profile.user.birthdayInfo?.age
                            birthdayShowStatus = profile.user.birthdayInfo?.birthdayShowStatus
                            birthdayTime = profile.user.birthdayInfo?.birthdayTime
                            constellation = profile.user.birthdayInfo?.constellation
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
    object Refresh : UserUiIntent
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

        object Start : Refresh()

        object NotLogin : Refresh()

        data class Success(val account: Account, val isLocal: Boolean = false) : Refresh()

        data class Failure(val errorMessage: String) : Refresh()
    }
}

data class UserUiState(
    val isLoading: Boolean = false,
    val account: Account? = null
) : UiState

sealed interface UserUiEvent : UiEvent