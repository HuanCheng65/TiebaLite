package com.huanchengfly.tieba.post.fragments

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import butterknife.BindView
import com.github.panpf.sketch.displayImage
import com.github.panpf.sketch.viewability.showRingProgressIndicator
import com.github.panpf.sketch.zoom.SketchZoomImageView
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.dpToPx
import com.huanchengfly.tieba.post.dpToPxFloat
import com.huanchengfly.tieba.post.models.PhotoViewBean
import com.huanchengfly.tieba.post.toastShort
import com.huanchengfly.tieba.post.ui.common.theme.utils.ThemeUtils
import com.huanchengfly.tieba.post.utils.DialogUtil
import com.huanchengfly.tieba.post.utils.ImageUtil

class PhotoViewFragment : BaseFragment() {
    @JvmField
    @BindView(R.id.big_image_view)
    var bigImageView: SketchZoomImageView? = null
    var photoViewBean: PhotoViewBean? = null
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            photoViewBean = arguments!!.getParcelable(ARG_INFO)!!
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_photo_view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (!canLoad()) return
        loadByBigImageView()
    }

    private fun showBottomBar(autoHide: Boolean) {
        if (attachContext is OnChangeBottomBarVisibilityListener) {
            (attachContext as OnChangeBottomBarVisibilityListener).onShow(autoHide)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun loadByBigImageView() {
        if (!canLoad()) return
        var url = photoViewBean!!.originUrl
        if (url == null) {
            url = photoViewBean!!.url
        }
        if (url == null) {
            return
        }
        bigImageView!!.visibility = View.VISIBLE
        bigImageView!!.showRingProgressIndicator(48.dpToPx(), 2f.dpToPxFloat(), ThemeUtils.getColorByAttr(attachContext, R.attr.colorPrimary))
        bigImageView!!.displayImage(url) {
            lifecycle(viewLifecycleOwner.lifecycle)
            listener(
                onError = { _, _ ->
                    attachContext.toastShort(R.string.toast_load_failed)
                }
            )
        }
        bigImageView!!.setOnTouchListener { _, _ ->
            showBottomBar(true)
            false
        }
        bigImageView!!.setOnLongClickListener {
            showBottomBar(false)
            openDialog(photoViewBean)
            true
        }
        bigImageView!!.setOnClickListener {
            if (activity != null) {
                activity!!.finish()
            }
        }
    }

    private fun canLoad(): Boolean {
        return activity != null && !activity!!.isDestroyed && !activity!!.isFinishing
    }

    private fun openDialog(photoViewBean: PhotoViewBean?) {
        if (!canLoad()) {
            return
        }
        val strArray = arrayOf(attachContext.getString(R.string.menu_save_photo))
        DialogUtil.build(attachContext)
            .setItems(strArray) { _: DialogInterface?, which: Int ->
                when (which) {
                    0 -> ImageUtil.download(attachContext, photoViewBean!!.originUrl)
                }
            }
            .create()
            .show()
    }

    interface OnChangeBottomBarVisibilityListener {
        fun onShow(autoHide: Boolean)
        fun onHide()
    }

    companion object {
        val TAG: String = PhotoViewFragment::class.java.simpleName
        private const val ARG_INFO = "info"

        @JvmStatic
        fun newInstance(photoViewBean: PhotoViewBean?): PhotoViewFragment {
            val fragment = PhotoViewFragment()
            val args = Bundle()
            args.putParcelable(ARG_INFO, photoViewBean)
            fragment.arguments = args
            return fragment
        }
    }
}