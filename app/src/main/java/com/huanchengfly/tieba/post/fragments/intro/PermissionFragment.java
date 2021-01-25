package com.huanchengfly.tieba.post.fragments.intro;

import android.os.Build;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.ui.intro.fragments.BaseIntroFragment;
import com.huanchengfly.tieba.post.ui.theme.utils.ThemeUtils;
import com.huanchengfly.tieba.post.utils.PermissionUtil;
import com.yanzhenjie.permission.runtime.Permission;

public class PermissionFragment extends BaseIntroFragment {
    @Override
    public int getIconRes() {
        return R.drawable.ic_round_warning;
    }

    @Nullable
    @Override
    protected CharSequence getTitle() {
        return getAttachContext().getString(R.string.title_fragment_permission);
    }

    @Nullable
    @Override
    protected CharSequence getSubtitle() {
        return getAttachContext().getString(R.string.subtitle_fragment_permission);
    }

    @Override
    protected int getIconColor() {
        return ThemeUtils.getColorByAttr(getAttachContext(), R.attr.colorAccent);
    }

    @Override
    protected int getTitleTextColor() {
        return ThemeUtils.getColorByAttr(getAttachContext(), R.attr.colorText);
    }

    @Override
    protected int getSubtitleTextColor() {
        return ThemeUtils.getColorByAttr(getAttachContext(), R.attr.colorTextSecondary);
    }

    @Override
    protected int getCustomLayoutResId() {
        return R.layout.layout_fragment_permission;
    }

    @Override
    protected void initCustomLayout(ViewGroup container) {
        super.initCustomLayout(container);
    }

    @Override
    public boolean onNext() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            PermissionUtil.askPermission(getAttachContext(), data -> next(),
                    new PermissionUtil.Permission(Permission.READ_PHONE_STATE, getString(R.string.tip_permission_phone)),
                    new PermissionUtil.Permission(Permission.Group.STORAGE, getString(R.string.tip_permission_storage)));
            return true;
        }
        return false;
    }
}
