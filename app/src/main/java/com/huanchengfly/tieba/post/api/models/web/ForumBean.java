package com.huanchengfly.tieba.post.api.models.web;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.huanchengfly.tieba.post.api.adapters.PortraitAdapter;
import com.huanchengfly.tieba.post.api.adapters.VideoInfoAdapter;
import com.huanchengfly.tieba.post.api.adapters.ZyqDefineAdapter;
import com.huanchengfly.tieba.post.api.models.ForumPageBean;
import com.huanchengfly.tieba.post.models.BaseBean;

import java.util.List;

public class ForumBean extends WebBaseBean<ForumBean.ForumDataBean> {
    public static class ForumDataBean {
        @SerializedName("frs_data")
        private FrsDataBean frsData;

        public FrsDataBean getFrsData() {
            return frsData;
        }
    }

    public static class TaskInfoBean {
    }

    public static class FrsThreadBean extends BaseBean {
        private String id;
        private String tid;
        private String title;
        @SerializedName("reply_num")
        private String replyNum;
        @SerializedName("task_info")
        private TaskInfoBean taskInfo;
        @SerializedName("view_num")
        private String viewNum;
        @SerializedName("last_time")
        private String lastTime;
        @SerializedName("last_time_int")
        private String lastTimeInt;
        @SerializedName("create_time")
        private String createTime;
        @SerializedName("is_top")
        private String isTop;
        @SerializedName("is_good")
        private String isGood;
        @SerializedName("is_livepost")
        private String isLivePost;
        @SerializedName("is_ntitle")
        private String isNoTitle;
        @SerializedName(value = "author", alternate = {"user"})
        private ForumPageBean.UserBean author;
        @JsonAdapter(VideoInfoAdapter.class)
        @SerializedName("video_info")
        private ForumPageBean.VideoInfoBean videoInfo;
        private List<MediaBean> media;
        @SerializedName("abstract")
        private String abstracts;
        private AgreeBean agree;

        public AgreeBean getAgree() {
            return agree;
        }

        public String getCreateTime() {
            return createTime;
        }

        public String getAbstracts() {
            return abstracts;
        }

        public List<MediaBean> getMedia() {
            return media;
        }

        public ForumPageBean.VideoInfoBean getVideoInfo() {
            return videoInfo;
        }

        public String getId() {
            return id;
        }

        public String getTid() {
            return tid;
        }

        public String getTitle() {
            return title;
        }

        public String getReplyNum() {
            return replyNum;
        }

        public String getViewNum() {
            return viewNum;
        }

        public String getLastTime() {
            return lastTime;
        }

        public String getLastTimeInt() {
            return lastTimeInt;
        }

        public String getIsTop() {
            return isTop;
        }

        public String getIsGood() {
            return isGood;
        }

        public String getIsNoTitle() {
            return isNoTitle;
        }

        public ForumPageBean.UserBean getAuthor() {
            return author;
        }

        public static class AgreeBean {
            @SerializedName("agree_num")
            private int agreeNum;

            public int getAgreeNum() {
                return agreeNum;
            }
        }
    }

    public static class MediaBean {
        @SerializedName("big_pic")
        private String bigPic;
        @SerializedName("is_gif")
        private boolean isGif;
        private int height;
        private int width;
        @SerializedName("small_pic")
        private String smallPic;
        @SerializedName("static_img")
        private String staticImg;
        private String type;
        @SerializedName("water_pic")
        private String waterPic;

        public String getBigPic() {
            return bigPic;
        }

        public boolean isGif() {
            return isGif;
        }

        public int getHeight() {
            return height;
        }

        public int getWidth() {
            return width;
        }

        public String getSmallPic() {
            return smallPic;
        }

        public String getStaticImg() {
            return staticImg;
        }

        public String getType() {
            return type;
        }

        public String getWaterPic() {
            return waterPic;
        }
    }

    public static class FrsDataBean {
        private ForumPageBean.AntiBean anti;
        private FrsForumBean forum;
        private FrsUserBean user;
        private FrsPageBean page;
        @SerializedName("thread_list")
        private List<FrsThreadBean> threadList;

        public List<FrsThreadBean> getThreadList() {
            return threadList;
        }

        public FrsPageBean getPage() {
            return page;
        }

        public ForumPageBean.AntiBean getAnti() {
            return anti;
        }

        public FrsForumBean getForum() {
            return forum;
        }

        public FrsUserBean getUser() {
            return user;
        }
    }

    public static class ForumAttrBean {
        @SerializedName("zyqtitle")
        private String zyqTitle;
        @JsonAdapter(ZyqDefineAdapter.class)
        @SerializedName("zyqdefine")
        private List<ForumPageBean.ZyqDefineBean> zyqDefine;
        @SerializedName("zyqfriend")
        private List<String> zyqFriend;

        public String getZyqTitle() {
            return zyqTitle;
        }

        public List<ForumPageBean.ZyqDefineBean> getZyqDefine() {
            return zyqDefine;
        }

        public List<String> getZyqFriend() {
            return zyqFriend;
        }
    }

    public static class FrsForumBean {
        private String id;
        private ForumAttrBean attrs;
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
        private boolean isExists;
        @SerializedName("cur_score")
        private String curScore;
        @SerializedName("levelup_score")
        private String levelUpScore;
        @SerializedName("member_num")
        private String memberNum;
        @SerializedName("post_num")
        private String postNum;
        @SerializedName("thread_num")
        private String threadNum;
        private List<ForumPageBean.ManagerBean> managers;
        @SerializedName("good_classify")
        private List<ForumPageBean.GoodClassifyBean> goodClassify;
        private String slogan;
        private String avatar;
        private String tids;
        @SerializedName("sign_in_info")
        private ForumPageBean.ForumBean.SignInInfo signInInfo;

        public List<ForumPageBean.ManagerBean> getManagers() {
            return managers;
        }

        public ForumAttrBean getAttrs() {
            return attrs;
        }

        public String getMemberNum() {
            return memberNum;
        }

        public String getPostNum() {
            return postNum;
        }

        public String getThreadNum() {
            return threadNum;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getIsLike() {
            return isLike;
        }

        public String getUserLevel() {
            return userLevel;
        }

        public String getLevelId() {
            return levelId;
        }

        public String getLevelName() {
            return levelName;
        }

        public boolean isExists() {
            return isExists;
        }

        public String getCurScore() {
            return curScore;
        }

        public String getLevelUpScore() {
            return levelUpScore;
        }

        public List<ForumPageBean.GoodClassifyBean> getGoodClassify() {
            return goodClassify;
        }

        public String getSlogan() {
            return slogan;
        }

        public String getAvatar() {
            return avatar;
        }

        public String getTids() {
            return tids;
        }

        public ForumPageBean.ForumBean.SignInInfo getSignInInfo() {
            return signInInfo;
        }


    }

    public static class FrsUserBean {
        private String id;
        private String name;
        @SerializedName(value = "name_show", alternate = {"nick"})
        private String nameShow;
        @JsonAdapter(PortraitAdapter.class)
        private String portrait;
        @SerializedName("new_user_info")
        private NewUserInfoBean newUserInfo;

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

        public NewUserInfoBean getNewUserInfo() {
            return newUserInfo;
        }
    }

    public static class NewUserInfoBean {
        @SerializedName("user_id")
        private String userId;
        @SerializedName("user_name")
        private String userName;
        @SerializedName("user_nickname")
        private String userNickname;
        @SerializedName("user_sex")
        private int userSex;

        public String getUserId() {
            return userId;
        }

        public String getUserName() {
            return userName;
        }

        public String getUserNickname() {
            return userNickname;
        }

        public int getUserSex() {
            return userSex;
        }
    }

    public static class FrsPageBean {
        @SerializedName("page_size")
        private int pageSize;
        private int offset;
        @SerializedName("current_page")
        private int currentPage;
        @SerializedName("total_count")
        private int totalCount;
        @SerializedName("total_page")
        private int totalPage;
        @SerializedName("cur_good_id")
        private int curGoodId;

        public int getPageSize() {
            return pageSize;
        }

        public int getOffset() {
            return offset;
        }

        public int getCurrentPage() {
            return currentPage;
        }

        public int getTotalCount() {
            return totalCount;
        }

        public int getTotalPage() {
            return totalPage;
        }

        public int getCurGoodId() {
            return curGoodId;
        }
    }
}
