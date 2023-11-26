package com.huanchengfly.tieba.post.adapters;

import static com.huanchengfly.tieba.post.utils.ThemeUtil.THEME_CUSTOM;
import static com.huanchengfly.tieba.post.utils.ThemeUtil.THEME_TRANSLUCENT;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.huanchengfly.tieba.post.App;
import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.activities.TranslucentThemeActivity;
import com.huanchengfly.tieba.post.components.MyViewHolder;
import com.huanchengfly.tieba.post.components.dialogs.CustomThemeDialog;
import com.huanchengfly.tieba.post.interfaces.OnItemClickListener;
import com.huanchengfly.tieba.post.ui.common.theme.interfaces.ExtraRefreshable;
import com.huanchengfly.tieba.post.ui.common.theme.utils.ThemeUtils;
import com.huanchengfly.tieba.post.utils.ColorUtils;
import com.huanchengfly.tieba.post.utils.ThemeUtil;

import java.util.Arrays;
import java.util.List;

public class ThemeAdapter extends RecyclerView.Adapter<MyViewHolder> implements View.OnClickListener {
    private static final int THEME_DAY = 0;
    private static final int THEME_NIGHT = 1;
    private static final int NIGHT_MODE_TIP_VISIBILITY = View.VISIBLE;
    private static final int NIGHT_MODE_TIP_GONE = View.GONE;

    private final Context mContext;
    private final String[] themes;
    private final String[] themeNames;
    private OnItemClickListener<String> onItemClickListener;
    private int selectedPosition;

    public ThemeAdapter(Context context) {
        this.mContext = context;
        themes = mContext.getResources().getStringArray(R.array.theme_values);
        themeNames = mContext.getResources().getStringArray(R.array.themeNames);
        List<String> themeList = Arrays.asList(themes);
        selectedPosition = themeList.indexOf(ThemeUtil.getRawTheme());
    }

    public void refresh() {
        List<String> themeList = Arrays.asList(themes);
        selectedPosition = themeList.indexOf(ThemeUtil.getRawTheme());
        notifyDataSetChanged();
    }

    public OnItemClickListener<String> getOnItemClickListener() {
        return onItemClickListener;
    }

    public ThemeAdapter setOnItemClickListener(OnItemClickListener<String> onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
        return this;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(mContext, R.layout.item_theme);
    }

    private int getToolbarColor(String theme) {
        if (ThemeUtil.isNightMode(theme)) {
            return App.ThemeDelegate.INSTANCE.getColorByAttr(mContext, R.attr.colorToolbar, theme);
        } else if (ThemeUtil.isTranslucentTheme(theme)) {
            return ColorUtils.alpha(App.ThemeDelegate.INSTANCE.getColorByAttr(mContext, R.attr.colorPrimary, ThemeUtil.THEME_TRANSLUCENT_LIGHT), 150);
        }
        return App.ThemeDelegate.INSTANCE.getColorByAttr(mContext, R.attr.colorPrimary, theme);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        int type = getItemViewType(position);
        View previewView = holder.getView(R.id.theme_preview);
        TextView themeName = holder.getView(R.id.theme_name);
        holder.setVisibility(R.id.night_mode_tip, type == THEME_NIGHT ? NIGHT_MODE_TIP_VISIBILITY : NIGHT_MODE_TIP_GONE);
        ImageView selected = holder.getView(R.id.theme_selected);
        String theme = themes[position];
        int toolbarColor = getToolbarColor(theme);
        themeName.setText(themeNames[position]);
        selected.setVisibility(position == selectedPosition ? View.VISIBLE : View.GONE);
        selected.setTag(theme);
        if (THEME_CUSTOM.equals(theme) || THEME_TRANSLUCENT.equals(theme)) {
            selected.setImageResource(R.drawable.ic_round_create);
            selected.setOnClickListener(this);
        } else {
            selected.setImageResource(R.drawable.ic_round_check);
            selected.setOnClickListener(null);
        }
        previewView.setBackgroundTintList(ColorStateList.valueOf(toolbarColor));
        holder.setItemOnClickListener(v -> {
            int oldPosition = selectedPosition;
            selectedPosition = position;
            notifyItemChanged(oldPosition);
            notifyItemChanged(position);
            if (getOnItemClickListener() != null) {
                getOnItemClickListener().onClick(holder.itemView, theme, position, type);
            }
        });
    }

    @Override
    public int getItemCount() {
        return themes.length;
    }

    @Override
    public int getItemViewType(int position) {
        String theme = themes[position];
        if (ThemeUtil.isNightMode(theme)) {
            return THEME_NIGHT;
        }
        return THEME_DAY;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.theme_selected) {
            handleThemeSelected(v);
        }
    }

    private void handleThemeSelected(View v) {
        String theme = (String) v.getTag();
        if (THEME_CUSTOM.equals(theme)) {
            showCustomThemeDialog();
        } else if (THEME_TRANSLUCENT.equals(theme)) {
            mContext.startActivity(new Intent(mContext, TranslucentThemeActivity.class));
        }
    }

    private void showCustomThemeDialog() {
        CustomThemeDialog customThemeDialog = new CustomThemeDialog(mContext);
        customThemeDialog.setOnDismissListener(dialog -> {
            if (mContext instanceof ExtraRefreshable) {
                ThemeUtils.refreshUI(mContext, (ExtraRefreshable) mContext);
            }
        });
        customThemeDialog.show();
    }
}
