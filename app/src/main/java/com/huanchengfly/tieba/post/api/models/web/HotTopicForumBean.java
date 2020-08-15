package com.huanchengfly.tieba.post.api.models.web;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class HotTopicForumBean extends WebBaseBean<HotTopicForumBean.HotTopicForumDataBean> {
    public static class HotTopicForumDataBean {
        @SerializedName("forum_list")
        private ForumListBean forumList;
        @SerializedName("pk_info")
        private PkInfoBean pkInfo;

        public ForumListBean getForumList() {
            return forumList;
        }

        public PkInfoBean getPkInfo() {
            return pkInfo;
        }
    }

    public static class ForumListBean {
        private List<ForumBean> output;

        public List<ForumBean> getOutput() {
            return output;
        }
    }

    public static class PkInfoBean {
        private List<PkInfoRetBean> ret;

        public List<PkInfoRetBean> getRet() {
            return ret;
        }
    }

    public static class PkInfoRetBean {
        @SerializedName("create_time")
        private String createTime;
        @SerializedName("module_name")
        private String moduleName;
        @SerializedName("module_type")
        private String moduleType;
        private PkPicBean pics;
        @SerializedName("module_type")
        private PkPicBean picUrls;
        @SerializedName("has_selected")
        private boolean hasSelected;
        @SerializedName("num_coefficient")
        private String numCoefficient;
        @SerializedName("pk_desc_1")
        private String pkDesc1;
        @SerializedName("pk_desc_2")
        private String pkDesc2;
        @SerializedName("pk_desc_3")
        private String pkDesc3;
        @SerializedName("pk_desc_4")
        private String pkDesc4;
        @SerializedName("pk_id")
        private String pkId;
        @SerializedName("pk_num_1")
        private String pkNum1;
        @SerializedName("pk_num_2")
        private String pkNum2;
        @SerializedName("pk_num_3")
        private String pkNum3;
        @SerializedName("pk_num_4")
        private String pkNum4;
        @SerializedName("selected_index")
        private String selectedIndex;
        private String title;
        @SerializedName("topic_id")
        private String topicId;

        public String getCreateTime() {
            return createTime;
        }

        public String getModuleName() {
            return moduleName;
        }

        public String getModuleType() {
            return moduleType;
        }

        public PkPicBean getPics() {
            return pics;
        }

        public PkPicBean getPicUrls() {
            return picUrls;
        }

        public boolean isHasSelected() {
            return hasSelected;
        }

        public String getNumCoefficient() {
            return numCoefficient;
        }

        public String getPkDesc1() {
            return pkDesc1;
        }

        public String getPkDesc2() {
            return pkDesc2;
        }

        public String getPkDesc3() {
            return pkDesc3;
        }

        public String getPkDesc4() {
            return pkDesc4;
        }

        public String getPkId() {
            return pkId;
        }

        public String getPkNum1() {
            return pkNum1;
        }

        public String getPkNum2() {
            return pkNum2;
        }

        public String getPkNum3() {
            return pkNum3;
        }

        public String getPkNum4() {
            return pkNum4;
        }

        public String getSelectedIndex() {
            return selectedIndex;
        }

        public String getTitle() {
            return title;
        }

        public String getTopicId() {
            return topicId;
        }
    }

    public static class ForumBean {

    }

    public static class PkPicBean {
        @SerializedName("pk_icon_1")
        private String pkIcon1;
        @SerializedName("pk_icon_2")
        private String pkIcon2;
        @SerializedName("pk_icon_after_1")
        private String pkIconAfter1;
        @SerializedName("pk_icon_after_2")
        private String pkIconAfter2;

        public String getPkIcon1() {
            return pkIcon1;
        }

        public String getPkIcon2() {
            return pkIcon2;
        }

        public String getPkIconAfter1() {
            return pkIconAfter1;
        }

        public String getPkIconAfter2() {
            return pkIconAfter2;
        }
    }
}
