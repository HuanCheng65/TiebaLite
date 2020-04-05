package com.huanchengfly.tieba.post.models;

import android.os.Parcel;
import android.os.Parcelable;

public class PhotoViewBean extends BaseBean implements Parcelable {
    public static final Creator<PhotoViewBean> CREATOR = new Creator<PhotoViewBean>() {
        @Override
        public PhotoViewBean createFromParcel(Parcel in) {
            return new PhotoViewBean(in);
        }

        @Override
        public PhotoViewBean[] newArray(int size) {
            return new PhotoViewBean[size];
        }
    };

    private String url;
    private String originUrl;
    private boolean longPic;
    private String index;
    private boolean gif;

    public PhotoViewBean(String url, String originUrl, boolean longPic, String index, boolean gif) {
        this.url = url;
        this.originUrl = originUrl;
        this.longPic = longPic;
        this.index = index;
        this.gif = gif;
    }

    public PhotoViewBean(String url, String originUrl, boolean longPic, String index) {
        this(url, originUrl, longPic, index, false);
    }

    public PhotoViewBean(String url, String originUrl, boolean longPic) {
        this(url, originUrl, longPic, null);
    }

    public PhotoViewBean(String url, boolean longPic) {
        this(url, url, longPic);
    }

    public PhotoViewBean(String url) {
        this(url, true);
    }

    protected PhotoViewBean(Parcel in) {
        url = in.readString();
        originUrl = in.readString();
        longPic = in.readInt() != 0;
        index = in.readString();
    }

    public boolean isGif() {
        return gif;
    }

    public PhotoViewBean setGif(boolean gif) {
        this.gif = gif;
        return this;
    }

    public String getIndex() {
        return index;
    }

    public PhotoViewBean setIndex(String index) {
        this.index = index;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public PhotoViewBean setUrl(String url) {
        this.url = url;
        return this;
    }

    public String getOriginUrl() {
        return originUrl;
    }

    public PhotoViewBean setOriginUrl(String originUrl) {
        this.originUrl = originUrl;
        return this;
    }

    public boolean isLongPic() {
        return longPic;
    }

    public PhotoViewBean setLongPic(boolean longPic) {
        this.longPic = longPic;
        return this;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(url);
        dest.writeString(originUrl);
        dest.writeInt(longPic ? 1 : 0);
        dest.writeString(index);
    }
}
