package com.huanchengfly.tieba.api.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SearchPostBean {
    @SerializedName("error_code")
    private String errorCode;
    @SerializedName("error_msg")
    private String errorMsg;
    private PageBean page;
    @SerializedName("post_list")
    private List<ThreadInfoBean> postList;

    public PageBean getPage() {
        return page;
    }

    public List<ThreadInfoBean> getPostList() {
        return postList;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public static class PageBean {
        @SerializedName("page_size")
        private String pageSize;
        private String offset;
        @SerializedName("current_page")
        private String currentPage;
        @SerializedName("total_count")
        private String totalCount;
        @SerializedName("total_page")
        private String totalPage;
        @SerializedName("has_more")
        private String hasMore;
        @SerializedName("has_prev")
        private String hasPrev;

        public String getPageSize() {
            return pageSize;
        }

        public String getOffset() {
            return offset;
        }

        public String getCurrentPage() {
            return currentPage;
        }

        public String getTotalCount() {
            return totalCount;
        }

        public String getTotalPage() {
            return totalPage;
        }

        public String getHasMore() {
            return hasMore;
        }

        public String getHasPrev() {
            return hasPrev;
        }
    }

    public static class ThreadInfoBean {
        private String tid;
        private String pid;
        private String title;
        private String content;
        private String time;
        @SerializedName("fname")
        private String forumName;
        private AuthorBean author;

        public AuthorBean getAuthor() {
            return author;
        }

        public String getForumName() {
            return forumName;
        }

        public String getTid() {
            return tid;
        }

        public String getPid() {
            return pid;
        }

        public String getTitle() {
            return title;
        }

        public String getContent() {
            return content;
        }

        public String getTime() {
            return time;
        }
    }

    public static class AuthorBean {
        private String name;
        @SerializedName("name_show")
        private String nameShow;

        public String getName() {
            return name;
        }

        public String getNameShow() {
            return nameShow;
        }
    }
}
