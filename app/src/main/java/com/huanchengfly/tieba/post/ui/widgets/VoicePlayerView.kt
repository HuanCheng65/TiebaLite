package com.huanchengfly.tieba.post.ui.widgets

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.ColorFilter
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.View.OnLongClickListener
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.IntDef
import androidx.annotation.OptIn
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.SimpleColorFilter
import com.airbnb.lottie.model.KeyPath
import com.airbnb.lottie.value.LottieValueCallback
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.ui.common.theme.utils.ColorStateListUtils
import com.huanchengfly.tieba.post.ui.common.theme.utils.ThemeUtils
import com.huanchengfly.tieba.post.utils.DisplayUtil
import com.huanchengfly.tieba.post.utils.FileUtil
import com.huanchengfly.tieba.post.utils.PopupUtil
import java.io.IOException
import java.util.Timer
import java.util.TimerTask

class VoicePlayerView @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr), View.OnClickListener, Player.Listener,
    OnLongClickListener, PopupMenu.OnMenuItemClickListener {
    private lateinit var time: TextView
    private lateinit var icon: ImageView
    private lateinit var animationView: LottieAnimationView
    private lateinit var progressBar: ProgressBar

    private var timer: Timer? = null
    private var player: Player? = null

    var url: String? = null

    private var hasPrepared = false
    var completed = false

    private var forceReset = false
    var isMini = false
        set(value) {
            field = value
            reloadView()
        }
    var duration = 0
        set(value) {
            field = value
            setText(calculateTime(duration / 1000))
        }

    init {
        initView()
        initMediaPlayer()
    }

    private fun reloadView() {
        val layoutParams = animationView.layoutParams
        if (isMini) {
            layoutParams.width = DisplayUtil.dp2px(context, 64f)
            layoutParams.height = DisplayUtil.dp2px(context, 12f)
            setPadding(
                DisplayUtil.dp2px(context, 4f), DisplayUtil.dp2px(
                    context, 2f
                ), DisplayUtil.dp2px(context, 4f), DisplayUtil.dp2px(
                    context, 2f
                )
            )
        } else {
            layoutParams.width = DisplayUtil.dp2px(context, 96f)
            layoutParams.height = DisplayUtil.dp2px(context, 18f)
            setPadding(
                DisplayUtil.dp2px(context, 8f), DisplayUtil.dp2px(
                    context, 4f
                ), DisplayUtil.dp2px(context, 8f), DisplayUtil.dp2px(
                    context, 4f
                )
            )
        }
        animationView.layoutParams = layoutParams
    }

    private fun initView() {
        backgroundTintList = ColorStateList.valueOf(
            ThemeUtils.getColorByAttr(
                context,
                R.attr.colorAccent
            )
        )
        setBackgroundResource(R.drawable.bg_audio)
        setOnClickListener(this)
        setOnLongClickListener(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            foreground = ContextCompat.getDrawable(context, R.drawable.fg_ripple_radius_50dp)
        }
        inflate(context, R.layout.layout_audio, this)
        time = findViewById(R.id.audio_text)
        icon = findViewById(R.id.audio_play_icon)
        animationView = findViewById<LottieAnimationView>(R.id.audio_wave_anim).apply {
            val colorRes: Int = R.color.default_color_primary
            val csl = ColorStateListUtils.createColorStateList(context, colorRes)
            val filter = SimpleColorFilter(csl.defaultColor)
            val keyPath = KeyPath("**")
            val callback = LottieValueCallback<ColorFilter>(filter)
            addValueCallback(keyPath, LottieProperty.COLOR_FILTER, callback)
        }
        progressBar = findViewById(R.id.audio_play_progress)
        reloadView()
    }

    fun calculateTime(sec: Int): String {
        val min = sec / 60
        return if (min > 0) {
            min.toString() + "'" + sec % 60 + "''"
        } else {
            "$sec''"
        }
    }

    private fun initMediaPlayer() {
        if (isInEditMode) {
            return
        }
        if (timer != null) {
            timer!!.cancel()
            timer = null
        }
        timer = Timer()
        player = Player(this)
    }

    fun setText(text: String?) {
        time.text = text
    }

    override fun onLongClick(v: View): Boolean {
        if (url == null) return false
        val popupMenu = PopupUtil.create(this)
        popupMenu.inflate(R.menu.menu_audio_long_click)
        popupMenu.setOnMenuItemClickListener(this)
        popupMenu.show()
        return true
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_download) {
            if (url != null) {
                val uri = Uri.parse(url)
                val md5 = uri.getQueryParameter("voice_md5")
                FileUtil.downloadBySystem(
                    context,
                    FileUtil.FILE_TYPE_AUDIO,
                    url,
                    (md5 ?: System.currentTimeMillis().toString()) + ".mp3"
                )
            }
            return true
        }
        return false
    }

    fun reset() {
        if (player == null) {
            initMediaPlayer()
        } else {
            try {
                player!!.reset()
            } catch (e: IllegalStateException) {
                e.printStackTrace()
                player = null
                initMediaPlayer()
            }
        }
        forceReset = true
        hasPrepared = false
        completed = false
        setState(STATE_PAUSING)
        animationView.visibility = GONE
        if (timer != null) {
            timer!!.cancel()
            timer = null
        }
        timer = Timer()
    }

    fun startPlay() {
        if (url == null) {
            throw NullPointerException("Url is null.")
        }
        startPlay(url)
    }

    private fun setState(@IconState state: Int) {
        when (state) {
            STATE_LOADING -> {
                icon.visibility = GONE
                progressBar.visibility = VISIBLE
                animationView.pauseAnimation()
            }

            STATE_PLAYING -> {
                icon.visibility = VISIBLE
                icon.setImageResource(R.drawable.ic_round_pause_circle_filled_18dp)
                progressBar.visibility = GONE
                animationView.playAnimation()
            }

            STATE_PAUSING -> {
                icon.visibility = VISIBLE
                icon.setImageResource(R.drawable.ic_round_play_circle_filled_18dp)
                progressBar.visibility = GONE
                animationView.pauseAnimation()
            }
        }
    }

    fun startPlay(url: String?) {
        if (url == null) {
            throw NullPointerException("Url is null.")
        }
        Player.notifyReset()
        try {
            player!!.reset()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
            player = null
            initMediaPlayer()
        }
        this.url = url
        forceReset = false
        hasPrepared = false
        completed = false
        try {
            player!!.setDataSource(url)
            player!!.prepare()
            setState(STATE_LOADING)
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e(TAG, "set dataSource error", e)
        } catch (e: IllegalStateException) {
            Log.e(TAG, "set dataSource error", e)
        }
    }

    fun release() {
        if (timer != null) {
            timer!!.cancel()
            timer = null
        }
        if (player != null) {
            player!!.release()
            player = null
        }
    }

    override fun onClick(v: View) {
        toggleStatus()
    }

    fun toggleStatus() {
        if (player == null) {
            initMediaPlayer()
        }
        if (!isThisPlaying) {
            Log.i(TAG, "toggleStatus: startPlay")
            startPlay()
        } else if (player!!.isPlaying) {
            Log.i(TAG, "toggleStatus: pause")
            pause()
        } else {
            Log.i(TAG, "toggleStatus: play")
            play()
        }
    }

    fun play() {
        if (player != null && hasPrepared && !player!!.isPlaying) {
            if (completed || forceReset) {
                startPlay()
                return
            }
            player!!.start()
            setState(STATE_PLAYING)
        }
    }

    private val isThisPlaying: Boolean
        get() = TextUtils.equals(Player.PLAYING_DATA_SOURCE, url)

    private fun pause() {
        if (player != null && hasPrepared && player!!.isPlaying) {
            player!!.pause()
            setState(STATE_PAUSING)
        }
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        if (playbackState == androidx.media3.common.Player.STATE_READY) {
            Log.i(TAG, "onPrepared: ")
            hasPrepared = true
            animationView.visibility = VISIBLE
            play()
            setState(STATE_PLAYING)
            setText(calculateTime(duration / 1000))
            timer!!.schedule(object : TimerTask() {
                override fun run() {
                    if (!completed) {
                        Companion.handler.post {
                            setText(calculateTime((player?.currentPosition ?: 0) / 1000))
                        }
                    }
                }
            }, 0, 50)
        } else if (playbackState == androidx.media3.common.Player.STATE_ENDED) {
            setState(STATE_PAUSING)
            completed = true
        }
    }

    @IntDef(STATE_LOADING, STATE_PLAYING, STATE_PAUSING)
    @Retention(AnnotationRetention.SOURCE)
    private annotation class IconState
    abstract class PlayerInterface(protected var voicePlayerView: VoicePlayerView) {
        abstract fun reset()

        @Throws(IOException::class)
        abstract fun setDataSource(url: String?)
        abstract fun prepare()
        abstract fun stop()
        abstract fun release()
        abstract val isPlaying: Boolean
        abstract fun start()
        abstract fun pause()
        abstract fun seekTo(msec: Int)
        abstract val currentPosition: Int
        abstract val duration: Int
    }

    class Player(voicePlayerView: VoicePlayerView) : PlayerInterface(voicePlayerView) {
        private val mExoPlayer: ExoPlayer
        private var dataSource: String? = null

        init {
            mExoPlayer = Manager.getExoPlayer(voicePlayerView.context)
        }

        override fun reset() {
            mExoPlayer.stop()
            PLAYING_DATA_SOURCE = null
            setCurrent(null)
        }

        @OptIn(UnstableApi::class)
        @Throws(IOException::class)
        override fun setDataSource(url: String?) {
            mExoPlayer.setMediaSource(
                DefaultMediaSourceFactory(voicePlayerView.context)
                    .createMediaSource(
                        MediaItem.fromUri(url!!)
                    )
            )
            dataSource = url
            setCurrent(voicePlayerView)
        }

        override fun prepare() {
            mExoPlayer.prepare()
            PLAYING_DATA_SOURCE = dataSource
            setCurrent(voicePlayerView)
        }

        private fun setCurrent(current: VoicePlayerView?) {
            if (current != null) {
                CURRENT = current
                Manager.notifyPlaying(CURRENT)
                mExoPlayer.addListener(CURRENT!!)
            } else {
                if (CURRENT != null) mExoPlayer.removeListener(
                    CURRENT!!
                )
                CURRENT = null
            }
        }

        override fun stop() {
            mExoPlayer.stop()
        }

        override fun release() {
            if (CURRENT != null) CURRENT!!.reset()
            mExoPlayer.release()
            PLAYING_DATA_SOURCE = null
            setCurrent(null)
        }

        override val isPlaying: Boolean
            get() = mExoPlayer.isPlaying

        override fun start() {
            mExoPlayer.play()
            PLAYING_DATA_SOURCE = dataSource
            setCurrent(voicePlayerView)
        }

        override fun pause() {
            mExoPlayer.pause()
        }

        override fun seekTo(msec: Int) {
            mExoPlayer.seekTo(msec.toLong())
        }

        override val currentPosition: Int
            get() = mExoPlayer.currentPosition.toInt()

        override val duration: Int
            get() {
                return mExoPlayer.duration.toInt()
            }

        companion object {
            @JvmStatic
            var CURRENT: VoicePlayerView? = null
            var PLAYING_DATA_SOURCE: String? = null
            fun notifyReset() {
                if (CURRENT != null) {
                    CURRENT!!.reset()
                }
            }
        }
    }

    object Manager {
        @JvmStatic
        private var sExoPlayer: ExoPlayer? = null

        @JvmStatic
        private var current: VoicePlayerView? = null

        @OptIn(UnstableApi::class)
        fun getExoPlayer(context: Context): ExoPlayer {
            if (sExoPlayer == null) {
                synchronized(Manager::class.java) {
                    if (sExoPlayer == null) {
                        sExoPlayer = ExoPlayer.Builder(context)
                            .setAudioAttributes(
                                AudioAttributes.Builder()
                                    .setUsage(C.USAGE_MEDIA)
                                    .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH)
                                    .build(), true
                            )
                            .setHandleAudioBecomingNoisy(true) // 自动暂停播放
                            .setRenderersFactory(
                                DefaultRenderersFactory(context).setExtensionRendererMode(
                                    DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER /* We prefer extensions, such as FFmpeg */
                                )
                            )
                            .build()
                            .apply { playWhenReady = true }
                    }
                }
            }
            return sExoPlayer!!
        }

        fun notifyPlaying(voicePlayerView: VoicePlayerView?) {
            current = voicePlayerView
        }

        fun release() {
            if (current != null) {
                current!!.release()
                current = null
            } else if (sExoPlayer != null) {
                sExoPlayer!!.release()
            }
            sExoPlayer = null
        }
    }

    companion object {
        const val TAG = "AudioView"
        const val STATE_LOADING = 0
        const val STATE_PLAYING = 1
        const val STATE_PAUSING = 2
        val handler = Handler(Looper.getMainLooper())
    }
}