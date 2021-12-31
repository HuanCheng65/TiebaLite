package com.huanchengfly.tieba.post.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
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
import com.huanchengfly.tieba.post.utils.*
import com.huanchengfly.tieba.post.widgets.edittext.widget.UndoableEditText
import com.huanchengfly.tieba.post.widgets.theme.TintConstraintLayout
import com.huanchengfly.tieba.post.widgets.theme.TintImageView
import com.zhihu.matisse.Matisse
import org.litepal.LitePal.where
import java.util.*

class ReplyActivity : BaseActivity(), View.OnClickListener {
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
    private var insertPhotoAdapter: InsertPhotoAdapter? = null
    private var sendItem: MenuItem? = null
    private var replySuccess = false
    private var content: String? = null
    private var mWebView: WebView? = null
    override val isNeedImmersionBar: Boolean
        get() = false

    override fun getLayoutId(): Int {
        return R.layout.activity_reply
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSwipeBackEnable(false)
        if (ThemeUtil.THEME_TRANSLUCENT == ThemeUtil.getTheme(this)) {
            val constraintLayout = findViewById(R.id.activity_reply_layout) as TintConstraintLayout
            constraintLayout.setBackgroundTintResId(0)
            ThemeUtil.setTranslucentBackground(constraintLayout)
        }
        Util.setStatusBarTransparent(this)
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        window.decorView.setBackgroundColor(resources.getColor(R.color.transparent))
        window.setBackgroundDrawableResource(R.drawable.bg_trans)
        initData()
        initView()
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
        val draft = where("hash = ?", replyInfoBean?.hash())
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
                if (viewHolder.adapterPosition < insertPhotoAdapter!!.itemCount - 1) {
                    swiped = ItemTouchHelper.UP or ItemTouchHelper.DOWN
                    if (viewHolder.adapterPosition < insertPhotoAdapter!!.itemCount - 2 && viewHolder.adapterPosition > 0) {
                        dragFlags = ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT
                    } else if (viewHolder.adapterPosition == insertPhotoAdapter!!.itemCount - 2) {
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
                if (newPosition < insertPhotoAdapter!!.itemCount - 1) {
                    if (oldPosition < newPosition) {
                        for (i in oldPosition until newPosition) {
                            insertPhotoAdapter!!.swap(i, i + 1)
                        }
                    } else {
                        for (i in oldPosition downTo newPosition + 1) {
                            insertPhotoAdapter!!.swap(i, i - 1)
                        }
                    }
                    insertPhotoAdapter!!.notifyItemMoved(oldPosition, newPosition)
                    return true
                }
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                insertPhotoAdapter!!.remove(position)
            }
        })
        mItemTouchHelper.attachToRecyclerView(insertView)
        findViewById(R.id.activity_reply_root).setOnClickListener(this)
        findViewById(R.id.activity_reply_layout).setOnClickListener(this)
        toolbar.setNavigationIcon(R.drawable.ic_reply_toolbar_round_close)
        if (replyInfoBean!!.pid == null && replyInfoBean!!.floorNum == null) {
            insertImageBtn.visibility = View.VISIBLE
        } else {
            insertImageBtn.visibility = View.INVISIBLE
        }
        insertPhotoAdapter = InsertPhotoAdapter(this)
        insertView.adapter = insertPhotoAdapter
        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = RecyclerView.HORIZONTAL
        insertView.layoutManager = layoutManager
        if (replyInfoBean!!.replyUser != null) {
            editText.hint = getString(R.string.hint_reply, replyInfoBean!!.replyUser)
        }
        val tabLayout = findViewById(R.id.activity_reply_emotion_tab) as TabLayout
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
                insertPhotoAdapter!!.fileList.size > 0
    }

    private fun needUpload(): Boolean {
        var needUpload = false
        if (replyInfoBean!!.isSubFloor) {
            return false
        }
        for (photoInfoBean in insertPhotoAdapter!!.fileList) {
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
                for (photoInfoBean in insertPhotoAdapter!!.fileList) {
                    if (photoInfoBean.webUploadPicBean != null) {
                        builder.append(photoInfoBean.webUploadPicBean.imageInfo)
                        if (insertPhotoAdapter!!.fileList.size - 1 > insertPhotoAdapter!!.fileList.indexOf(
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
                .setFileList(insertPhotoAdapter!!.fileList)
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
        return insertPhotoAdapter!!.fileList != null && insertPhotoAdapter!!.fileList.size > 0
    }

    private fun setEnabled(imageButton: TintImageView, enable: Boolean) {
        imageButton.isClickable = enable
        imageButton.isEnabled = enable
    }

    private fun initListener() {
        val undo = findViewById(R.id.activity_reply_edit_undo) as TintImageView
        val redo = findViewById(R.id.activity_reply_edit_redo) as TintImageView
        val clear = findViewById(R.id.activity_reply_edit_clear) as TintImageView
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
        EmotionUtil.GlobalOnItemClickManagerUtil.getInstance(this).attachToEditText(editText)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_CHOOSE && resultCode == RESULT_OK) {
            val uriList = Matisse.obtainResult(data)
            val photoInfoBeans = insertPhotoAdapter!!.fileList
            for (uri in uriList) {
                val infoBean = PhotoInfoBean(this, uri)
                photoInfoBeans.add(infoBean)
            }
            insertPhotoAdapter!!.fileList = photoInfoBeans
            sendItem!!.isEnabled = true
        }
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
        const val REQUEST_CODE_CHOOSE = 2
        const val TAG = "ReplyActivity"
    }
}