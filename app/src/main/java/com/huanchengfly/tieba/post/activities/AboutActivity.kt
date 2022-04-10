package com.huanchengfly.tieba.post.activities;

import android.animation.LayoutTransition;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.ui.about.AboutPage;
import com.huanchengfly.tieba.post.ui.theme.utils.ThemeUtils;
import com.huanchengfly.tieba.post.utils.ThemeUtil;
import com.huanchengfly.tieba.post.utils.VersionUtil;

public class AboutActivity extends BaseActivity {
    @Override
    public int getLayoutId() {
        return R.layout.activity_about;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeUtil.setTranslucentThemeBackground(findViewById(R.id.background));
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        RelativeLayout mainView = (RelativeLayout) findViewById(R.id.main);
        View headerView = View.inflate(this, R.layout.header_about, null);
        ((ViewGroup) headerView).setLayoutTransition(new LayoutTransition());
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.title_about);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        int colorIcon = ThemeUtils.getColorByAttr(this, R.attr.colorAccent);
        new AboutPage(this)
                .setHeaderView(headerView)
                .addTitle("应用信息", colorIcon)
                .addItem(new AboutPage.Item("当前版本", VersionUtil.getVersionName(this), R.drawable.ic_round_info, colorIcon))
                .addItem(new AboutPage.Item("源代码").setIcon(R.drawable.ic_codepen, colorIcon).setOnClickListener(v -> WebViewActivity.launch(v.getContext(), "https://github.com/HuanCheng65/TiebaLite")))
                .into(mainView);
    }
}