package com.huanchengfly.tieba.post.api.models;

import com.huanchengfly.tieba.post.models.BaseBean;

public class WebUploadPicBean extends BaseBean {
    private String errorMsg;
    private String imageBaseSrc;
    private String imageInfo;
    private String imageSrc;

    public String getErrorMsg() {
        return errorMsg;
    }

    public String getImageBaseSrc() {
        return imageBaseSrc;
    }

    public String getImageInfo() {
        return imageInfo;
    }

    public String getImageSrc() {
        return imageSrc;
    }
}
