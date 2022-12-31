package com.huanchengfly.tieba.post.adapters

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.components.MyViewHolder
import com.huanchengfly.tieba.post.components.transformations.RadiusTransformation
import com.huanchengfly.tieba.post.models.PhotoInfoBean
import com.huanchengfly.tieba.post.utils.PermissionUtils
import com.huanchengfly.tieba.post.utils.PickMediasRequest
import com.huanchengfly.tieba.post.utils.isPhotoPickerAvailable
import java.util.Collections

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
                    if (mContext is AppCompatActivity && mContext is PickMediasLauncherProvider) {
                        askPermission {
                            mContext.getPickMediasLauncher().launch(PickMediasRequest().apply {
                                maxItems = 10 - fileList.size
                                mediaType = PickMediasRequest.ImageOnly
                            })
                        }
                    }
                }
            } else {
                Toast.makeText(mContext, R.string.toast_max_selectable, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun askPermission(granted: () -> Unit) {
        if (isPhotoPickerAvailable()) {
            granted()
            return
        }
        PermissionUtils.askPermission(
            mContext,
            PermissionUtils.PermissionData(
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                    listOf(
                        PermissionUtils.READ_EXTERNAL_STORAGE,
                        PermissionUtils.WRITE_EXTERNAL_STORAGE
                    )
                } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                    listOf(
                        PermissionUtils.READ_EXTERNAL_STORAGE
                    )
                } else {
                    listOf(PermissionUtils.READ_MEDIA_IMAGES)
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

    interface PickMediasLauncherProvider {
        fun getPickMediasLauncher(): ActivityResultLauncher<PickMediasRequest>
    }

    companion object {
        const val TYPE_IMAGE = 0
        const val TYPE_INSERT = 1
    }
}