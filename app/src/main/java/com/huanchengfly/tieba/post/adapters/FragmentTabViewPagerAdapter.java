package com.huanchengfly.tieba.post.adapters;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class FragmentTabViewPagerAdapter extends FragmentPagerAdapter {
    public Fragment currentFragment;
    public int currentFragmentPosition;
    private List<Fragment> fragments;
    private List<String> titles;

    public FragmentTabViewPagerAdapter(FragmentManager fm) {
        super(fm);
        fragments = new ArrayList<>();
        titles = new ArrayList<>();
    }

    @Override
    public Fragment getItem(int position) {
        currentFragmentPosition = position;
        currentFragment = fragments.get(position);
        return fragments.get(position);
    }

    public Fragment getCurrentFragment() {
        return currentFragment;
    }

    public int getCurrentFragmentPosition() {
        return currentFragmentPosition;
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    public void addFragment(Fragment fragment, String title) {
        fragments.add(fragment);
        titles.add(title);
        notifyDataSetChanged();
    }

    public void clear() {
        fragments = new ArrayList<>();
        titles = new ArrayList<>();
        notifyDataSetChanged();
    }

    public List<Fragment> getFragments() {
        return fragments;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titles.get(position);
    }
}