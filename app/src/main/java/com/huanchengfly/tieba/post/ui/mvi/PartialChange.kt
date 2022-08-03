package com.huanchengfly.tieba.post.ui.mvi

interface PartialChange<State> {
    fun reduce(oldState: State): State
}