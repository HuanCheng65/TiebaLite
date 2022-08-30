package com.huanchengfly.tieba.post.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.ext.SdkExtensions.getExtensionVersion
import android.provider.MediaStore
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.app.AppCompatActivity
import com.huanchengfly.tieba.post.R
import com.zhihu.matisse.Matisse
import com.zhihu.matisse.MimeType
import com.zhihu.matisse.engine.impl.GlideEngine
import com.zhihu.matisse.ui.MatisseActivity

fun AppCompatActivity.registerPickMediasLauncher(callback: (List<Uri>) -> Unit): ActivityResultLauncher<PickMediasRequest> {
    return registerForActivityResult(
        PickMediasContract()
    ) {
        callback(it)
    }
}

fun isPhotoPickerAvailable(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        true
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        getExtensionVersion(Build.VERSION_CODES.R) >= 2
    } else {
        false
    }
}

private fun Intent.getClipDataUris(): List<Uri> {
    // Use a LinkedHashSet to maintain any ordering that may be
    // present in the ClipData
    val resultSet = LinkedHashSet<Uri>()
    data?.let { data ->
        resultSet.add(data)
    }
    val clipData = clipData
    if (clipData == null && resultSet.isEmpty()) {
        return emptyList()
    } else if (clipData != null) {
        for (i in 0 until clipData.itemCount) {
            val uri = clipData.getItemAt(i).uri
            if (uri != null) {
                resultSet.add(uri)
            }
        }
    }
    return ArrayList(resultSet)
}

private fun getMaxItems() = if (isPhotoPickerAvailable()) {
    MediaStore.getPickImagesMaxLimit()
} else {
    Integer.MAX_VALUE
}

class PickMediasRequest(
    var maxItems: Int = 1,
    var mediaType: MediaType = ImageAndVideo
) {
    sealed interface MediaType

    object ImageOnly : MediaType

    object VideoOnly : MediaType

    object ImageAndVideo : MediaType

    class SingleMimeType(val mimeType: String) : MediaType

    companion object {
        internal fun getMimeType(input: MediaType): String? {
            return when (input) {
                is ImageOnly -> "image/*"
                is VideoOnly -> "video/*"
                is SingleMimeType -> input.mimeType
                is ImageAndVideo -> null
            }
        }
    }
}

class PickMediasContract : ActivityResultContract<PickMediasRequest, List<Uri>>() {
    override fun createIntent(context: Context, input: PickMediasRequest): Intent {
        if (isPhotoPickerAvailable()) {
            return Intent(MediaStore.ACTION_PICK_IMAGES).apply {
                type = PickMediasRequest.getMimeType(input.mediaType)
                if (input.maxItems > 1) {
                    putExtra(MediaStore.EXTRA_PICK_IMAGES_MAX, input.maxItems)
                }
            }
        }
        val mimeType: Set<MimeType> = when (val mediaType = input.mediaType) {
            is PickMediasRequest.ImageOnly -> MimeType.ofImage()
            is PickMediasRequest.VideoOnly -> MimeType.ofVideo()
            is PickMediasRequest.SingleMimeType -> setOf(MimeType.valueOf(mediaType.mimeType))
            is PickMediasRequest.ImageAndVideo -> MimeType.ofAll()
        }
        Matisse.from(context as Activity)
            .choose(mimeType)
            .theme(if (ThemeUtil.isNightMode()) R.style.Matisse_Dracula else R.style.Matisse_Zhihu)
            .countable(input.maxItems > 1)
            .maxSelectable(input.maxItems)
            .imageEngine(GlideEngine())
        return Intent(context, MatisseActivity::class.java)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): List<Uri> {
        if (resultCode != Activity.RESULT_OK || intent == null) {
            return emptyList()
        }
        if (isPhotoPickerAvailable()) {
            return intent.getClipDataUris()
        }
        return Matisse.obtainResult(intent)
    }
}