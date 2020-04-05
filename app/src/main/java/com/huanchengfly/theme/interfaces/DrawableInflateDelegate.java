package com.huanchengfly.theme.interfaces;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

public interface DrawableInflateDelegate<T extends Drawable> {
    T inflateDrawable(Context context, XmlPullParser parser, AttributeSet attrs) throws IOException, XmlPullParserException;
}
