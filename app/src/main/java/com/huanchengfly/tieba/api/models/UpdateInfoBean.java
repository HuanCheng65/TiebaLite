package com.huanchengfly.tieba.api.models;

import com.google.gson.annotations.SerializedName;
import com.huanchengfly.tieba.post.models.BaseBean;

import java.util.List;

public class UpdateInfoBean extends BaseBean {
    @SerializedName("gruops")
    private List<GroupInfo> groups;
    private List<SupportmentBean> supportment;

    public List<SupportmentBean> getSupportment() {
        return supportment;
    }

    public List<GroupInfo> getGroups() {
        return groups;
    }

    public static class SupportmentBean {
        private String id;
        private String title;
        private String subtitle;
        @SerializedName("expire_time")
        private long expireTime;
        private IconBean icon;
        private ActionBean action;

        public String getId() {
            return id;
        }

        public long getExpireTime() {
            return expireTime;
        }

        public String getTitle() {
            return title;
        }

        public String getSubtitle() {
            return subtitle;
        }

        public IconBean getIcon() {
            return icon;
        }

        public ActionBean getAction() {
            return action;
        }

        public static class IconBean {
            public static final int TYPE_RESOURCE = 0;
            public static final int TYPE_IMAGE = 1;
            private int type;
            private String id;
            private String url;

            public int getType() {
                return type;
            }

            public String getId() {
                return id;
            }

            public String getUrl() {
                return url;
            }
        }

        public static class ActionBean {
            public static final int TYPE_LINK = 0;
            public static final int TYPE_IMAGE = 1;
            private int type;
            private String url;

            public int getType() {
                return type;
            }

            public String getUrl() {
                return url;
            }
        }
    }

    public static class WebDiskBean {
        private String link;
        private String name;
        private String password;

        public String getLink() {
            return link;
        }

        public String getName() {
            return name;
        }

        public String getPassword() {
            return password;
        }
    }

    public class GroupInfo {
        private String type;
        private String name;
        @SerializedName("qq_group_key")
        private String QGroupKey;
        @SerializedName("qq_group_number")
        private String QGroupNumber;
        private String link;
        private boolean enabled;

        public String getType() {
            return type;
        }

        public String getQGroupNumber() {
            return QGroupNumber;
        }

        public String getQGroupKey() {
            return QGroupKey;
        }

        public String getName() {
            return name;
        }

        public String getLink() {
            return link;
        }

        public boolean isEnabled() {
            return enabled;
        }
    }
}
