package com.huanchengfly.tieba.post.widgets;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.cardview.widget.CardView;

import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.utils.DialogUtil;
import com.huanchengfly.tieba.post.utils.FileUtil;
import com.huanchengfly.tieba.post.utils.ImageUtil;

import cn.jzvd.JzvdStd;

public class VideoPlayerStandard extends JzvdStd {
    private ImageButton downloadBtn;
    private CardView background;

    public VideoPlayerStandard(Context context) {
        super(context);
    }

    public VideoPlayerStandard(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void init(Context context) {
        super.init(context);
        background = findViewById(R.id.background);
        background.setCardBackgroundColor(Color.TRANSPARENT);
        background.setRadius(ImageUtil.getRadiusPx(getContext()));
        downloadBtn = findViewById(R.id.download_btn);
        downloadBtn.setOnClickListener(this);
    }

    @Override
    public void setScreenTiny() {
        super.setScreenTiny();
        background.setCardBackgroundColor(Color.TRANSPARENT);
        background.setRadius(ImageUtil.getRadiusPx(getContext()));
        batteryTimeLayout.setVisibility(GONE);
        downloadBtn.setVisibility(INVISIBLE);
    }

    @Override
    public int getLayoutId() {
        return R.layout.layout_video_player;
    }

    @Override
    public void setScreenNormal() {
        super.setScreenNormal();
        background.setCardBackgroundColor(Color.TRANSPARENT);
        background.setRadius(ImageUtil.getRadiusPx(getContext()));
        fullscreenButton.setImageResource(R.drawable.ic_round_fullscreen);
        batteryTimeLayout.setVisibility(GONE);
        downloadBtn.setVisibility(INVISIBLE);
    }

    @Override
    public void setScreenFullscreen() {
        super.setScreenFullscreen();
        background.setCardBackgroundColor(Color.BLACK);
        background.setRadius(0);
        fullscreenButton.setImageResource(R.drawable.ic_round_fullscreen_exit);
        batteryTimeLayout.setVisibility(GONE);
        downloadBtn.setVisibility(VISIBLE);
    }

    @Override
    public void gotoNormalScreen() {
        try {
            super.gotoNormalScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void gotoFullscreen() {
        try {
            super.gotoFullscreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateStartImage() {
        if (state == STATE_PLAYING) {
            startButton.setVisibility(VISIBLE);
            startButton.setImageResource(R.drawable.ic_round_pause);
            replayTextView.setVisibility(GONE);
        } else if (state == STATE_ERROR) {
            startButton.setVisibility(INVISIBLE);
            replayTextView.setVisibility(GONE);
        } else if (state == STATE_AUTO_COMPLETE) {
            startButton.setVisibility(VISIBLE);
            startButton.setImageResource(R.drawable.ic_round_replay);
            replayTextView.setVisibility(VISIBLE);
        } else {
            startButton.setImageResource(R.drawable.ic_round_play_arrow);
            replayTextView.setVisibility(GONE);
        }
    }

    @Override
    public void showWifiDialog() {
        DialogUtil.build(getContext())
                .setMessage(getResources().getString(R.string.tips_not_wifi))
                .setPositiveButton(getResources().getString(R.string.tips_not_wifi_confirm), (dialog, which) -> {
                    dialog.dismiss();
                    startVideo();
                    WIFI_TIP_DIALOG_SHOWED = true;
                })
                .setNegativeButton(getResources().getString(R.string.tips_not_wifi_cancel), (dialog, which) -> {
                    dialog.dismiss();
                    try {
                        clearFloatScreen();
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                })
                .setOnCancelListener(DialogInterface::dismiss).create().show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.download_btn:
                String url = (String) jzDataSource.getCurrentUrl();
                FileUtil.downloadBySystem(getContext(), FileUtil.FILE_TYPE_VIDEO, url);
                Toast.makeText(getContext(), R.string.toast_start_download, Toast.LENGTH_SHORT).show();
                break;
            default:
                super.onClick(v);
                break;
        }
    }
}
