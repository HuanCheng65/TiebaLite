package com.huanchengfly.theme.interfaces;

import android.app.Activity;
import android.view.View;

public interface ExtraRefreshable {
    void refreshGlobal(Activity activity);

    void refreshSpecificView(View view);
}
