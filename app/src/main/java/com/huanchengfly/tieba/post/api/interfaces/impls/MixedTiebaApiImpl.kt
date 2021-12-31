package com.huanchengfly.tieba.post.api.interfaces.impls

import android.os.Build
import android.text.TextUtils
import com.huanchengfly.tieba.post.BaseApplication
import com.huanchengfly.tieba.post.api.ForumSortType
import com.huanchengfly.tieba.post.api.SearchThreadFilter
import com.huanchengfly.tieba.post.api.SearchThreadOrder
import com.huanchengfly.tieba.post.api.interfaces.ITiebaApi
import com.huanchengfly.tieba.post.api.models.*
import com.huanchengfly.tieba.post.api.models.web.ForumBean
import com.huanchengfly.tieba.post.api.models.web.HotMessageListBean
import com.huanchengfly.tieba.post.api.retrofit.ApiResult
import com.huanchengfly.tieba.post.api.retrofit.RetrofitTiebaApi
import com.huanchengfly.tieba.post.models.DislikeBean
import com.huanchengfly.tieba.post.models.MyInfoBean
import com.huanchengfly.tieba.post.models.PhotoInfoBean
import com.huanchengfly.tieba.post.toJson
import com.huanchengfly.tieba.post.utils.AccountUtil
import com.huanchengfly.tieba.post.utils.ImageUtil
import kotlinx.coroutines.Deferred
import retrofit2.Call
import java.io.FileInputStream
import java.io.IOException
import java.net.URLEncoder

object MixedTiebaApiImpl : ITiebaApi {
    override fun personalized(loadType: Int, page: Int): Call<PersonalizedBean> =
        RetrofitTiebaApi.MINI_TIEBA_API.personalized(loadType, page)

    override fun agree(
        threadId: String,
        postId: String
    ): Call<AgreeBean> =
        RetrofitTiebaApi.MINI_TIEBA_API.agree(postId, threadId)

    override fun disagree(
        threadId: String,
        postId: String
    ): Call<AgreeBean> = RetrofitTiebaApi.MINI_TIEBA_API.disagree(postId, threadId)

    override fun forumRecommend(): Call<ForumRecommend> =
        RetrofitTiebaApi.MINI_TIEBA_API.forumRecommend()

    override fun forumRecommendAsync(): Deferred<ApiResult<ForumRecommend>> =
        RetrofitTiebaApi.MINI_TIEBA_API.forumRecommendAsync()

    override fun forumPage(
        forumName: String, page: Int, sortType: ForumSortType, goodClassifyId: String?
    ): Call<ForumPageBean> =
        RetrofitTiebaApi.MINI_TIEBA_API.forumPage(forumName, page, sortType.value, goodClassifyId)

    override fun forumPageAsync(
        forumName: String,
        page: Int,
        sortType: ForumSortType,
        goodClassifyId: String?
    ): Deferred<ApiResult<ForumPageBean>> =
        RetrofitTiebaApi.MINI_TIEBA_API.forumPageAsync(
            forumName,
            page,
            sortType.value,
            goodClassifyId
        )

    override fun floor(
        threadId: String, page: Int, postId: String?, subPostId: String?
    ): Call<SubFloorListBean> =
        RetrofitTiebaApi.MINI_TIEBA_API.floor(threadId, page, postId, subPostId)

    override fun userLikeForum(
        uid: String, page: Int
    ): Call<UserLikeForumBean> {
        val myUid = AccountUtil.getUid(BaseApplication.instance)
        return RetrofitTiebaApi.MINI_TIEBA_API.userLikeForum(
            page = page,
            uid = myUid,
            friendUid = if (!TextUtils.equals(uid, myUid)) uid else null,
            is_guest = if (!TextUtils.equals(uid, myUid)) "1" else null

        )
    }

    override fun userPost(
        uid: String, page: Int, isThread: Boolean
    ): Call<UserPostBean> =
        RetrofitTiebaApi.MINI_TIEBA_API.userPost(uid, page, if (isThread) 1 else 0)

    override fun picPage(
        forumId: String,
        forumName: String,
        threadId: String,
        seeLz: Boolean,
        picId: String,
        picIndex: String,
        objType: String,
        prev: Boolean
    ): Call<PicPageBean> = RetrofitTiebaApi.MINI_TIEBA_API.picPage(
        forumId,
        forumName,
        threadId,
        picId,
        picIndex,
        objType,
        prev = if (prev) 1 else 0,
        not_see_lz = if (seeLz) 0 else 1
    )

    override fun profile(uid: String): Call<ProfileBean> =
        RetrofitTiebaApi.MINI_TIEBA_API.profile(uid)

    override fun unlikeForum(
        forumId: String, forumName: String, tbs: String
    ): Call<CommonResponse> = RetrofitTiebaApi.MINI_TIEBA_API.unlikeForum(forumId, forumName, tbs)

    override fun likeForum(
        forumId: String, forumName: String, tbs: String
    ): Call<LikeForumResultBean> =
        RetrofitTiebaApi.MINI_TIEBA_API.likeForum(forumId, forumName, tbs)

    override fun signAsync(forumName: String, tbs: String): Deferred<ApiResult<SignResultBean>> =
        RetrofitTiebaApi.MINI_TIEBA_API.signAsync(forumName, tbs)

    override fun delThread(
        forumId: String,
        forumName: String,
        threadId: String,
        tbs: String
    ): Call<CommonResponse> =
        RetrofitTiebaApi.MINI_TIEBA_API.delThread(forumId, forumName, threadId, tbs)

    override fun delPost(
        forumId: String,
        forumName: String,
        threadId: String,
        postId: String,
        tbs: String,
        isFloor: Boolean,
        delMyPost: Boolean
    ): Call<CommonResponse> =
        RetrofitTiebaApi.MINI_TIEBA_API.delPost(
            forumId,
            forumName,
            threadId,
            postId,
            tbs,
            is_floor = if (isFloor) 1 else 0,
            src = if (isFloor) 3 else 1,
            is_vip_del = if (delMyPost) 0 else 1,
            delete_my_post = if (delMyPost) 1 else 0
        )

    override fun searchPost(
        keyword: String, forumName: String, onlyThread: Boolean, page: Int, pageSize: Int
    ): Call<SearchPostBean> = RetrofitTiebaApi.MINI_TIEBA_API.searchPost(
        keyword,
        forumName,
        page,
        pageSize,
        only_thread = if (onlyThread) 1 else 0
    )

    override fun searchUser(keyword: String): Call<SearchUserBean> =
        RetrofitTiebaApi.MINI_TIEBA_API.searchUser(keyword)

    override fun msg(): Call<MsgBean> = RetrofitTiebaApi.NEW_TIEBA_API.msg()

    override fun threadStore(page: Int, pageSize: Int): Call<ThreadStoreBean> =
        RetrofitTiebaApi.NEW_TIEBA_API.threadStore(
            pageSize,
            pageSize * page,
            AccountUtil.getUid(BaseApplication.instance)
        )

    override fun removeStore(threadId: String, tbs: String): Call<CommonResponse> =
        RetrofitTiebaApi.NEW_TIEBA_API.removeStore(threadId, tbs)

    override fun addStore(threadId: String, postId: String, tbs: String): Call<CommonResponse> =
        RetrofitTiebaApi.NEW_TIEBA_API.addStore(
            listOf(
                CollectDataBean(
                    postId,
                    threadId,
                    "0",
                    "0"
                )
            ).toJson(), tbs
        )

    override fun replyMe(page: Int): Call<MessageListBean> =
        RetrofitTiebaApi.NEW_TIEBA_API.replyMe(page)

    override fun replyMeAsync(page: Int): Deferred<ApiResult<MessageListBean>> =
        RetrofitTiebaApi.NEW_TIEBA_API.replyMeAsync(page)

    override fun atMe(page: Int): Call<MessageListBean> = RetrofitTiebaApi.NEW_TIEBA_API.atMe(page)

    override fun atMeAsync(page: Int): Deferred<ApiResult<MessageListBean>> =
        RetrofitTiebaApi.NEW_TIEBA_API.atMeAsync(page)

    override fun agreeMe(page: Int): Call<MessageListBean> =
        RetrofitTiebaApi.NEW_TIEBA_API.agreeMe(page)

    override fun threadContent(
        threadId: String, page: Int, seeLz: Boolean, reverse: Boolean
    ): Call<ThreadContentBean> = RetrofitTiebaApi.OFFICIAL_TIEBA_API.threadContent(
        threadId,
        page,
        last = if (reverse) "1" else null,
        r = if (reverse) "1" else null,
        lz = if (seeLz) 1 else 0
    )

    override fun threadContent(
        threadId: String, postId: String?, seeLz: Boolean, reverse: Boolean
    ): Call<ThreadContentBean> = RetrofitTiebaApi.OFFICIAL_TIEBA_API.threadContent(
        threadId,
        postId,
        last = if (reverse) "1" else null,
        r = if (reverse) "1" else null,
        lz = if (seeLz) 1 else 0
    )

    override fun submitDislike(
        dislikeBean: DislikeBean,
        stoken: String
    ): Call<CommonResponse> =
        RetrofitTiebaApi.OFFICIAL_TIEBA_API.submitDislike(listOf(dislikeBean).toJson(), stoken)

    override fun follow(
        portrait: String, tbs: String
    ): Call<CommonResponse> = RetrofitTiebaApi.WEB_TIEBA_API.follow(
        "https://tieba.baidu.com/i/?portrait=${
            URLEncoder.encode(
                portrait,
                "UTF-8"
            )
        }&cuid=&auth=&uid=&ssid=&from=&uid=&pu=&bd_page_type=2&auth=&originid=&mo_device=1&tbs=${tbs}&action=follow&op=follow"
    )

    override fun unfollow(
        portrait: String, tbs: String
    ): Call<CommonResponse> = RetrofitTiebaApi.WEB_TIEBA_API.follow(
        "https://tieba.baidu.com/i/?portrait=${
            URLEncoder.encode(
                portrait,
                "UTF-8"
            )
        }&cuid=&auth=&uid=&ssid=&from=&uid=&pu=&bd_page_type=2&auth=&originid=&mo_device=1&tbs=${tbs}&action=follow&op=unfollow"
    )

    override fun hotMessageList(): Call<HotMessageListBean> =
        RetrofitTiebaApi.WEB_TIEBA_API.hotMessageList()

    override fun myInfo(cookie: String): Call<MyInfoBean> =
        RetrofitTiebaApi.WEB_TIEBA_API.myInfo(cookie)

    override fun myInfoAsync(cookie: String): Deferred<ApiResult<MyInfoBean>> =
        RetrofitTiebaApi.WEB_TIEBA_API.myInfoAsync(cookie)

    override fun searchForum(keyword: String): Call<SearchForumBean> =
        RetrofitTiebaApi.WEB_TIEBA_API.searchForum(keyword)

    override fun searchThread(
        keyword: String, page: Int, order: SearchThreadOrder, filter: SearchThreadFilter
    ): Call<SearchThreadBean> =
        RetrofitTiebaApi.WEB_TIEBA_API.searchThread(
            keyword,
            page,
            order.toString(),
            filter.toString()
        )

    override fun webUploadPic(photoInfoBean: PhotoInfoBean): Call<WebUploadPicBean> {
        var base64: String? = null
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            base64 = ImageUtil.imageToBase64(photoInfoBean.file)
        } else {
            try {
                BaseApplication.instance.contentResolver.openAssetFileDescriptor(
                    photoInfoBean.fileUri,
                    "r"
                )?.use { afd ->
                    base64 =
                        ImageUtil.imageToBase64(FileInputStream(afd.parcelFileDescriptor.fileDescriptor))
                }
            } catch (e: IOException) {
                e.printStackTrace()
                base64 = null
            }
        }
        return RetrofitTiebaApi.WEB_TIEBA_API.webUploadPic(base64)
    }

    override fun webReply(
        forumId: String,
        forumName: String,
        threadId: String,
        tbs: String,
        content: String,
        imgInfo: String?,
        nickName: String,
        pn: String,
        bsk: String
    ): Call<WebReplyResultBean> =
        RetrofitTiebaApi.WEB_TIEBA_API.webReply(
            content = content,
            imgInfo = imgInfo ?: "",
            forumId = forumId,
            forumName = forumName,
            tbs = tbs,
            threadId = threadId,
            nickName = nickName,
            bsk = bsk,
            referer = "https://tieba.baidu.com/p/$threadId?lp=5028&mo_device=1&is_jingpost=0&pn=$pn&"
        )

    override fun webReply(
        forumId: String,
        forumName: String,
        threadId: String,
        tbs: String,
        content: String,
        imgInfo: String?,
        nickName: String,
        postId: String,
        floor: String,
        pn: String,
        bsk: String
    ): Call<WebReplyResultBean> =
        RetrofitTiebaApi.WEB_TIEBA_API.webReply(
            content = content,
            imgInfo = imgInfo ?: "",
            forumId = forumId,
            forumName = forumName,
            tbs = tbs,
            threadId = threadId,
            nickName = nickName,
            postId = postId,
            floor = floor,
            bsk = bsk,
            referer = "https://tieba.baidu.com/p/$threadId?lp=5028&mo_device=1&is_jingpost=0&pn=$pn&"
        )

    override fun webReply(
        forumId: String,
        forumName: String,
        threadId: String,
        tbs: String,
        content: String,
        imgInfo: String?,
        nickName: String,
        postId: String,
        replyPostId: String,
        floor: String,
        pn: String,
        bsk: String
    ): Call<WebReplyResultBean> =
        RetrofitTiebaApi.WEB_TIEBA_API.webReply(
            content = content,
            imgInfo = imgInfo ?: "",
            forumId = forumId,
            forumName = forumName,
            tbs = tbs,
            threadId = threadId,
            nickName = nickName,
            postId = postId,
            replyPostId = replyPostId,
            floor = floor,
            bsk = bsk,
            referer = "https://tieba.baidu.com/p/$threadId?lp=5028&mo_device=1&is_jingpost=0&pn=$pn&"
        )

    override fun webReplyAsync(
        forumId: String,
        forumName: String,
        threadId: String,
        tbs: String,
        content: String,
        imgInfo: String?,
        nickName: String,
        pn: String,
        bsk: String
    ): Deferred<ApiResult<WebReplyResultBean>> =
        RetrofitTiebaApi.WEB_TIEBA_API.webReplyAsync(
            content = content,
            imgInfo = imgInfo ?: "",
            forumId = forumId,
            forumName = forumName,
            tbs = tbs,
            threadId = threadId,
            nickName = nickName,
            bsk = bsk,
            referer = "https://tieba.baidu.com/p/$threadId?lp=5028&mo_device=1&is_jingpost=0&pn=$pn&"
        )

    override fun webReplyAsync(
        forumId: String,
        forumName: String,
        threadId: String,
        tbs: String,
        content: String,
        imgInfo: String?,
        nickName: String,
        postId: String,
        floor: String,
        pn: String,
        bsk: String
    ): Deferred<ApiResult<WebReplyResultBean>> =
        RetrofitTiebaApi.WEB_TIEBA_API.webReplyAsync(
            content = content,
            imgInfo = imgInfo ?: "",
            forumId = forumId,
            forumName = forumName,
            tbs = tbs,
            threadId = threadId,
            nickName = nickName,
            postId = postId,
            floor = floor,
            bsk = bsk,
            referer = "https://tieba.baidu.com/p/$threadId?lp=5028&mo_device=1&is_jingpost=0&pn=$pn&"
        )

    override fun webReplyAsync(
        forumId: String,
        forumName: String,
        threadId: String,
        tbs: String,
        content: String,
        imgInfo: String?,
        nickName: String,
        postId: String,
        replyPostId: String,
        floor: String,
        pn: String,
        bsk: String
    ): Deferred<ApiResult<WebReplyResultBean>> =
        RetrofitTiebaApi.WEB_TIEBA_API.webReplyAsync(
            content = content,
            imgInfo = imgInfo ?: "",
            forumId = forumId,
            forumName = forumName,
            tbs = tbs,
            threadId = threadId,
            nickName = nickName,
            postId = postId,
            replyPostId = replyPostId,
            floor = floor,
            bsk = bsk,
            referer = "https://tieba.baidu.com/p/$threadId?lp=5028&mo_device=1&is_jingpost=0&pn=$pn&"
        )


    override fun webForumPage(
        forumName: String,
        page: Int,
        goodClassifyId: String?,
        sortType: ForumSortType,
        pageSize: Int
    ): Call<ForumBean> =
        RetrofitTiebaApi.WEB_TIEBA_API.frs(
            forumName,
            (page - 1) * pageSize,
            sortType.value,
            goodClassifyId
        )

    override fun webForumPageAsync(
        forumName: String,
        page: Int,
        goodClassifyId: String?,
        sortType: ForumSortType,
        pageSize: Int
    ): Deferred<ApiResult<ForumBean>> =
        RetrofitTiebaApi.WEB_TIEBA_API.frsAsync(
            forumName,
            (page - 1) * pageSize,
            sortType.value,
            goodClassifyId
        )

    override fun checkReportPost(postId: String): Call<CheckReportBean> =
        RetrofitTiebaApi.MINI_TIEBA_API.checkReport(
            category = "1",
            reportParam = mapOf(
                "pid" to postId
            )
        )
}