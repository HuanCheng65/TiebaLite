package com.huanchengfly.tieba.post.interfaces;

public interface ReplyContentCallback {
    void onSuccess(String data);

    void onStart(int total);

    void onProgress(int current, int total);

    void onFailure(String error);
}
