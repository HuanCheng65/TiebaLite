package com.huanchengfly.tieba.api.models;

import com.google.gson.annotations.SerializedName;
import com.huanchengfly.tieba.post.models.BaseBean;
import com.huanchengfly.tieba.post.models.ErrorBean;

public class MsgBean extends ErrorBean {
    private MessageBean message;

    public MessageBean getMessage() {
        return message;
    }

    public class MessageBean extends BaseBean {
        @SerializedName("replyme")
        private String replyMe;
        @SerializedName("atme")
        private String atMe;
        private String fans;

        public String getReplyMe() {
            return replyMe;
        }

        public String getAtMe() {
            return atMe;
        }

        public String getFans() {
            return fans;
        }
    }
}
