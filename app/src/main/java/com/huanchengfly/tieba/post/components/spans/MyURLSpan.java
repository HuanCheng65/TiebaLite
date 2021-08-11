package com.huanchengfly.tieba.post.components.spans;

import android.content.Context;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;

import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.ui.theme.utils.ThemeUtils;
import com.huanchengfly.tieba.post.utils.NavigationHelper;
import com.huanchengfly.tieba.post.utils.UtilsKt;

public class MyURLSpan extends ClickableSpan {
    public String url;
    private Context context;
    private NavigationHelper navigationHelper;

    public MyURLSpan(Context context, String url) {
        super();
        Log.i("MyURLSpan", "MyURLSpan: " + url);
        this.url = url;
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
        UtilsKt.launchUrl(context, url);
    }
}
