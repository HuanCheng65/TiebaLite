package com.huanchengfly.tieba.post.adapters;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.huanchengfly.tieba.post.fragments.PhotoViewFragment;
import com.huanchengfly.tieba.post.models.PhotoViewBean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PhotoViewAdapter extends FragmentStateAdapter {
    private List<PhotoViewBean> mList;

    @SuppressLint("WrongConstant")
    public PhotoViewAdapter(@NonNull FragmentActivity fragmentActivity, List<PhotoViewBean> list) {
        super(fragmentActivity);
        mList = new ArrayList<>(list);
    }

    public PhotoViewBean getBean(int position) {
        return mList.get(position);
    }

    public List<PhotoViewBean> getData() {
        return mList;
    }

    public void insert(Collection<? extends PhotoViewBean> photoViewBeans) {
        insert(mList.size(), photoViewBeans);
    }

    public void insert(int position, Collection<? extends PhotoViewBean> photoViewBeans) {
        if (position <= mList.size() && position >= 0) {
            mList.addAll(position, photoViewBeans);
            this.notifyItemRangeInserted(position, photoViewBeans.size());
            this.notifyItemRangeChanged(position, mList.size() - position);
        }
    }

    public void insert(PhotoViewBean photoViewBean) {
        insert(mList.size(), photoViewBean);
    }

    public void insert(int position, PhotoViewBean photoViewBean) {
        if (position <= mList.size() && position >= 0) {
            mList.add(position, photoViewBean);
            this.notifyItemInserted(position);
            this.notifyItemRangeChanged(position, mList.size() - position);
        }
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return PhotoViewFragment.newInstance(mList.get(position));
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }
}
