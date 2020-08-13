package com.huanchengfly.tieba.api.models;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.huanchengfly.tieba.api.adapters.MediaAdapter;
import com.huanchengfly.tieba.api.adapters.PortraitAdapter;
import com.huanchengfly.tieba.post.models.BaseBean;
import com.huanchengfly.tieba.post.models.ErrorBean;

import java.util.List;

public class ForumPageBean extends ErrorBean {
    private ForumBean forum;
    private AntiBean anti;
    private UserBean user;
    private PageBean page;
    @SerializedName("thread_list")
    private List<ThreadBean> threadList;
    @SerializedName("user_list")
    private List<UserBean> userList;

    public ForumBean getForum() {
        return forum;
    }

    public ForumPageBean setForum(ForumBean forum) {
        this.forum = forum;
        return this;
    }

    public AntiBean getAnti() {
        return anti;
    }

    public ForumPageBean setAnti(AntiBean anti) {
        this.anti = anti;
        return this;
    }

    public UserBean getUser() {
        return user;
    }

    public void setUser(UserBean user) {
        this.user = user;
    }

    public PageBean getPage() {
        return page;
    }

    public void setPage(PageBean page) {
        this.page = page;
    }

    public List<ThreadBean> getThreadList() {
        return threadList;
    }

    public void setThreadList(List<ThreadBean> threadList) {
        this.threadList = threadList;
    }

    public List<UserBean> getUserList() {
        return userList;
    }

    public void setUserList(List<UserBean> userList) {
        this.userList = userList;
    }

    public static class ZyqDefineBean extends BaseBean {
        private String name;
        private String link;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getLink() {
            return link;
        }

        public void setLink(String link) {
            this.link = link;
        }
    }

    public static class ManagerBean extends BaseBean {
        private String id;
        private String name;

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }

    public static class ForumBean extends BaseBean {
        private String id;
        private String name;
        @SerializedName("is_like")
        private String isLike;
        @SerializedName("user_level")
        private String userLevel;
        @SerializedName("level_id")
        private String levelId;
        @SerializedName("level_name")
        private String levelName;
        @SerializedName("is_exists")
        private String isExists;
        @SerializedName("cur_score")
        private String curScore;
        @SerializedName("levelup_score")
        private String levelUpScore;
        @SerializedName("member_num")
        private String memberNum;
        @SerializedName("post_num")
        private String postNum;
        private List<ManagerBean> managers;
        private String zyqTitle;
        private List<ZyqDefineBean> zyqDefine;
        private List<String> zyqFriend;
        @SerializedName("good_classify")
        private List<GoodClassifyBean> goodClassify;
        private String slogan;
        private String avatar;
        private String tids;
        @SerializedName("sign_in_info")
        private SignInInfo signInInfo;

        public List<ManagerBean> getManagers() {
            return managers;
        }

        public void setManagers(List<ManagerBean> managers) {
            this.managers = managers;
        }

        public String getZyqTitle() {
            return zyqTitle;
        }

        public void setZyqTitle(String zyqTitle) {
            this.zyqTitle = zyqTitle;
        }

        public List<ZyqDefineBean> getZyqDefine() {
            return zyqDefine;
        }

        public void setZyqDefine(List<ZyqDefineBean> zyqDefine) {
            this.zyqDefine = zyqDefine;
        }

        public List<String> getZyqFriend() {
            return zyqFriend;
        }

        public void setZyqFriend(List<String> zyqFriend) {
            this.zyqFriend = zyqFriend;
        }

        public String getMemberNum() {
            return memberNum;
        }

        public void setMemberNum(String memberNum) {
            this.memberNum = memberNum;
        }

        public String getPostNum() {
            return postNum;
        }

        public void setPostNum(String postNum) {
            this.postNum = postNum;
        }

        public SignInInfo getSignInInfo() {
            return signInInfo;
        }

        public void setSignInInfo(SignInInfo signInInfo) {
            this.signInInfo = signInInfo;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getIsLike() {
            return isLike;
        }

        public ForumBean setIsLike(String isLike) {
            this.isLike = isLike;
            return this;
        }

        public String getUserLevel() {
            return userLevel;
        }

        public void setUserLevel(String userLevel) {
            this.userLevel = userLevel;
        }

        public String getLevelId() {
            return levelId;
        }

        public void setLevelId(String levelId) {
            this.levelId = levelId;
        }

        public String getLevelName() {
            return levelName;
        }

        public void setLevelName(String levelName) {
            this.levelName = levelName;
        }

        public String getIsExists() {
            return isExists;
        }

        public void setIsExists(String isExists) {
            this.isExists = isExists;
        }

        public String getCurScore() {
            return curScore;
        }

        public void setCurScore(String curScore) {
            this.curScore = curScore;
        }

        public String getLevelUpScore() {
            return levelUpScore;
        }

        public void setLevelUpScore(String levelUpScore) {
            this.levelUpScore = levelUpScore;
        }

        public List<GoodClassifyBean> getGoodClassify() {
            return goodClassify;
        }

        public void setGoodClassify(List<GoodClassifyBean> goodClassify) {
            this.goodClassify = goodClassify;
        }

        public String getSlogan() {
            return slogan;
        }

        public void setSlogan(String slogan) {
            this.slogan = slogan;
        }

        public String getAvatar() {
            return avatar;
        }

        public void setAvatar(String avatar) {
            this.avatar = avatar;
        }

        public String getTids() {
            return tids;
        }

        public void setTids(String tids) {
            this.tids = tids;
        }

        public static class SignInInfo {
            @SerializedName("user_info")
            private UserInfo userInfo;

            public UserInfo getUserInfo() {
                return userInfo;
            }

            public static class UserInfo {
                @SerializedName("is_sign_in")
                private String isSignIn;

                public String getIsSignIn() {
                    return isSignIn;
                }

                public UserInfo setIsSignIn(String isSignIn) {
                    this.isSignIn = isSignIn;
                    return this;
                }
            }
        }
    }

    public static class GoodClassifyBean extends BaseBean {
        @SerializedName("class_id")
        private String classId;
        @SerializedName("class_name")
        private String className;

        public String getClassId() {
            return classId;
        }

        public String getClassName() {
            return className;
        }
    }

    public static class AntiBean extends BaseBean {
        private String tbs;
        @SerializedName("ifpost")
        private String ifPost;
        @SerializedName("forbid_flag")
        private String forbidFlag;
        @SerializedName("forbid_info")
        private String forbidInfo;

        public String getIfPost() {
            return ifPost;
        }

        public String getForbidFlag() {
            return forbidFlag;
        }

        public String getForbidInfo() {
            return forbidInfo;
        }

        public String getTbs() {
            return tbs;
        }
    }

    public static class UserBean extends BaseBean {
        private String id;
        private String name;
        @SerializedName(value = "name_show", alternate = {"nick"})
        private String nameShow;
        @JsonAdapter(PortraitAdapter.class)
        private String portrait;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getNameShow() {
            return nameShow;
        }

        public void setNameShow(String nameShow) {
            this.nameShow = nameShow;
        }

        public String getPortrait() {
            return portrait;
        }

        public void setPortrait(String portrait) {
            this.portrait = portrait;
        }
    }

    public static class PageBean extends BaseBean {
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
        @SerializedName("cur_good_id")
        private String curGoodId;

        public String getPageSize() {
            return pageSize;
        }

        public void setPageSize(String pageSize) {
            this.pageSize = pageSize;
        }

        public String getOffset() {
            return offset;
        }

        public void setOffset(String offset) {
            this.offset = offset;
        }

        public String getCurrentPage() {
            return currentPage;
        }

        public void setCurrentPage(String currentPage) {
            this.currentPage = currentPage;
        }

        public String getTotalCount() {
            return totalCount;
        }

        public void setTotalCount(String totalCount) {
            this.totalCount = totalCount;
        }

        public String getTotalPage() {
            return totalPage;
        }

        public void setTotalPage(String totalPage) {
            this.totalPage = totalPage;
        }

        public String getHasMore() {
            return hasMore;
        }

        public void setHasMore(String hasMore) {
            this.hasMore = hasMore;
        }

        public String getHasPrev() {
            return hasPrev;
        }

        public void setHasPrev(String hasPrev) {
            this.hasPrev = hasPrev;
        }

        public String getCurGoodId() {
            return curGoodId;
        }

        public void setCurGoodId(String curGoodId) {
            this.curGoodId = curGoodId;
        }
    }

    public static class ThreadBean extends BaseBean {
        private String id;
        private String tid;
        private String title;
        @SerializedName("reply_num")
        private String replyNum;
        @SerializedName("view_num")
        private String viewNum;
        @SerializedName("last_time")
        private String lastTime;
        @SerializedName("last_time_int")
        private String lastTimeInt;
        @SerializedName("create_time")
        private String createTime;
        @SerializedName("agree_num")
        private String agreeNum;
        @SerializedName("is_top")
        private String isTop;
        @SerializedName("is_good")
        private String isGood;
        @SerializedName("is_ntitle")
        private String isNoTitle;
        @SerializedName("author_id")
        private String authorId;
        @SerializedName("video_info")
        private VideoInfoBean videoInfo;
        @JsonAdapter(MediaAdapter.class)
        private List<MediaInfoBean> media;
        @SerializedName("abstract")
        private List<AbstractBean> abstractBeans;
        private String abstractString;

        public String getCreateTime() {
            return createTime;
        }

        public ThreadBean setCreateTime(String createTime) {
            this.createTime = createTime;
            return this;
        }

        public List<MediaInfoBean> getMedia() {
            return media;
        }

        public ThreadBean setMedia(List<MediaInfoBean> media) {
            this.media = media;
            return this;
        }

        public VideoInfoBean getVideoInfo() {
            return videoInfo;
        }

        public ThreadBean setVideoInfo(VideoInfoBean videoInfo) {
            this.videoInfo = videoInfo;
            return this;
        }

        public String getId() {
            return id;
        }

        public ThreadBean setId(String id) {
            this.id = id;
            return this;
        }

        public String getTid() {
            return tid;
        }

        public ThreadBean setTid(String tid) {
            this.tid = tid;
            return this;
        }

        public String getTitle() {
            return title;
        }

        public ThreadBean setTitle(String title) {
            this.title = title;
            return this;
        }

        public String getReplyNum() {
            return replyNum;
        }

        public ThreadBean setReplyNum(String replyNum) {
            this.replyNum = replyNum;
            return this;
        }

        public String getViewNum() {
            return viewNum;
        }

        public ThreadBean setViewNum(String viewNum) {
            this.viewNum = viewNum;
            return this;
        }

        public String getLastTime() {
            return lastTime;
        }

        public ThreadBean setLastTime(String lastTime) {
            this.lastTime = lastTime;
            return this;
        }

        public String getLastTimeInt() {
            return lastTimeInt;
        }

        public ThreadBean setLastTimeInt(String lastTimeInt) {
            this.lastTimeInt = lastTimeInt;
            return this;
        }

        public String getAgreeNum() {
            return agreeNum;
        }

        public ThreadBean setAgreeNum(String agreeNum) {
            this.agreeNum = agreeNum;
            return this;
        }

        public String getIsTop() {
            return isTop;
        }

        public ThreadBean setIsTop(String isTop) {
            this.isTop = isTop;
            return this;
        }

        public String getIsGood() {
            return isGood;
        }

        public ThreadBean setIsGood(String isGood) {
            this.isGood = isGood;
            return this;
        }

        public String getIsNoTitle() {
            return isNoTitle;
        }

        public ThreadBean setIsNoTitle(String isNoTitle) {
            this.isNoTitle = isNoTitle;
            return this;
        }

        public String getAuthorId() {
            return authorId;
        }

        public ThreadBean setAuthorId(String authorId) {
            this.authorId = authorId;
            return this;
        }

        public String getAbstractString() {
            if (abstractString != null) {
                return abstractString;
            }
            if (abstractBeans != null) {
                StringBuilder stringBuilder = new StringBuilder();
                for (AbstractBean abstractBean : abstractBeans) {
                    stringBuilder.append(abstractBean.getText());
                }
                return stringBuilder.toString();
            }
            return null;
        }

        public ThreadBean setAbstractString(String abstractString) {
            this.abstractString = abstractString;
            return this;
        }

        public List<AbstractBean> getAbstractBeans() {
            return abstractBeans;
        }

        public ThreadBean setAbstractBeans(List<AbstractBean> abstractBeans) {
            this.abstractBeans = abstractBeans;
            return this;
        }
    }

    public static class AbstractBean extends BaseBean {
        private String type;
        private String text;

        public AbstractBean(String type, String text) {
            this.type = type;
            this.text = text;
        }

        public String getType() {
            return type;
        }

        public String getText() {
            return text;
        }
    }

    public static class MediaInfoBean extends BaseBean {
        private String type;
        @SerializedName("show_original_btn")
        private String showOriginalBtn;
        @SerializedName("is_long_pic")
        private String isLongPic;
        @SerializedName("is_gif")
        private String isGif;
        @SerializedName("big_pic")
        private String bigPic;
        @SerializedName("src_pic")
        private String srcPic;
        @SerializedName("post_id")
        private String postId;
        @SerializedName("origin_pic")
        private String originPic;

        public String getOriginPic() {
            return originPic;
        }

        public MediaInfoBean setOriginPic(String originPic) {
            this.originPic = originPic;
            return this;
        }

        public String getType() {
            return type;
        }

        public MediaInfoBean setType(String type) {
            this.type = type;
            return this;
        }

        public String getShowOriginalBtn() {
            return showOriginalBtn;
        }

        public MediaInfoBean setShowOriginalBtn(String showOriginalBtn) {
            this.showOriginalBtn = showOriginalBtn;
            return this;
        }

        public String getIsLongPic() {
            return isLongPic;
        }

        public MediaInfoBean setIsLongPic(String isLongPic) {
            this.isLongPic = isLongPic;
            return this;
        }

        public String getBigPic() {
            return bigPic;
        }

        public MediaInfoBean setBigPic(String bigPic) {
            this.bigPic = bigPic;
            return this;
        }

        public String getSrcPic() {
            return srcPic;
        }

        public MediaInfoBean setSrcPic(String srcPic) {
            this.srcPic = srcPic;
            return this;
        }

        public String getIsGif() {
            return isGif;
        }

        public MediaInfoBean setIsGif(String isGif) {
            this.isGif = isGif;
            return this;
        }

        public String getPostId() {
            return postId;
        }

        public MediaInfoBean setPostId(String postId) {
            this.postId = postId;
            return this;
        }
    }

    public static class VideoInfoBean extends BaseBean {
        @SerializedName("video_url")
        private String videoUrl;
        @SerializedName("thumbnail_url")
        private String thumbnailUrl;
        @SerializedName("origin_video_url")
        private String originVideoUrl;

        public String getVideoUrl() {
            return videoUrl;
        }

        public VideoInfoBean setVideoUrl(String videoUrl) {
            this.videoUrl = videoUrl;
            return this;
        }

        public String getThumbnailUrl() {
            return thumbnailUrl;
        }

        public String getOriginVideoUrl() {
            return originVideoUrl;
        }
    }
}
