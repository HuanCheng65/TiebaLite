package com.huanchengfly.tieba.api.models;

import com.google.gson.annotations.SerializedName;
import com.huanchengfly.tieba.post.models.BaseBean;
import com.huanchengfly.utils.GsonUtil;

public class SignResultBean extends BaseBean {
    @SerializedName("user_info")
    private UserInfo userInfo;
    @SerializedName("error_code")
    private String errorCode;
    @SerializedName("error_msg")
    private String errorMsg;
    private long time;

    public String getErrorMsg() {
        return errorMsg;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public int getErrorCode() {
        return Integer.valueOf(errorCode);
    }

    public long getTime() {
        return time;
    }

    @Override
    public String toString() {
        return GsonUtil.getGson().toJson(this);
    }

    public class UserInfo extends BaseBean {
        @SerializedName("user_id")
        private String userId;
        @SerializedName("is_sign_in")
        private String isSignIn;
        @SerializedName("user_sign_rank")
        private String userSignRank;
        @SerializedName("sign_time")
        private String signTime;
        @SerializedName("sign_bonus_point")
        private String signBonusPoint;
        @SerializedName("level_name")
        private String levelName;
        @SerializedName("levelup_score")
        private String levelUpScore;

        public String getUserId() {
            return userId;
        }

        public String getIsSignIn() {
            return isSignIn;
        }

        public String getUserSignRank() {
            return userSignRank;
        }

        public String getSignTime() {
            return signTime;
        }

        public String getSignBonusPoint() {
            return signBonusPoint;
        }

        public String getLevelName() {
            return levelName;
        }

        public String getLevelUpScore() {
            return levelUpScore;
        }
    }
}