package com.huanchengfly.tieba.post.components

import android.graphics.BitmapFactory
import android.util.Log
import com.huanchengfly.tieba.post.api.BOUNDARY
import com.huanchengfly.tieba.post.api.booleanToString
import com.huanchengfly.tieba.post.api.models.UploadPictureResultBean
import com.huanchengfly.tieba.post.api.retrofit.RetrofitTiebaApi
import com.huanchengfly.tieba.post.api.retrofit.body.MyMultipartBody
import com.huanchengfly.tieba.post.api.retrofit.body.buildMultipartBody
import com.huanchengfly.tieba.post.api.retrofit.exception.TiebaException
import com.huanchengfly.tieba.post.api.retrofit.exception.getErrorCode
import com.huanchengfly.tieba.post.api.retrofit.exception.getErrorMessage
import com.huanchengfly.tieba.post.utils.MD5Util
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.runningFold
import kotlinx.coroutines.withContext
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.RandomAccessFile

class ImageUploader(
    private val forumName: String,
    private val chunkSize: Int = DEFAULT_CHUNK_SIZE
) {
    companion object {
        const val DEFAULT_CHUNK_SIZE = 512000

        const val IMAGE_MAX_SIZE = 5242880
        const val ORIGIN_IMAGE_MAX_SIZE = 10485760

        const val PIC_WATER_TYPE_NO = 0
        const val PIC_WATER_TYPE_USER_NAME = 1
        const val PIC_WATER_TYPE_FORUM_NAME = 2
    }

    fun uploadImages(
        filePaths: List<String>,
        isOriginImage: Boolean = false,
    ): Flow<List<UploadPictureResultBean>> {
        return filePaths.asFlow()
            .map { filePath ->
                uploadSinglePicture(filePath, isOriginImage)
            }
            .runningFold<UploadPictureResultBean, MutableList<UploadPictureResultBean>>(initial = mutableListOf()) { list, result ->
                list.add(result)
                list
            }
            .filter { it.size == filePaths.size }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun uploadSinglePicture(
        filePath: String,
        isOriginImage: Boolean = false,
    ): UploadPictureResultBean {
        val option = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeFile(filePath, option)
        val width = option.outWidth
        val height = option.outHeight
        check(width > 0 && height > 0) { "图片宽高不正确" }
        val file = File(filePath)
        val fileLength = file.length()
        val maxSize = if (isOriginImage) ORIGIN_IMAGE_MAX_SIZE else IMAGE_MAX_SIZE
        check(fileLength <= maxSize) { "图片大小超过限制" }
        val fileMd5 = MD5Util.toMd5(file)
        val isMultipleChunkSize = fileLength % chunkSize == 0L
        val totalChunkNum = fileLength / chunkSize + if (isMultipleChunkSize) 0 else 1
        Log.i("ImageUploader", "fileLength=$fileLength, totalChunkNum=$totalChunkNum")
        val requestBodies = (0 until totalChunkNum).map { chunk ->
            val isFinish = chunk == totalChunkNum - 1
            val curChunkSize = if (isFinish) {
                if (isMultipleChunkSize) {
                    chunkSize
                } else {
                    fileLength % chunkSize
                }
            } else {
                chunkSize
            }.toInt()
            val chunkBytes = ByteArray(curChunkSize)
            withContext(Dispatchers.IO) {
                RandomAccessFile(file, "r").use {
                    it.seek(chunk * chunkSize.toLong())
                    it.read(chunkBytes)
                }
            }
            buildMultipartBody(BOUNDARY) {
                setType(MyMultipartBody.FORM)
                addFormDataPart("alt", "json")
                addFormDataPart("chunkNo", "${chunk + 1}")
                if (forumName.isNotEmpty()) addFormDataPart("forum_name", forumName)
                addFormDataPart("groupId", "1")
                addFormDataPart("height", "$height")
                addFormDataPart("isFinish", isFinish.booleanToString())
                addFormDataPart("is_bjh", "0")
                addFormDataPart("pic_water_type", "2")
                addFormDataPart("resourceId", "$fileMd5$chunkSize")
                addFormDataPart("saveOrigin", isOriginImage.booleanToString())
                addFormDataPart("size", "$fileLength")
                if (forumName.isNotEmpty()) addFormDataPart("small_flow_fname", forumName)
                addFormDataPart("width", "$width")
                addFormDataPart("chunk", "file", chunkBytes.toRequestBody())
            }
        }
        return requestBodies.asFlow()
            .flatMapConcat { RetrofitTiebaApi.OFFICIAL_TIEBA_API.uploadPicture(it) }
            .catch {
                throw UploadPictureFailedException(it.getErrorCode(), it.getErrorMessage())
            }
            .onEach {
                Log.i("ImageUploader", "uploadSinglePicture: $it")
            }
            .last()
    }
}

class UploadPictureFailedException(
    override val code: Int = -1,
    override val message: String = "上传图片失败",
) : TiebaException(message)