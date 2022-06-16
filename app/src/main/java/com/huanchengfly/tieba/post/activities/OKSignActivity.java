package com.huanchengfly.tieba.post.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.huanchengfly.tieba.post.utils.TiebaUtil;

public class OKSignActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TiebaUtil.startSign(this);
        finish();
    }
}
