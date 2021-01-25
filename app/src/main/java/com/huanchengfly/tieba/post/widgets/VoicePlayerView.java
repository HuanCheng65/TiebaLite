package com.huanchengfly.tieba.post.widgets;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.IntDef;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;

import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.ui.theme.utils.ThemeUtils;
import com.huanchengfly.tieba.post.utils.DisplayUtil;
import com.huanchengfly.tieba.post.utils.FileUtil;
import com.huanchengfly.tieba.post.utils.PopupUtil;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Timer;
import java.util.TimerTask;

public class VoicePlayerView extends RelativeLayout implements View.OnClickListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener, MediaPlayer.OnPreparedListener, SeekBar.OnSeekBarChangeListener, View.OnLongClickListener, PopupMenu.OnMenuItemClickListener {
    public static final String TAG = "AudioView";
    public static final int STATE_LOADING = 0;
    public static final int STATE_PLAYING = 1;
    public static final int STATE_PAUSING = 2;
    TextView time;
    ImageView icon;
    SeekBar seekBar;
    ProgressBar progressBar;
    String mUrl;
    Timer timer;
    Player player;
    boolean hasPrepared = false;
    boolean completed = false;
    boolean forceReset = false;
    boolean isSeekbarChaning = false;
    boolean mini = false;
    int duration = 0;

    public VoicePlayerView(Context context) {
        this(context, null);
    }

    public VoicePlayerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VoicePlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
        initMediaPlayer();
    }

    public boolean isMini() {
        return mini;
    }

    public VoicePlayerView setMini(boolean mini) {
        this.mini = mini;
        reloadView();
        return this;
    }

    public String getUrl() {
        return mUrl;
    }

    public VoicePlayerView setUrl(String url) {
        this.mUrl = url;
        return this;
    }

    private void reloadView() {
        ViewGroup.LayoutParams layoutParams = seekBar.getLayoutParams();
        if (isMini()) {
            layoutParams.width = DisplayUtil.dp2px(getContext(), 64);
            layoutParams.height = DisplayUtil.dp2px(getContext(), 12);
            setPadding(DisplayUtil.dp2px(getContext(), 4), DisplayUtil.dp2px(getContext(), 2), DisplayUtil.dp2px(getContext(), 4), DisplayUtil.dp2px(getContext(), 2));
        } else {
            layoutParams.width = DisplayUtil.dp2px(getContext(), 96);
            layoutParams.height = DisplayUtil.dp2px(getContext(), 18);
            setPadding(DisplayUtil.dp2px(getContext(), 8), DisplayUtil.dp2px(getContext(), 4), DisplayUtil.dp2px(getContext(), 8), DisplayUtil.dp2px(getContext(), 4));
        }
        seekBar.setLayoutParams(layoutParams);
    }

    private void initView() {
        setBackgroundTintList(ColorStateList.valueOf(ThemeUtils.getColorByAttr(getContext(), R.attr.colorAccent)));
        setBackgroundResource(R.drawable.bg_audio);
        setOnClickListener(this);
        setOnLongClickListener(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            setForeground(ContextCompat.getDrawable(getContext(), R.drawable.fg_ripple_radius_50dp));
        }
        inflate(getContext(), R.layout.layout_audio, this);
        time = findViewById(R.id.audio_text);
        icon = findViewById(R.id.audio_play_icon);
        seekBar = findViewById(R.id.seek_progress);
        int color = ThemeUtils.getColorByAttr(getContext(), R.attr.colorAccent);
        color = Color.argb((int) (0.15F * 255), Color.red(color), Color.green(color), Color.blue(color));
        seekBar.setProgressBackgroundTintList(ColorStateList.valueOf(color));
        progressBar = findViewById(R.id.audio_play_progress);
        seekBar.setOnSeekBarChangeListener(this);
        reloadView();
    }

    public String calculateTime(long sec) {
        long min = sec / 60;
        if (min > 0) {
            return min + "'" + sec % 60 + "''";
        } else {
            return sec + "''";
        }
    }

    private void initMediaPlayer() {
        if (isInEditMode()) {
            return;
        }
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        timer = new Timer();
        player = new Player(this);
    }

    public void setText(String text) {
        time.setText(text);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        setText(calculateTime((seekBar.getMax() - progress) / 1000));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        isSeekbarChaning = true;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
        setText(calculateTime(duration / 1000));
        seekBar.setProgress(0);
        seekBar.setMax(duration);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        isSeekbarChaning = false;
        player.seekTo(seekBar.getProgress());
        setText(calculateTime((getDuration() - player.getCurrentPosition()) / 1000));
    }

    @Override
    public boolean onLongClick(View v) {
        if (mUrl == null) return false;
        PopupMenu popupMenu = PopupUtil.create(this);
        popupMenu.inflate(R.menu.menu_audio_long_click);
        popupMenu.setOnMenuItemClickListener(this);
        popupMenu.show();
        return true;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if (item.getItemId() == R.id.menu_download) {
            if (mUrl != null) {
                Uri uri = Uri.parse(mUrl);
                String md5 = uri.getQueryParameter("voice_md5");
                FileUtil.downloadBySystem(getContext(), FileUtil.FILE_TYPE_AUDIO, mUrl, (md5 == null ? String.valueOf(System.currentTimeMillis()) : md5) + ".mp3");
            }
            return true;
        }
        return false;
    }

    public void reset() {
        if (player == null) {
            initMediaPlayer();
        } else {
            try {
                player.reset();
            } catch (IllegalStateException e) {
                e.printStackTrace();
                player = null;
                initMediaPlayer();
            }
        }
        forceReset = true;
        hasPrepared = false;
        completed = false;
        setIconState(STATE_PAUSING);
        seekBar.setVisibility(GONE);
        seekBar.setProgress(0);
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        timer = new Timer();
    }

    public void startPlay() {
        if (mUrl == null) {
            throw new NullPointerException("Url is null.");
        }
        startPlay(mUrl);
    }

    private void setIconState(@IconState int state) {
        switch (state) {
            case STATE_LOADING:
                icon.setVisibility(GONE);
                progressBar.setVisibility(VISIBLE);
                break;
            case STATE_PLAYING:
                icon.setVisibility(VISIBLE);
                icon.setImageResource(R.drawable.ic_round_pause_circle_filled_18dp);
                progressBar.setVisibility(GONE);
                break;
            case STATE_PAUSING:
                icon.setVisibility(VISIBLE);
                icon.setImageResource(R.drawable.ic_round_play_circle_filled_18dp);
                progressBar.setVisibility(GONE);
                break;
        }
    }

    public void startPlay(String url) {
        if (url == null) {
            throw new NullPointerException("Url is null.");
        }
        Player.notifyReset();
        try {
            player.reset();
        } catch (IllegalStateException e) {
            e.printStackTrace();
            player = null;
            initMediaPlayer();
        }
        mUrl = url;
        forceReset = false;
        hasPrepared = false;
        completed = false;
        try {
            player.setDataSource(url);
            player.prepare();
            setIconState(STATE_LOADING);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "set dataSource error", e);
        } catch (IllegalStateException e) {
            Log.e(TAG, "set dataSource error", e);
        }
    }

    public void release() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (player != null) {
            player.release();
            player = null;
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        setIconState(STATE_PAUSING);
        completed = true;
        seekBar.setProgress(0);
    }

    @Override
    public void onClick(View v) {
        toggleStatus();
    }

    public void toggleStatus() {
        if (player == null) {
            initMediaPlayer();
        }
        if (!isThisPlaying()) {
            Log.i(TAG, "toggleStatus: startPlay");
            startPlay();
        } else if (player.isPlaying()) {
            Log.i(TAG, "toggleStatus: pause");
            pause();
        } else {
            Log.i(TAG, "toggleStatus: play");
            play();
        }
    }

    public void play() {
        if (player != null && hasPrepared && !player.isPlaying()) {
            if (completed || forceReset) {
                startPlay();
                return;
            }
            player.start();
            setIconState(STATE_PLAYING);
        }
    }

    private boolean isThisPlaying() {
        return TextUtils.equals(Player.PLAYING_DATA_SOURCE, mUrl);
    }

    public void pause() {
        if (player != null && hasPrepared && player.isPlaying()) {
            player.pause();
            setIconState(STATE_PAUSING);
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.i(TAG, "onError: " + what + " " + extra);
        Toast.makeText(getContext(), R.string.toast_play_failed, Toast.LENGTH_SHORT).show();
        setIconState(STATE_PAUSING);
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        Log.i(TAG, "onPrepared: ");
        hasPrepared = true;
        seekBar.setVisibility(VISIBLE);
        mp.start();
        setIconState(STATE_PLAYING);
        duration = mp.getDuration();
        setText(calculateTime(duration / 1000));
        seekBar.setProgress(0);
        seekBar.setMax(duration);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (!(isSeekbarChaning || completed)) {
                    seekBar.setProgress(player.getCurrentPosition());
                }
            }
        }, 0, 50);
    }

    @IntDef({STATE_LOADING, STATE_PLAYING, STATE_PAUSING})
    @Retention(RetentionPolicy.SOURCE)
    private @interface IconState {
    }

    static abstract class PlayerInterface {
        protected VoicePlayerView voicePlayerView;

        PlayerInterface(VoicePlayerView voicePlayerView) {
            this.voicePlayerView = voicePlayerView;
        }

        public abstract void reset();

        public abstract void setDataSource(String url) throws IOException;

        public abstract void prepare();

        public abstract void stop();

        public abstract void release();

        public abstract boolean isPlaying();

        public abstract void start();

        public abstract void pause();

        public abstract void seekTo(int msec);

        public abstract int getCurrentPosition();

        public abstract int getDuration();
    }

    static class Player extends PlayerInterface {
        static VoicePlayerView CURRENT;
        static String PLAYING_DATA_SOURCE;
        MediaPlayer mediaPlayer;
        String dataSource;

        Player(VoicePlayerView voicePlayerView) {
            super(voicePlayerView);
            mediaPlayer = Manager.getMediaPlayer();
        }

        public static void notifyReset() {
            if (CURRENT != null) {
                CURRENT.reset();
            }
        }

        @Override
        public void reset() {
            mediaPlayer.reset();
            PLAYING_DATA_SOURCE = null;
            setCurrent(null);
        }

        @Override
        public void setDataSource(String url) throws IOException {
            mediaPlayer.setDataSource(url);
            dataSource = url;
            setCurrent(voicePlayerView);
        }

        @Override
        public void prepare() {
            mediaPlayer.prepareAsync();
            PLAYING_DATA_SOURCE = dataSource;
            setCurrent(voicePlayerView);
        }

        private void setCurrent(@Nullable VoicePlayerView current) {
            CURRENT = current;
            if (current != null) {
                Manager.notifyPlaying(CURRENT);
                mediaPlayer.setOnCompletionListener(CURRENT);
                mediaPlayer.setOnErrorListener(CURRENT);
                mediaPlayer.setOnPreparedListener(CURRENT);
            } else {
                mediaPlayer.setOnCompletionListener(null);
                mediaPlayer.setOnErrorListener(null);
                mediaPlayer.setOnPreparedListener(null);
            }
        }

        @Override
        public void stop() {
            mediaPlayer.stop();
        }

        @Override
        public void release() {
            if (CURRENT != null) CURRENT.reset();
            mediaPlayer.release();
            PLAYING_DATA_SOURCE = null;
            setCurrent(null);
        }

        @Override
        public boolean isPlaying() {
            return mediaPlayer.isPlaying();
        }

        @Override
        public void start() {
            mediaPlayer.start();
            PLAYING_DATA_SOURCE = dataSource;
            setCurrent(voicePlayerView);
        }

        @Override
        public void pause() {
            mediaPlayer.pause();
        }

        @Override
        public void seekTo(int msec) {
            mediaPlayer.seekTo(msec);
        }

        @Override
        public int getCurrentPosition() {
            if (mediaPlayer != null) {
                return mediaPlayer.getCurrentPosition();
            }
            return 0;
        }

        @Override
        public int getDuration() {
            if (mediaPlayer != null) {
                return mediaPlayer.getDuration();
            }
            return 0;
        }
    }

    public static class Manager {
        private static MediaPlayer sMediaPlayer;

        private static VoicePlayerView current;

        public static MediaPlayer getMediaPlayer() {
            if (sMediaPlayer == null) {
                synchronized (Manager.class) {
                    if (sMediaPlayer == null) {
                        sMediaPlayer = new MediaPlayer();
                        sMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    }
                }
            }
            return sMediaPlayer;
        }

        public static void notifyPlaying(VoicePlayerView voicePlayerView) {
            current = voicePlayerView;
        }

        public static void release() {
            if (current != null) {
                current.release();
            } else if (sMediaPlayer != null) {
                sMediaPlayer.release();
            }
            sMediaPlayer = null;
        }
    }
}