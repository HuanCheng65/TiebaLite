package com.huanchengfly.tieba.post.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;

import androidx.appcompat.view.menu.MenuPopupHelper;
import androidx.appcompat.widget.ListPopupWindow;
import androidx.appcompat.widget.MenuPopupWindow;
import androidx.appcompat.widget.PopupMenu;

import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.ui.common.theme.utils.ThemeUtils;

import java.lang.reflect.Field;

@SuppressLint("RestrictedApi")
public class PopupUtil {
    private PopupUtil() {
    }

    public static void replaceBackground(ListPopupWindow listPopupWindow) {
        try {
            Field contextField = ListPopupWindow.class.getDeclaredField("mContext");
            contextField.setAccessible(true);
            Context context = (Context) contextField.get(listPopupWindow);
            if (ThemeUtil.THEME_TRANSLUCENT.equals(ThemeUtil.getTheme())) {
                listPopupWindow.setBackgroundDrawable(ThemeUtils.tintDrawable(context.getDrawable(R.drawable.bg_popup), context.getResources().getColor(R.color.theme_color_background_dark)));
            } else {
                listPopupWindow.setBackgroundDrawable(ThemeUtils.tintDrawable(context.getDrawable(R.drawable.bg_popup), ThemeUtils.getColorByAttr(context, R.attr.colorCard)));
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private static void replaceBackground(PopupMenu popupMenu) {
        try {
            Field contextField = PopupMenu.class.getDeclaredField("mContext");
            contextField.setAccessible(true);
            Context context = (Context) contextField.get(popupMenu);
            Field field = PopupMenu.class.getDeclaredField("mPopup");
            field.setAccessible(true);
            MenuPopupHelper menuPopupHelper = (MenuPopupHelper) field.get(popupMenu);
            Object obj = menuPopupHelper.getPopup();
            Field popupField = obj.getClass().getDeclaredField("mPopup");
            popupField.setAccessible(true);
            MenuPopupWindow menuPopupWindow = (MenuPopupWindow) popupField.get(obj);
            if (ThemeUtil.THEME_TRANSLUCENT.equals(ThemeUtil.getTheme())) {
                menuPopupWindow.setBackgroundDrawable(ThemeUtils.tintDrawable(context.getDrawable(R.drawable.bg_popup), context.getResources().getColor(R.color.white)));
            } else {
                menuPopupWindow.setBackgroundDrawable(ThemeUtils.tintDrawable(context.getDrawable(R.drawable.bg_popup), ThemeUtils.getColorByAttr(context, R.attr.colorCard)));
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static PopupMenu create(View anchor) {
        PopupMenu popupMenu = new PopupMenu(anchor.getContext(), anchor);
        replaceBackground(popupMenu);
        return popupMenu;
    }
}
