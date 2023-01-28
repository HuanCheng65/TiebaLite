package com.huanchengfly.tieba.post.arch

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

@Stable
class StableHolder<T>(val item: T) {
    operator fun component1(): T = item
}

@Immutable
class ImmutableHolder<T>(val item: T) {
    operator fun component1(): T = item
}

fun <T> wrapStable(item: T): StableHolder<T> = StableHolder(item)

fun <T> wrapImmutable(item: T): ImmutableHolder<T> = ImmutableHolder(item)