package com.huanchengfly.tieba.api.interfaces;

public interface CommonCallback<T> {
    void onSuccess(T data);

    void onFailure(int code, String error);
}