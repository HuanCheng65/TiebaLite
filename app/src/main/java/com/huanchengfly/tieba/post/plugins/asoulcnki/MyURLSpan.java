package com.huanchengfly.tieba.post.plugins.asoulcnki;

import android.content.Context;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

import androidx.annotation.NonNull;

import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.ui.theme.utils.ThemeUtils;
import com.huanchengfly.tieba.post.utils.UtilsKt;

public class MyURLSpan extends ClickableSpan {
    public String url;
    private Context context;

    public MyURLSpan(Context context, String url) {
        super();
        this.url = url;
        this.context = context;
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
        //context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }
}
