package com.huanchengfly.tieba.post.interfaces;

import com.huanchengfly.tieba.api.models.UploadResultBean;

public interface InsertPhotoListener {
    void onInsert(UploadResultBean.UploadInfo uploadInfo);
}
