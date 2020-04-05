package com.huanchengfly.tieba.post.activities;

import android.os.Bundle;

import com.huanchengfly.tieba.post.utils.TiebaUtil;

import me.imid.swipebacklayout.lib.app.SwipeBackActivity;

public class OKSignActivity extends SwipeBackActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TiebaUtil.startSign(this);
        finish();
    }
}
