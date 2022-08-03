package com.huanchengfly.tieba.post.ui.common.intro.adapters;

import android.annotation.SuppressLint;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.huanchengfly.tieba.post.ui.common.intro.fragments.BaseFragment;

import java.util.ArrayList;
import java.util.List;

public class ViewPagerAdapter extends FragmentPagerAdapter {
    private BaseFragment currentFragment;
    private int currentFragmentPosition;
    private final List<BaseFragment> fragments = new ArrayList<>();

    @SuppressLint("WrongConstant")
    public ViewPagerAdapter(FragmentManager fm) {
        super(fm, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
    }

    public List<BaseFragment> getFragments() {
        return fragments;
    }

    @NonNull
    @Override
    public BaseFragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    public void addFragment(BaseFragment fragment) {
        fragments.add(fragment);
    }

    @Override
    public void setPrimaryItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        currentFragment = (BaseFragment) object;
        currentFragmentPosition = position;
        super.setPrimaryItem(container, position, object);
    }

    public BaseFragment getCurrentFragment() {
        return currentFragment;
    }

    public int getCurrentFragmentPosition() {
        return currentFragmentPosition;
    }

    public void clear() {
        fragments.clear();
        notifyDataSetChanged();
    }
}