package com.huanchengfly.tieba.post.arch

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

@Stable
class StableHolder<T>(val item: T) {
    operator fun component1(): T = item

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as StableHolder<*>

        if (item != other.item) return false

        return true
    }

    override fun hashCode(): Int {
        return item?.hashCode() ?: 0
    }
}

@Immutable
class ImmutableHolder<T>(val item: T) {
    operator fun component1(): T = item
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ImmutableHolder<*>

        if (item != other.item) return false

        return true
    }

    override fun hashCode(): Int {
        return item?.hashCode() ?: 0
    }
}

fun <T> wrapStable(item: T): StableHolder<T> = StableHolder(item)

fun <T> wrapImmutable(item: T): ImmutableHolder<T> = ImmutableHolder(item)

fun <T> List<T>.wrapImmutable(): List<ImmutableHolder<T>> = map { wrapImmutable(it) }