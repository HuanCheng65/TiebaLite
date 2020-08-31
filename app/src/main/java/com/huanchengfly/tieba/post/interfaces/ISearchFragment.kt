package com.huanchengfly.tieba.post.interfaces

interface ISearchFragment {
    fun setKeyword(
            keyword: String?,
            needRefresh: Boolean = true
    )
}