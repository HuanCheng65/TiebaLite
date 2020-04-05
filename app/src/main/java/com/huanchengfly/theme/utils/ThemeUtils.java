package com.huanchengfly.theme.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.WrapperListAdapter;

import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.huanchengfly.theme.interfaces.ExtraRefreshable;
import com.huanchengfly.theme.interfaces.ThemeSwitcher;
import com.huanchengfly.theme.interfaces.Tintable;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ThemeUtils {
    private static ThemeSwitcher mThemeSwitcher;
    private static Field sRecycler;
    private static Method sRecycleViewClearMethod;
    private static Field sRecyclerBin;
    private static Method sListViewClearMethod;

    public static Drawable tintDrawable(Drawable drawable, ColorStateList colorStateList) {
        if (drawable == null) return null;
        Drawable wrapper = DrawableCompat.wrap(drawable.mutate());
        DrawableCompat.setTintList(wrapper, colorStateList);
        DrawableCompat.setTintMode(drawable, PorterDuff.Mode.SRC_IN);
        return drawable;
    }

    public static Drawable tintDrawable(Drawable drawable, @ColorInt int color) {
        if (drawable == null) return null;
        Drawable wrapper = DrawableCompat.wrap(drawable.mutate());
        DrawableCompat.setTint(wrapper, color);
        DrawableCompat.setTintMode(drawable, PorterDuff.Mode.SRC_IN);
        return drawable;
    }

    public static void init(ThemeSwitcher themeSwitcher) {
        mThemeSwitcher = themeSwitcher;
    }

    @ColorInt
    public static int getColorByAttr(Context context, @AttrRes int attrId) {
        if (mThemeSwitcher == null) {
            throw new IllegalStateException("ThemeSwitcher is uninitialized.");
        }
        return mThemeSwitcher.getColorByAttr(context, attrId);
    }

    @ColorInt
    public static int getColorById(Context context, @ColorRes int colorId) {
        if (mThemeSwitcher == null) {
            throw new IllegalStateException("ThemeSwitcher is uninitialized.");
        }
        return mThemeSwitcher.getColorById(context, colorId);
    }

    public static void refreshUI(Context context) {
        refreshUI(context, null);
    }

    public static Activity getWrapperActivity(Context context) {
        if (context instanceof Activity) {
            return (Activity) context;
        } else if (context instanceof ContextWrapper) {
            return getWrapperActivity(((ContextWrapper) context).getBaseContext());
        }
        return null;
    }

    public static void refreshUI(Context context, ExtraRefreshable extraRefreshable) {
        Activity activity = getWrapperActivity(context);
        if (activity != null) {
            if (extraRefreshable != null) {
                extraRefreshable.refreshGlobal(activity);
            }
            View rootView = activity.getWindow().getDecorView().findViewById(android.R.id.content);
            refreshView(rootView, extraRefreshable);
        }
    }

    @SuppressLint("PrivateApi")
    private static void refreshView(View view, ExtraRefreshable extraRefreshable) {
        if (view == null) return;
        view.destroyDrawingCache();
        if (view instanceof Tintable) {
            ((Tintable) view).tint();
            if (view instanceof ViewGroup) {
                for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                    refreshView(((ViewGroup) view).getChildAt(i), extraRefreshable);
                }
            }
        } else {
            if (extraRefreshable != null) {
                extraRefreshable.refreshSpecificView(view);
            }
            if (view instanceof AbsListView) {
                try {
                    if (sRecyclerBin == null) {
                        sRecyclerBin = AbsListView.class.getDeclaredField("mRecycler");
                        sRecyclerBin.setAccessible(true);
                    }
                    if (sListViewClearMethod == null) {
                        sListViewClearMethod = Class.forName("android.widget.AbsListView$RecycleBin")
                                .getDeclaredMethod("clear");
                        sListViewClearMethod.setAccessible(true);
                    }
                    sListViewClearMethod.invoke(sRecyclerBin.get(view));
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                ListAdapter adapter = ((AbsListView) view).getAdapter();
                while (adapter instanceof WrapperListAdapter) {
                    adapter = ((WrapperListAdapter) adapter).getWrappedAdapter();
                }
                if (adapter instanceof BaseAdapter) {
                    ((BaseAdapter) adapter).notifyDataSetChanged();
                }
            }
            if (view instanceof RecyclerView) {
                try {
                    sRecycler = RecyclerView.class.getDeclaredField("mRecycler");
                    sRecycler.setAccessible(true);
                    sRecycleViewClearMethod = Class.forName("androidx.recyclerview.widget.RecyclerView$Recycler")
                            .getDeclaredMethod("clear");
                    sRecycleViewClearMethod.setAccessible(true);
                    sRecycleViewClearMethod.invoke(sRecycler.get(view));
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                RecyclerView recyclerView = (RecyclerView) view;
                recyclerView.getRecycledViewPool().clear();
                for (int i = 0; i < recyclerView.getItemDecorationCount(); i++) {
                    RecyclerView.ItemDecoration itemDecoration = recyclerView.getItemDecorationAt(i);
                    if (itemDecoration instanceof Tintable) {
                        ((Tintable) itemDecoration).tint();
                    }
                }
                recyclerView.invalidateItemDecorations();
            }
            if (view instanceof ViewGroup) {
                for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                    refreshView(((ViewGroup) view).getChildAt(i), extraRefreshable);
                }
            }
        }
    }
}
