package com.huanchengfly.tieba.api.models

import com.google.gson.annotations.SerializedName

class SearchPostBean {
    @SerializedName("error_code")
    val errorCode: String? = null

    @SerializedName("error_msg")
    val errorMsg: String? = null
    val page: PageBean? = null

    @SerializedName("post_list")
    val postList: List<ThreadInfoBean>? = null

    class PageBean {
        @SerializedName("page_size")
        val pageSize: String? = null
        val offset: String? = null

        @SerializedName("current_page")
        val currentPage: String? = null

        @SerializedName("total_count")
        val totalCount: String? = null

        @SerializedName("total_page")
        val totalPage: String? = null

        @SerializedName("has_more")
        val hasMore: String? = null

        @SerializedName("has_prev")
        val hasPrev: String? = null

    }

    class ThreadInfoBean {
        val tid: String? = null
        val pid: String? = null
        val title: String? = null
        val content: String? = null
        val time: String? = null

        @SerializedName("fname")
        val forumName: String? = null
        val author: AuthorBean? = null

    }

    class AuthorBean {
        val name: String? = null

        @SerializedName("name_show")
        val nameShow: String? = null

    }
}