package com.huanchengfly.tieba.post.components.spans;

import android.content.Context;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

import androidx.annotation.NonNull;

import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.ui.theme.utils.ThemeUtils;
import com.huanchengfly.tieba.post.utils.NavigationHelper;

public class MyUserSpan extends ClickableSpan {
    public String uid;
    private Context context;
    private NavigationHelper navigationHelper;

    public MyUserSpan(Context context, String uid) {
        super();
        this.uid = uid;
        this.context = context;
        this.navigationHelper = NavigationHelper.newInstance(context);
    }

    @Override
    public void updateDrawState(@NonNull TextPaint ds) {
        super.updateDrawState(ds);
        ds.setColor(ThemeUtils.getColorByAttr(this.context, R.attr.colorAccent));
        ds.setUnderlineText(false);
    }

    @Override
    public void onClick(@NonNull View view) {
        navigationHelper.navigationByData(NavigationHelper.ACTION_USER_BY_UID, uid);
    }
}
