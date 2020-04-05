package com.huanchengfly.tieba.post.adapters;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.huanchengfly.tieba.post.fragments.BaseFragment;

import java.util.ArrayList;
import java.util.List;

public class ViewPagerAdapter extends FragmentPagerAdapter {
    private BaseFragment currentFragment;
    private int currentFragmentPosition;
    private List<BaseFragment> fragments = new ArrayList<>();

    public ViewPagerAdapter(FragmentManager fm) {
        super(fm);
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

    public void addFragment(BaseFragment fragment, int position) {
        fragments.add(position, fragment);
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