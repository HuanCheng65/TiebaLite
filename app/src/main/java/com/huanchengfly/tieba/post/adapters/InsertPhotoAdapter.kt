package com.huanchengfly.tieba.post.adapters

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.components.MyViewHolder
import com.huanchengfly.tieba.post.components.transformations.RadiusTransformation
import com.huanchengfly.tieba.post.models.PhotoInfoBean
import com.huanchengfly.tieba.post.utils.PermissionUtils
import com.huanchengfly.tieba.post.utils.ThemeUtil
import com.zhihu.matisse.Matisse
import com.zhihu.matisse.MimeType
import com.zhihu.matisse.engine.impl.GlideEngine
import java.util.*

class InsertPhotoAdapter(private val mContext: Context) : RecyclerView.Adapter<MyViewHolder>() {
    private var fileList: MutableList<PhotoInfoBean> = ArrayList<PhotoInfoBean>()

    fun remove(position: Int) {
        fileList.removeAt(position)
        notifyItemRemoved(position)
    }

    fun swap(oldPosition: Int, newPosition: Int) {
        Collections.swap(fileList, oldPosition, newPosition)
    }

    fun getFileList(): List<PhotoInfoBean> {
        return fileList
    }

    fun setFileList(fileList: MutableList<PhotoInfoBean>) {
        this.fileList = fileList
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return if (position >= fileList.size) TYPE_INSERT else TYPE_IMAGE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        if (viewType == TYPE_INSERT) {
            val view: View = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_insert_more, parent, false)
            return MyViewHolder(view)
        }
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_insert_image, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        if (holder.itemViewType == TYPE_IMAGE) {
            val photoInfoBean: PhotoInfoBean = fileList[position]
            val imageView: ImageView = holder.getView(R.id.image_preview)
            Glide.with(mContext)
                .load(photoInfoBean.fileUri)
                .apply(RequestOptions.bitmapTransform(RadiusTransformation()))
                .into(imageView)
        } else if (holder.itemViewType == TYPE_INSERT) {
            if (fileList.size < 10) {
                holder.setItemOnClickListener {
                    if (mContext is AppCompatActivity && mContext is MatisseLauncherProvider) {
                        askPermission {
                            Matisse.from(mContext)
                                .choose(MimeType.ofImage())
                                .countable(true)
                                .maxSelectable(10 - fileList.size)
                                .theme(if (ThemeUtil.isNightMode()) R.style.Matisse_Dracula else R.style.Matisse_Zhihu)
                                .imageEngine(GlideEngine())
                                .forResult(mContext.getMatisseLauncher())
                        }
                    }
                }
            } else {
                Toast.makeText(mContext, R.string.toast_max_selectable, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun askPermission(granted: () -> Unit) {
        PermissionUtils.askPermission(
            mContext,
            PermissionUtils.Permission(
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                    listOf(
                        PermissionUtils.READ_EXTERNAL_STORAGE,
                        PermissionUtils.WRITE_EXTERNAL_STORAGE
                    )
                } else {
                    listOf(PermissionUtils.READ_EXTERNAL_STORAGE)
                },
                mContext.getString(R.string.tip_permission_storage)
            ),
            R.string.toast_no_permission_insert_photo,
            granted
        )
    }

    override fun getItemCount(): Int {
        return fileList.size + 1
    }

    fun getItem(position: Int): PhotoInfoBean {
        return fileList[position]
    }

    interface MatisseLauncherProvider {
        fun getMatisseLauncher(): ActivityResultLauncher<Intent>
    }

    class MatisseResultContract : ActivityResultContract<Intent, List<Uri>>() {
        override fun createIntent(context: Context, input: Intent): Intent {
            return input
        }

        override fun parseResult(resultCode: Int, intent: Intent?): List<Uri> {
            if (resultCode != RESULT_OK) {
                return emptyList()
            }
            return Matisse.obtainResult(intent)
        }
    }

    companion object {
        const val TYPE_IMAGE = 0
        const val TYPE_INSERT = 1
    }
}