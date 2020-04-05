package com.huanchengfly.tieba.post.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class ImageInfoBean extends BaseBean implements Parcelable {
    public static final Creator<ImageInfoBean> CREATOR = new Creator<ImageInfoBean>() {
        @Override
        public ImageInfoBean createFromParcel(Parcel in) {
            return new ImageInfoBean(in);
        }

        @Override
        public ImageInfoBean[] newArray(int size) {
            return new ImageInfoBean[size];
        }
    };
    private String url;
    private String size;
    private boolean longPic;

    public ImageInfoBean(String url, String size, boolean longPic) {
        this.url = url;
        this.size = size;
        this.longPic = longPic;
    }

    public ImageInfoBean(String url, boolean longPic) {
        this(url, "", longPic);
    }

    public ImageInfoBean(String url, String size) {
        this(url, size, false);
    }

    public ImageInfoBean(String url) {
        this(url, "", false);
    }

    protected ImageInfoBean(Parcel in) {
        url = in.readString();
        size = in.readString();
        longPic = in.readInt() != 0;
    }

    public static List<ImageInfoBean> asList(List<String> strings) {
        List<ImageInfoBean> list = new ArrayList<>();
        for (String url : strings) {
            list.add(new ImageInfoBean(url, true));
        }
        return list;
    }

    public String getUrl() {
        return url;
    }

    public ImageInfoBean setUrl(String url) {
        this.url = url;
        return this;
    }

    public boolean isLongPic() {
        return longPic;
    }

    public ImageInfoBean setLongPic(boolean longPic) {
        this.longPic = longPic;
        return this;
    }

    public String getSize() {
        return size;
    }

    public ImageInfoBean setSize(String size) {
        this.size = size;
        return this;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(url);
        dest.writeString(size);
        dest.writeInt(longPic ? 1 : 0);
    }
}
