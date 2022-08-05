package com.huanchengfly.tieba.post.arch

interface PartialChange<State : UiState> {
    fun reduce(oldState: State): State
}