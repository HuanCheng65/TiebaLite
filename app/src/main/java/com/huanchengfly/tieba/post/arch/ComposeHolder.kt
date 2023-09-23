package com.huanchengfly.tieba.post.arch

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@Stable
class StableHolder<T>(val item: T) {
    operator fun component1(): T = item
}

@Immutable
class ImmutableHolder<T>(val item: T) {
    operator fun component1(): T = item

    @Stable
    fun get(): T = item

    @Stable
    fun <R> get(getter: T.() -> R): R {
        return getter(item)
    }

    fun <R> getImmutable(getter: T.() -> R): ImmutableHolder<R> {
        return wrapImmutable(getter(item))
    }

    fun <R> getImmutableList(getter: T.() -> List<R>): ImmutableList<ImmutableHolder<R>> {
        return getter(item).map { wrapImmutable(it) }.toImmutableList()
    }

    @Stable
    fun isNotNull(): Boolean = item != null

    @Stable
    fun <R> isNotNull(getter: T.() -> R): Boolean = getter(item) != null
}

fun <T> wrapStable(item: T): StableHolder<T> = StableHolder(item)

fun <T> wrapImmutable(item: T): ImmutableHolder<T> = ImmutableHolder(item)

@JvmName("wrapImmutableExt")
fun <T> T.wrapImmutable(): ImmutableHolder<T> = ImmutableHolder(this)

fun <T> List<T>.wrapImmutable(): ImmutableList<ImmutableHolder<T>> =
    map { wrapImmutable(it) }.toImmutableList()