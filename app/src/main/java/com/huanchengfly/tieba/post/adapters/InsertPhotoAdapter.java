package com.huanchengfly.tieba.post.adapters;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.activities.ReplyActivity;
import com.huanchengfly.tieba.post.components.MyImageEngine;
import com.huanchengfly.tieba.post.components.MyViewHolder;
import com.huanchengfly.tieba.post.components.transformations.RadiusTransformation;
import com.huanchengfly.tieba.post.models.PhotoInfoBean;
import com.huanchengfly.tieba.post.utils.PermissionUtil;
import com.huanchengfly.tieba.post.utils.ThemeUtil;
import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.runtime.Permission;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class InsertPhotoAdapter extends RecyclerView.Adapter<MyViewHolder> {
    public static final int TYPE_IMAGE = 0;
    public static final int TYPE_INSERT = 1;
    private static final String TAG = "InsertPhotoAdapter";
    private Context mContext;
    private List<PhotoInfoBean> fileList;

    public InsertPhotoAdapter(Context context) {
        super();
        this.mContext = context;
        this.fileList = new ArrayList<>();
    }

    public void remove(int position) {
        fileList.remove(position);
        notifyItemRemoved(position);
    }

    public void swap(int oldPosition, int newPosition) {
        Collections.swap(fileList, oldPosition, newPosition);
    }

    public List<PhotoInfoBean> getFileList() {
        return fileList;
    }

    public void setFileList(List<PhotoInfoBean> fileList) {
        this.fileList = fileList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (position >= fileList.size()) return TYPE_INSERT;
        return TYPE_IMAGE;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_INSERT) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_insert_more, parent, false);
            return new MyViewHolder(view);
        }
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_insert_image, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        if (holder.getItemViewType() == TYPE_IMAGE) {
            PhotoInfoBean photoInfoBean = fileList.get(position);
            ImageView imageView = holder.getView(R.id.image_preview);
            Glide.with(mContext)
                    .load(photoInfoBean.getFileUri())
                    .apply(RequestOptions.bitmapTransform(new RadiusTransformation()))
                    .into(imageView);
        } else if (holder.getItemViewType() == TYPE_INSERT) {
            if (fileList.size() < 10) {
                holder.setItemOnClickListener((View view) -> {
                    if (mContext instanceof Activity) {
                        askPermission(data -> Matisse.from((Activity) mContext)
                                .choose(MimeType.ofImage())
                                .countable(true)
                                .maxSelectable(10 - fileList.size())
                                .theme(ThemeUtil.isNightMode(mContext) ? R.style.Matisse_Dracula : R.style.Matisse_Zhihu)
                                .imageEngine(new MyImageEngine())
                                .forResult(ReplyActivity.REQUEST_CODE_CHOOSE));
                    }
                });
            } else {
                Toast.makeText(mContext, R.string.toast_max_selectable, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void askPermission(Action<List<String>> granted) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            PermissionUtil.askPermission(mContext, granted, R.string.toast_no_permission_insert_photo,
                    new PermissionUtil.Permission(Permission.Group.STORAGE, mContext.getString(R.string.tip_permission_storage)));
        } else {
            PermissionUtil.askPermission(mContext, granted, R.string.toast_no_permission_insert_photo,
                    new PermissionUtil.Permission(Permission.READ_EXTERNAL_STORAGE, mContext.getString(R.string.tip_permission_storage)));
        }
    }

    @Override
    public int getItemCount() {
        return fileList.size() + 1;
    }

    public PhotoInfoBean getItem(int position) {
        return fileList.get(position);
    }
}