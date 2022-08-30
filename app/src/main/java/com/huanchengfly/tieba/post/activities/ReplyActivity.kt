package com.huanchengfly.tieba.post.activities

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.FrameLayout
import android.widget.GridView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import butterknife.BindView
import cn.dreamtobe.kpswitch.util.KPSwitchConflictUtil
import cn.dreamtobe.kpswitch.util.KPSwitchConflictUtil.SubPanelAndTrigger
import cn.dreamtobe.kpswitch.util.KeyboardUtil
import cn.dreamtobe.kpswitch.widget.KPSwitchFSPanelFrameLayout
import com.google.android.material.tabs.TabLayout
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.adapters.InsertPhotoAdapter
import com.huanchengfly.tieba.post.adapters.TabViewPagerAdapter
import com.huanchengfly.tieba.post.adapters.TextWatcherAdapter
import com.huanchengfly.tieba.post.api.TiebaApi
import com.huanchengfly.tieba.post.api.retrofit.doIfFailure
import com.huanchengfly.tieba.post.api.retrofit.doIfSuccess
import com.huanchengfly.tieba.post.components.EmotionViewFactory
import com.huanchengfly.tieba.post.components.dialogs.LoadingDialog
import com.huanchengfly.tieba.post.interfaces.ReplyContentCallback
import com.huanchengfly.tieba.post.interfaces.UploadCallback
import com.huanchengfly.tieba.post.models.PhotoInfoBean
import com.huanchengfly.tieba.post.models.ReplyInfoBean
import com.huanchengfly.tieba.post.models.database.Draft
import com.huanchengfly.tieba.post.toastShort
import com.huanchengfly.tieba.post.utils.*
import com.huanchengfly.tieba.post.widgets.edittext.widget.UndoableEditText
import com.huanchengfly.tieba.post.widgets.theme.TintConstraintLayout
import com.huanchengfly.tieba.post.widgets.theme.TintImageView
import org.litepal.LitePal.where

class ReplyActivity : BaseActivity(), View.OnClickListener,
    InsertPhotoAdapter.PickMediasLauncherProvider {
    @BindView(R.id.activity_reply_edit_text)
    lateinit var editText: UndoableEditText

    @BindView(R.id.activity_reply_panel_root)
    lateinit var panelFrameLayout: KPSwitchFSPanelFrameLayout

    @BindView(R.id.activity_reply_emotion)
    lateinit var emotionView: RelativeLayout

    @BindView(R.id.activity_reply_insert_photo)
    lateinit var insertImageView: FrameLayout

    @BindView(R.id.activity_reply_edit_emotion)
    lateinit var emotionBtn: TintImageView

    @BindView(R.id.activity_reply_edit_insert_photo)
    lateinit var insertImageBtn: TintImageView

    @BindView(R.id.activity_reply_emotion_view_pager)
    lateinit var emotionViewPager: ViewPager

    @BindView(R.id.activity_reply_insert_photo_view)
    lateinit var insertView: RecyclerView

    @BindView(R.id.webview_container)
    lateinit var webViewContainer: FrameLayout

    @BindView(R.id.toolbar)
    lateinit var toolbar: Toolbar
    private var replyInfoBean: ReplyInfoBean? = null
    private var loadingDialog: LoadingDialog? = null
    private val insertPhotoAdapter: InsertPhotoAdapter by lazy {
        InsertPhotoAdapter(this)
    }
    private var sendItem: MenuItem? = null
    private var replySuccess = false
    private var content: String? = null
    private var mWebView: WebView? = null
    override val isNeedImmersionBar: Boolean
        get() = false

    @JvmField
    val pickMediasLauncher = registerPickMediasLauncher {
        val photoInfoBeans = insertPhotoAdapter.getFileList().toMutableList()
        for (uri in it) {
            photoInfoBeans.add(PhotoInfoBean(this, uri))
        }
        insertPhotoAdapter.setFileList(photoInfoBeans)
    }

    override fun getPickMediasLauncher(): ActivityResultLauncher<PickMediasRequest> =
        pickMediasLauncher

    override fun getLayoutId(): Int {
        return R.layout.activity_reply
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (ThemeUtil.THEME_TRANSLUCENT == ThemeUtil.getTheme()) {
            val constraintLayout = findViewById<TintConstraintLayout>(R.id.activity_reply_layout)
            constraintLayout.setBackgroundTintResId(0)
            ThemeUtil.setTranslucentBackground(constraintLayout)
        }
        Util.setStatusBarTransparent(this)
        val decor = window.decorView as ViewGroup
        val decorChild = decor.getChildAt(0) as ViewGroup
        decorChild.setBackgroundColor(Color.TRANSPARENT)
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        window.decorView.setBackgroundColor(Color.TRANSPARENT)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        initData()
        initView()
        if (appPreferences.postOrReplyWarning) showDialog {
            setTitle(R.string.title_dialog_reply_warning)
            setMessage(R.string.message_dialog_reply_warning)
            setNegativeButton(R.string.btn_cancel_reply) { _, _ ->
                finish()
            }
            setNeutralButton(R.string.btn_continue_reply, null)
            setPositiveButton(R.string.button_official_client_reply) { _, _ ->
                val intent = Intent(ACTION_VIEW).setData(getDispatchUri())
                val resolveInfos =
                    packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
                        .filter { it.resolvePackageName != packageName }
                try {
                    if (resolveInfos.isNotEmpty()) {
                        startActivity(intent)
                    } else {
                        toastShort(R.string.toast_official_client_not_install)
                    }
                } catch (e: ActivityNotFoundException) {
                    toastShort(R.string.toast_official_client_not_install)
                }
                finish()
            }
        }
    }

    private fun getDispatchUri(): Uri? {
        if (replyInfoBean == null) {
            return null
        }
        return if (replyInfoBean!!.pid != null) {
            Uri.parse("com.baidu.tieba://unidispatch/pb?obj_locate=comment_lzl_cut_guide&obj_source=wise&obj_name=index&obj_param2=chrome&has_token=0&qd=scheme&refer=tieba.baidu.com&wise_sample_id=3000232_2&hightlight_anchor_pid=${replyInfoBean!!.pid}&is_anchor_to_comment=1&comment_sort_type=0&fr=bpush&tid=${replyInfoBean!!.threadId}")
        } else {
            Uri.parse("com.baidu.tieba://unidispatch/pb?obj_locate=pb_reply&obj_source=wise&obj_name=index&obj_param2=chrome&has_token=0&qd=scheme&refer=tieba.baidu.com&wise_sample_id=3000232_2-99999_9&fr=bpush&tid=${replyInfoBean!!.threadId}")
        }
    }

    private fun destroyWebView() {
        if (mWebView != null) {
            (mWebView!!.parent as ViewGroup).removeView(mWebView)
            mWebView!!.removeAllViews()
            mWebView!!.destroy()
            mWebView = null
        }
    }

    override fun onDestroy() {
        if (mWebView != null) {
            destroyWebView()
        }
        super.onDestroy()
    }

    override fun onPause() {
        super.onPause()
        panelFrameLayout.recordKeyboardStatus(window)
        if (replyInfoBean != null && !replySuccess) {
            Draft(
                replyInfoBean!!.hash(),
                if (editText.text == null) "" else editText.text.toString()
            )
                .saveOrUpdate("hash = ?", replyInfoBean!!.hash())
        } else if (replySuccess) {
            val draft = where("hash = ?", replyInfoBean!!.hash())
                .findFirst(Draft::class.java)
            draft?.delete()
        }
    }

    private fun initData() {
        val intent = intent
        val jsonData = intent.getStringExtra("data")
        replyInfoBean = GsonUtil.getGson().fromJson(jsonData, ReplyInfoBean::class.java)
        val draft = where("hash = ?", replyInfoBean?.hash() ?: "")
            .findFirst(Draft::class.java)
        if (draft != null) {
            content = draft.content
        }
    }

    protected fun initView() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val mItemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.Callback() {
            override fun clearView(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ) {
                super.clearView(recyclerView, viewHolder)
                viewHolder.itemView.setBackgroundColor(Color.TRANSPARENT)
            }

            override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
                super.onSelectedChanged(viewHolder, actionState)
                if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
                    viewHolder!!.itemView.setBackgroundColor(
                        Util.getColorByAttr(
                            this@ReplyActivity,
                            R.attr.colorControlHighlight,
                            R.color.transparent
                        )
                    )
                }
            }

            override fun getMovementFlags(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ): Int {
                var dragFlags = 0
                var swiped = 0
                if (viewHolder.adapterPosition < insertPhotoAdapter.itemCount - 1) {
                    swiped = ItemTouchHelper.UP or ItemTouchHelper.DOWN
                    if (viewHolder.adapterPosition < insertPhotoAdapter.itemCount - 2 && viewHolder.adapterPosition > 0) {
                        dragFlags = ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT
                    } else if (viewHolder.adapterPosition == insertPhotoAdapter.itemCount - 2) {
                        dragFlags = ItemTouchHelper.LEFT
                    } else if (viewHolder.adapterPosition == 0) {
                        dragFlags = ItemTouchHelper.RIGHT
                    }
                }
                return makeMovementFlags(dragFlags, swiped)
            }

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val oldPosition = viewHolder.adapterPosition
                val newPosition = target.adapterPosition
                if (newPosition < insertPhotoAdapter.itemCount - 1) {
                    if (oldPosition < newPosition) {
                        for (i in oldPosition until newPosition) {
                            insertPhotoAdapter.swap(i, i + 1)
                        }
                    } else {
                        for (i in oldPosition downTo newPosition + 1) {
                            insertPhotoAdapter.swap(i, i - 1)
                        }
                    }
                    insertPhotoAdapter.notifyItemMoved(oldPosition, newPosition)
                    return true
                }
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                insertPhotoAdapter.remove(position)
            }
        })
        mItemTouchHelper.attachToRecyclerView(insertView)
        findViewById<View>(R.id.activity_reply_root).setOnClickListener(this)
        findViewById<View>(R.id.activity_reply_layout).setOnClickListener(this)
        toolbar.setNavigationIcon(R.drawable.ic_reply_toolbar_round_close)
        if (replyInfoBean!!.pid == null && replyInfoBean!!.floorNum == null) {
            insertImageBtn.visibility = View.VISIBLE
        } else {
            insertImageBtn.visibility = View.INVISIBLE
        }
        insertView.adapter = insertPhotoAdapter
        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = RecyclerView.HORIZONTAL
        insertView.layoutManager = layoutManager
        if (replyInfoBean!!.replyUser != null) {
            editText.hint = getString(R.string.hint_reply, replyInfoBean!!.replyUser)
        }
        val tabLayout = findViewById<TabLayout>(R.id.activity_reply_emotion_tab)
        val emotionViewPagerAdapter = TabViewPagerAdapter()
        val classicEmotionGridView = GridView(this)
        val emojiEmotionGridView = GridView(this)
        EmotionViewFactory.initGridView(
            this,
            EmotionUtil.EMOTION_CLASSIC_WEB_TYPE,
            classicEmotionGridView
        )
        EmotionViewFactory.initGridView(
            this,
            EmotionUtil.EMOTION_EMOJI_WEB_TYPE,
            emojiEmotionGridView
        )
        emotionViewPagerAdapter.addView(
            classicEmotionGridView,
            getString(R.string.title_emotion_classic)
        )
        emotionViewPagerAdapter.addView(
            emojiEmotionGridView,
            getString(R.string.title_emotion_emoji)
        )
        emotionViewPager.adapter = emotionViewPagerAdapter
        tabLayout.setupWithViewPager(emotionViewPager)
        if (content != null) {
            editText.mgr.disable()
            editText.setText(
                StringUtil.getEmotionContent(
                    EmotionUtil.EMOTION_ALL_WEB_TYPE,
                    editText,
                    content
                )
            )
            editText.mgr.enable()
        }
        initListener()
    }

    private fun canSend(): Boolean {
        return editText.text.toString().isNotEmpty() ||
                insertPhotoAdapter.getFileList().isNotEmpty()
    }

    private fun needUpload(): Boolean {
        var needUpload = false
        if (replyInfoBean!!.isSubFloor) {
            return false
        }
        for (photoInfoBean in insertPhotoAdapter.getFileList()) {
            if (photoInfoBean.webUploadPicBean == null) {
                needUpload = true
                break
            }
        }
        return needUpload
    }

    private val replyContent: String
        get() {
            val builder = StringBuilder()
            if (replyInfoBean!!.isSubFloor && replyInfoBean!!.replyUser != null) {
                builder.append("回复 ")
                    .append(replyInfoBean!!.replyUser)
                    .append(" :")
            }
            builder.append(editText.text)
            if (appPreferences.littleTail != null) {
                builder.append("\n")
                    .append(appPreferences.littleTail)
            }
            return builder.toString()
        }

    private fun getImageInfo(callback: ReplyContentCallback) {
        val builder = StringBuilder()
        if (hasPhoto()) {
            if (!needUpload()) {
                for (photoInfoBean in insertPhotoAdapter.getFileList()) {
                    if (photoInfoBean.webUploadPicBean != null) {
                        builder.append(photoInfoBean.webUploadPicBean.imageInfo)
                        if (insertPhotoAdapter.getFileList().size - 1 > insertPhotoAdapter.getFileList()
                                .indexOf(
                                    photoInfoBean
                                )
                        ) {
                            builder.append("|")
                        }
                    }
                }
                callback.onSuccess(builder.toString())
                return
            }
            UploadHelper.with(this)
                .setFileList(insertPhotoAdapter.getFileList())
                .setCallback(object : UploadCallback {
                    override fun onSuccess(photoInfoBeans: List<PhotoInfoBean>) {
                        for (photoInfoBean in photoInfoBeans) {
                            if (photoInfoBean.webUploadPicBean != null) {
                                builder.append(photoInfoBean.webUploadPicBean.imageInfo)
                                if (photoInfoBeans.size - 1 > photoInfoBeans.indexOf(photoInfoBean)) {
                                    builder.append("|")
                                }
                            }
                        }
                        Log.i(TAG, "onSuccess: $builder")
                        callback.onSuccess(builder.toString())
                    }

                    override fun onStart(total: Int) {
                        callback.onStart(total)
                    }

                    override fun onProgress(current: Int, total: Int) {
                        callback.onProgress(current, total)
                    }

                    override fun onFailure(error: String) {
                        callback.onFailure(error)
                    }
                })
                .start()
            return
        }
        callback.onSuccess("")
    }

    private fun hasPhoto(): Boolean {
        return insertPhotoAdapter.getFileList()
            .isNotEmpty()
    }

    private fun setEnabled(imageButton: TintImageView, enable: Boolean) {
        imageButton.isClickable = enable
        imageButton.isEnabled = enable
    }

    private fun initListener() {
        val undo = findViewById<TintImageView>(R.id.activity_reply_edit_undo)
        val redo = findViewById<TintImageView>(R.id.activity_reply_edit_redo)
        val clear = findViewById<TintImageView>(R.id.activity_reply_edit_clear)
        undo.setOnClickListener(this)
        setEnabled(undo, false)
        redo.setOnClickListener(this)
        setEnabled(redo, false)
        clear.setOnClickListener(this)
        setEnabled(clear, false)
        editText.addTextChangedListener(object : TextWatcherAdapter() {
            override fun afterTextChanged(editable: Editable) {
                super.afterTextChanged(editable)
                setEnabled(undo, editText.canUndo())
                setEnabled(redo, editText.canRedo())
                setEnabled(clear, !TextUtils.isEmpty(editable))
                if (sendItem != null) sendItem!!.isEnabled = canSend()
            }
        })
        KeyboardUtil.attach(this, panelFrameLayout)
        KPSwitchConflictUtil.attach(
            panelFrameLayout,
            editText,
            SubPanelAndTrigger(emotionView, emotionBtn),
            SubPanelAndTrigger(insertImageView, insertImageBtn)
        )
        EmotionUtil.GlobalOnItemClickManagerUtil.getInstance().attachToEditText(editText)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_reply_toolbar, menu)
        sendItem = menu.findItem(R.id.menu_send)
        sendItem?.isEnabled = content?.isNotEmpty() ?: false
        return super.onCreateOptionsMenu(menu)
    }

    override fun finish() {
        overridePendingTransition(R.anim.in_bottom, R.anim.out_bottom)
        super.finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_send) {
            realReply()
        }
        return super.onOptionsItemSelected(item)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun getBSK(tbs: String, callback: (String) -> Unit) {
        mWebView = WebView(this)
        mWebView!!.settings.javaScriptEnabled = true
        webViewContainer.addView(mWebView)
        mWebView!!.evaluateJavascript(
            AssetUtil.getStringFromAsset(
                this,
                "new_bsk.js"
            )
        ) {
            mWebView!!.evaluateJavascript(
                "get_bsk_data(\"$tbs\")"
            ) { value: String ->
                destroyWebView()
                callback(value.replace("\"".toRegex(), ""))
            }
        }
    }


    private fun realReply( /*String code, String md5*/) {
        if (replyInfoBean == null && replyInfoBean!!.forumId == null) {
            toastShort(R.string.toast_data_error)
            return
        }
        loadingDialog = LoadingDialog(this)
        loadingDialog!!.show()
        getImageInfo(object : ReplyContentCallback {
            override fun onSuccess(data: String) {
                loadingDialog!!.setTipText("正在提交...")
                getBSK(replyInfoBean!!.tbs) {
                    launchIO {
                        when {
                            replyInfoBean!!.pid == null && replyInfoBean!!.floorNum == null -> {
                                TiebaApi.getInstance().webReplyAsync(
                                    replyInfoBean!!.forumId,
                                    replyInfoBean!!.forumName,
                                    replyInfoBean!!.threadId,
                                    replyInfoBean!!.tbs,
                                    replyContent,
                                    data,
                                    replyInfoBean!!.nickName,
                                    replyInfoBean!!.pn,
                                    it
                                )
                            }
                            replyInfoBean!!.isSubFloor && replyInfoBean!!.spid != null -> {
                                TiebaApi.getInstance().webReplyAsync(
                                    replyInfoBean!!.forumId, replyInfoBean!!.forumName,
                                    replyInfoBean!!.threadId,
                                    replyInfoBean!!.tbs,
                                    replyContent,
                                    data,
                                    replyInfoBean!!.nickName,
                                    replyInfoBean!!.pid,
                                    replyInfoBean!!.spid,
                                    replyInfoBean!!.floorNum,
                                    replyInfoBean!!.pn,
                                    it
                                )
                            }
                            else -> {
                                TiebaApi.getInstance().webReplyAsync(
                                    replyInfoBean!!.forumId, replyInfoBean!!.forumName,
                                    replyInfoBean!!.threadId,
                                    replyInfoBean!!.tbs,
                                    replyContent,
                                    data,
                                    replyInfoBean!!.nickName,
                                    replyInfoBean!!.pid,
                                    replyInfoBean!!.floorNum,
                                    replyInfoBean!!.pn,
                                    it
                                )
                            }
                        }.doIfSuccess {
                            if (loadingDialog != null) loadingDialog!!.cancel()
                            Toast.makeText(
                                this@ReplyActivity,
                                R.string.toast_reply_success,
                                Toast.LENGTH_SHORT
                            ).show()
                            sendBroadcast(
                                Intent()
                                    .setAction(ThreadActivity.ACTION_REPLY_SUCCESS)
                                    .putExtra(
                                        "pid",
                                        if (replyInfoBean!!.pid != null) replyInfoBean!!.pid else it.data.pid
                                    )
                            )
                            replySuccess = true
                            finish()
                        }.doIfFailure {
                            if (loadingDialog != null) loadingDialog!!.cancel()
                            KeyboardUtil.hideKeyboard(panelFrameLayout)
                            showErrorSnackBar(panelFrameLayout, it)
                        }
                    }
                }
            }

            override fun onStart(total: Int) {
                loadingDialog!!.setTipText("正在上传图片...(0/$total)")
            }

            override fun onProgress(current: Int, total: Int) {
                loadingDialog!!.setTipText("正在上传图片...($current/$total)")
            }

            override fun onFailure(error: String) {
                loadingDialog!!.cancel()
                Toast.makeText(this@ReplyActivity, error, Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.activity_reply_root -> finish()
            R.id.activity_reply_edit_undo -> editText.undo()
            R.id.activity_reply_edit_redo -> editText.redo()
            R.id.activity_reply_edit_clear -> editText.setText(null)
        }
    }

    companion object {
        const val TAG = "ReplyActivity"
    }
}