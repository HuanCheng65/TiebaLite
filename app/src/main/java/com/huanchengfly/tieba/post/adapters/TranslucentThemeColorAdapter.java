package com.huanchengfly.tieba.post.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.RecyclerView;

import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.components.MyViewHolder;
import com.huanchengfly.tieba.post.interfaces.OnItemClickListener;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class TranslucentThemeColorAdapter extends RecyclerView.Adapter<MyViewHolder> {
    private static final int[] sColors = new int[]{
            Color.parseColor("#FF4477E0"),
            Color.parseColor("#FFFF9A9E"),
            Color.parseColor("#FFC51100"),
            Color.parseColor("#FF000000"),
            Color.parseColor("#FF512DA8")
    };
    private final WeakReference<Context> mContextWeakReference;
    private List<Integer> mColors;
    private OnItemClickListener<Integer> mOnItemClickListener;
    private int mSelectedColor;

    public TranslucentThemeColorAdapter(Context context) {
        mContextWeakReference = new WeakReference<>(context);
    }

    public OnItemClickListener<Integer> getOnItemClickListener() {
        return mOnItemClickListener;
    }

    public void setOnItemClickListener(OnItemClickListener<Integer> onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
        notifyDataSetChanged();
    }

    public Context getContext() {
        return mContextWeakReference.get();
    }

    public void setPalette(Palette palette) {
        mColors = new ArrayList<>();
        int[] colors = new int[]{
                palette.getVibrantColor(Color.TRANSPARENT),
                palette.getMutedColor(Color.TRANSPARENT),
                palette.getDominantColor(Color.TRANSPARENT)
        };
        for (int color : colors) {
            if (color != Color.TRANSPARENT) mColors.add(color);
        }
        for (int color : sColors) {
            mColors.add(color);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(getContext(), R.layout.item_theme_color);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.setItemOnClickListener(v -> {
            mSelectedColor = mColors.get(position);
            notifyDataSetChanged();
            if (getOnItemClickListener() != null) {
                getOnItemClickListener().onClick(holder.itemView, mColors.get(position), position, 0);
            }
        });
        View preview = holder.getView(R.id.theme_preview);
        preview.setBackgroundTintList(ColorStateList.valueOf(mColors.get(position)));
        if (mSelectedColor == mColors.get(position)) {
            holder.setVisibility(R.id.theme_selected, View.VISIBLE);
        } else {
            holder.setVisibility(R.id.theme_selected, View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return mColors.size();
    }
}
