package com.huanchengfly.tieba.post.api.interfaces;

public interface CommonAPICallback<T> {
    void onSuccess(T data);

    void onFailure(int code, String error);
}