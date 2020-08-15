package com.huanchengfly.tieba.post.interfaces;

import com.huanchengfly.tieba.post.api.models.UploadResultBean;

public interface InsertPhotoListener {
    void onInsert(UploadResultBean.UploadInfo uploadInfo);
}
