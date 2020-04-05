package com.huanchengfly.tieba.post.fragments;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.MenuRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.navigation.NavigationView;
import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.interfaces.InitMenuCallback;

public class MenuDialogFragment extends BaseBottomSheetDialogFragment implements NavigationView.OnNavigationItemSelectedListener {
    private int menuRes;
    private String title;
    private TextView titleView;
    private NavigationView.OnNavigationItemSelectedListener onNavigationItemSelectedListener;
    private NavigationView navigationView;
    private InitMenuCallback initMenuCallback;

    public MenuDialogFragment() {
    }

    public static MenuDialogFragment newInstance(@MenuRes int menuRes, String title) {
        MenuDialogFragment fragment = new MenuDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("menuRes", menuRes);
        bundle.putString("title", title);
        fragment.setArguments(bundle);
        return fragment;
    }

    public InitMenuCallback getInitMenuCallback() {
        return initMenuCallback;
    }

    public MenuDialogFragment setInitMenuCallback(InitMenuCallback initMenuCallback) {
        this.initMenuCallback = initMenuCallback;
        return this;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            menuRes = bundle.getInt("menuRes", -1);
            title = bundle.getString("title", null);
        }
    }

    public NavigationView.OnNavigationItemSelectedListener getOnNavigationItemSelectedListener() {
        return onNavigationItemSelectedListener;
    }

    public MenuDialogFragment setOnNavigationItemSelectedListener(NavigationView.OnNavigationItemSelectedListener onNavigationItemSelectedListener) {
        this.onNavigationItemSelectedListener = onNavigationItemSelectedListener;
        return this;
    }

    @Override
    protected void onCreatedBehavior(BottomSheetBehavior behavior) {
        behavior.setHideable(false);
        behavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    close();
                } else if (newState != BottomSheetBehavior.STATE_COLLAPSED) {
                    behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            }
        });
    }

    @Override
    protected void initView() {
        navigationView = rootView.findViewById(R.id.navigation_view);
        titleView = rootView.findViewById(R.id.title_text_view);
        titleView.setText(title);
        titleView.setVisibility(title == null ? View.GONE : View.VISIBLE);
        navigationView.inflateMenu(menuRes);
        if (initMenuCallback != null) {
            initMenuCallback.init(navigationView.getMenu());
        }
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.post(() -> {
            mBehavior.setPeekHeight(titleView.getHeight() + navigationView.getHeight(), false);
        });
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_menu_dialog;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (onNavigationItemSelectedListener != null) {
            close();
            return onNavigationItemSelectedListener.onNavigationItemSelected(item);
        }
        return false;
    }

    @Override
    protected int getHeight() {
        return ViewGroup.LayoutParams.MATCH_PARENT;
    }
}
