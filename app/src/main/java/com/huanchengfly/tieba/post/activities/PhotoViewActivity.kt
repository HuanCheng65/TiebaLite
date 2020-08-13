package com.huanchengfly.tieba.post.activities

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import butterknife.BindView
import com.google.android.material.bottomappbar.BottomAppBar
import com.huanchengfly.theme.utils.ThemeUtils
import com.huanchengfly.tieba.api.TiebaApi.getInstance
import com.huanchengfly.tieba.api.models.PicPageBean
import com.huanchengfly.tieba.api.models.PicPageBean.ImgInfoBean
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.activities.base.BaseActivity
import com.huanchengfly.tieba.post.adapters.PhotoViewAdapter
import com.huanchengfly.tieba.post.base.Config
import com.huanchengfly.tieba.post.fragments.PhotoViewFragment.OnChangeBottomBarVisibilityListener
import com.huanchengfly.tieba.post.models.PhotoViewBean
import com.huanchengfly.tieba.post.utils.AnimUtil
import com.huanchengfly.tieba.post.utils.ImageUtil
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class PhotoViewActivity : BaseActivity(), OnChangeBottomBarVisibilityListener, Toolbar.OnMenuItemClickListener {
    @BindView(R.id.counter)
    lateinit var mCounter: TextView
    @BindView(R.id.bottom_app_bar)
    lateinit var mAppBar: BottomAppBar
    @BindView(R.id.view_pager)
    lateinit var mViewPager: ViewPager2

    private lateinit var photoViewBeans: MutableList<PhotoViewBean>
    private lateinit var mAdapter: PhotoViewAdapter
    private var startPosition = 0
    private var lastIndex = 0
    private var seeLz = false
    private var isFrs = false
    private var mLoading = false
    private var loadFinished = false
    private val autoHideRunnable = Runnable { onHide() }
    private var amount: String? = null
    private var forumName: String? = null
    private var forumId: String? = null
    private var threadId: String? = null
    private var objType: String? = null

    private fun loadMore() {
        if (loadFinished) {
            return
        }
        if (mLoading) {
            return
        }
        mLoading = true
        val lastBean = photoViewBeans[photoViewBeans.size - 1]
        getInstance().picPage(
                forumId!!,
                forumName!!,
                threadId!!,
                seeLz,
                ImageUtil.getPicId(lastBean.originUrl), photoViewBeans.size.toString(),
                objType!!,
                false
        ).enqueue(object : Callback<PicPageBean?> {
            override fun onResponse(call: Call<PicPageBean?>, response: Response<PicPageBean?>) {
                val data = response.body()!!
                mLoading = false
                amount = data.picAmount ?: "${photoViewBeans.size}"
                updateCounter(mViewPager.currentItem)
                val picBeans: MutableList<PicPageBean.PicBean> = ArrayList()
                val imgInfoBeans: MutableList<ImgInfoBean> = ArrayList()
                if (data.picList?.isNotEmpty()!!) {
                    val index = data.picList.last().overAllIndex?.toInt()
                    if (index != null) {
                        loadFinished = index >= amount!!.toInt()
                    }
                    picBeans.addAll(data.picList)
                    picBeans.forEach {
                        it.img?.original?.let { it1 -> imgInfoBeans.add(it1) }
                    }
                    lastIndex = picBeans.first().overAllIndex?.toInt()!!
                    for (photoViewBean in photoViewBeans) {
                        val ind = lastIndex - (photoViewBeans.size - 1 - photoViewBeans.indexOf(photoViewBean))
                        photoViewBean.index = ind.toString()
                    }
                    picBeans.removeAt(0)
                    imgInfoBeans.removeAt(0)
                    val beans = imgInfoBeans.mapIndexed { i, it ->
                        PhotoViewBean(it.bigCdnSrc,
                                it.originalSrc,
                                (it.height ?: "0").toInt() > Config.EXACT_SCREEN_HEIGHT,
                                picBeans[i].overAllIndex,
                                "2" == it.format)
                    }.toMutableList()
                    mAdapter.insert(beans)
                    photoViewBeans = mAdapter.data
                    mAdapter.notifyDataSetChanged()
                    updateCounter(mViewPager.currentItem)
                } else {
                    loadFinished = true
                }
            }

            override fun onFailure(call: Call<PicPageBean?>, t: Throwable) {
                mLoading = false
            }
        })
    }

    private fun loadFrs() {
        forumName = intent.getStringExtra(EXTRA_FORUM_NAME)
        forumId = intent.getStringExtra(EXTRA_FORUM_ID)
        threadId = intent.getStringExtra(EXTRA_THREAD_ID)
        seeLz = intent.getBooleanExtra(EXTRA_SEE_LZ, false)
        objType = intent.getStringExtra(EXTRA_OBJ_TYPE)
        loadMore()
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_photo_view
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        mAppBar.setOnMenuItemClickListener(this)
        val overflowIcon = mAppBar.overflowIcon
        if (overflowIcon != null) {
            mAppBar.overflowIcon = ThemeUtils.tintDrawable(overflowIcon, Color.WHITE)
        }
        isFrs = intent.getBooleanExtra(EXTRA_IS_FRS, false)
        photoViewBeans = mutableListOf()
        startPosition = intent.getIntExtra(EXTRA_POSITION, 0)
        val parcelables = intent.getParcelableArrayExtra(EXTRA_BEANS)
        photoViewBeans.addAll(parcelables.map { it as PhotoViewBean })
        amount = photoViewBeans.size.toString()
        mAdapter = PhotoViewAdapter(this, photoViewBeans)
        mViewPager.adapter = mAdapter
        mViewPager.setCurrentItem(startPosition, false)
        updateCounter()
        mViewPager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                onShow(true)
            }

            override fun onPageSelected(position: Int) {
                updateCounter(position)
                onShow(true)
                if (!mLoading && isFrs && position >= photoViewBeans.size - 1) {
                    loadMore()
                }
            }
        })
        if (isFrs) {
            loadFrs()
        }
    }

    override fun isNeedImmersionBar(): Boolean {
        return false
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.anim_fade_in, R.anim.anim_fade_out)
    }

    override fun onBackPressed() {
        finish()
    }

    private fun updateCounter(position: Int = startPosition) {
        onShow(true)
        if (photoViewBeans.size <= 1) {
            mCounter.text = null
        } else if (isFrs && lastIndex > 0) {
            val index = photoViewBeans[position].index
            mCounter.text = getString(R.string.tip_position, (index ?: position
            + 1).toString(), amount)
        } else {
            mCounter.text = getString(R.string.tip_position, (position + 1).toString(), amount)
        }
    }

    override fun onShow(autoHide: Boolean) {
        handler.removeCallbacks(autoHideRunnable)
        if (mAppBar.visibility == View.VISIBLE) {
            if (autoHide) {
                handler.postDelayed(autoHideRunnable, DEFAULT_HIDE_DELAY.toLong())
            }
            return
        }
        AnimUtil.alphaIn(mAppBar)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        mAppBar.visibility = View.VISIBLE
                    }

                    override fun onAnimationEnd(animation: Animator, isReverse: Boolean) {
                        if (autoHide) {
                            handler.postDelayed(autoHideRunnable, DEFAULT_HIDE_DELAY.toLong())
                        }
                    }
                })
                .start()
    }

    override fun onHide() {
        if (mAppBar.visibility == View.GONE || mViewPager.orientation != ViewPager2.ORIENTATION_HORIZONTAL) {
            return
        }
        AnimUtil.alphaOut(mAppBar)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        mAppBar.visibility = View.GONE
                    }
                })
                .start()
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_toggle_orientation -> {
                item.setIcon(if (mViewPager.orientation == ViewPager2.ORIENTATION_HORIZONTAL) R.drawable.ic_round_view_day_white else R.drawable.ic_round_view_carousel_white)
                item.setTitle(if (mViewPager.orientation == ViewPager2.ORIENTATION_HORIZONTAL) R.string.title_comic_mode_on else R.string.title_comic_mode)
                Toast.makeText(this, if (mViewPager.orientation == ViewPager2.ORIENTATION_HORIZONTAL) R.string.toast_comic_mode_on else R.string.toast_comic_mode_off, Toast.LENGTH_SHORT).show()
                mViewPager.orientation = if (mViewPager.orientation == ViewPager2.ORIENTATION_HORIZONTAL) ViewPager2.ORIENTATION_VERTICAL else ViewPager2.ORIENTATION_HORIZONTAL
                return true
            }
            R.id.menu_save_image -> {
                ImageUtil.download(this, mAdapter.getBean(mViewPager.currentItem).originUrl, mAdapter.getBean(mViewPager.currentItem).isGif)
                return true
            }
            R.id.menu_share -> {
                Toast.makeText(this, R.string.toast_preparing_share_pic, Toast.LENGTH_SHORT).show()
                ImageUtil.download(this, mAdapter.getBean(mViewPager.currentItem).originUrl, mAdapter.getBean(mViewPager.currentItem).isGif, true) { uri: Uri? ->
                    val intent = Intent(Intent.ACTION_SEND)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        intent.putExtra(Intent.EXTRA_STREAM, uri)
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    } else {
                        intent.putExtra(Intent.EXTRA_STREAM, uri)
                    }
                    intent.type = Intent.normalizeMimeType("image/jpeg")
                    val chooser = Intent.createChooser(intent, getString(R.string.title_share_pic))
                    if (intent.resolveActivity(packageManager) != null) {
                        startActivity(chooser)
                    }
                }
                return true
            }
        }
        return false
    }

    companion object {
        val TAG = PhotoViewActivity::class.java.simpleName
        const val EXTRA_BEANS = "beans"
        const val EXTRA_POSITION = "position"
        const val EXTRA_FORUM_ID = "forum_id"
        const val EXTRA_FORUM_NAME = "forum_name"
        const val EXTRA_THREAD_ID = "thread_id"
        const val EXTRA_SEE_LZ = "see_lz"
        const val EXTRA_IS_FRS = "is_frs"
        const val EXTRA_OBJ_TYPE = "obj_type"
        const val DEFAULT_HIDE_DELAY = 3000
        const val OBJ_TYPE_THREAD_PAGE = "pb"
        const val OBJ_TYPE_FORUM_PAGE = "frs"
        private val handler = Handler()

        @JvmStatic
        fun launch(context: Context, photoViewBean: PhotoViewBean) {
            launch(context, arrayOf(photoViewBean))
        }

        @JvmStatic
        fun launch(context: Context, photoViewBeanList: List<PhotoViewBean>) {
            launch(context, photoViewBeanList.toTypedArray(), 0)
        }

        @JvmStatic
        fun launch(context: Context, photoViewBeanList: List<PhotoViewBean>, position: Int) {
            launch(context, photoViewBeanList.toTypedArray(), position)
        }

        @JvmStatic
        @JvmOverloads
        fun launch(context: Context, photoViewBeans: Array<PhotoViewBean>, position: Int = 0) {
            context.startActivity(Intent(context, PhotoViewActivity::class.java)
                    .putExtra(EXTRA_BEANS, photoViewBeans)
                    .putExtra(EXTRA_POSITION, position)
                    .putExtra(EXTRA_IS_FRS, false))
        }

        @JvmStatic
        fun launch(context: Context,
                   photoViewBeans: Array<PhotoViewBean?>?,
                   position: Int,
                   forumName: String?,
                   forumId: String?,
                   threadId: String?,
                   seeLz: Boolean,
                   objType: String?) {
            context.startActivity(Intent(context, PhotoViewActivity::class.java)
                    .putExtra(EXTRA_BEANS, photoViewBeans)
                    .putExtra(EXTRA_POSITION, position)
                    .putExtra(EXTRA_IS_FRS, true)
                    .putExtra(EXTRA_FORUM_NAME, forumName)
                    .putExtra(EXTRA_FORUM_ID, forumId)
                    .putExtra(EXTRA_THREAD_ID, threadId)
                    .putExtra(EXTRA_SEE_LZ, seeLz)
                    .putExtra(EXTRA_OBJ_TYPE, objType))
        }
    }
}