package com.huanchengfly.tieba.post.api.interfaces.impls

import android.os.Build
import android.text.TextUtils
import com.huanchengfly.tieba.post.App
import com.huanchengfly.tieba.post.api.ClientVersion
import com.huanchengfly.tieba.post.api.ForumSortType
import com.huanchengfly.tieba.post.api.Param
import com.huanchengfly.tieba.post.api.SearchThreadFilter
import com.huanchengfly.tieba.post.api.SearchThreadOrder
import com.huanchengfly.tieba.post.api.booleanToString
import com.huanchengfly.tieba.post.api.buildAdParam
import com.huanchengfly.tieba.post.api.buildAppPosInfo
import com.huanchengfly.tieba.post.api.buildCommonRequest
import com.huanchengfly.tieba.post.api.buildProtobufRequestBody
import com.huanchengfly.tieba.post.api.getScreenHeight
import com.huanchengfly.tieba.post.api.getScreenWidth
import com.huanchengfly.tieba.post.api.interfaces.ITiebaApi
import com.huanchengfly.tieba.post.api.models.AgreeBean
import com.huanchengfly.tieba.post.api.models.CheckReportBean
import com.huanchengfly.tieba.post.api.models.CollectDataBean
import com.huanchengfly.tieba.post.api.models.CommonResponse
import com.huanchengfly.tieba.post.api.models.FollowBean
import com.huanchengfly.tieba.post.api.models.ForumPageBean
import com.huanchengfly.tieba.post.api.models.ForumRecommend
import com.huanchengfly.tieba.post.api.models.GetForumListBean
import com.huanchengfly.tieba.post.api.models.InitNickNameBean
import com.huanchengfly.tieba.post.api.models.LikeForumResultBean
import com.huanchengfly.tieba.post.api.models.LoginBean
import com.huanchengfly.tieba.post.api.models.MSignBean
import com.huanchengfly.tieba.post.api.models.MessageListBean
import com.huanchengfly.tieba.post.api.models.MsgBean
import com.huanchengfly.tieba.post.api.models.NewCollectDataBean
import com.huanchengfly.tieba.post.api.models.PersonalizedBean
import com.huanchengfly.tieba.post.api.models.PicPageBean
import com.huanchengfly.tieba.post.api.models.Profile
import com.huanchengfly.tieba.post.api.models.ProfileBean
import com.huanchengfly.tieba.post.api.models.SearchForumBean
import com.huanchengfly.tieba.post.api.models.SearchPostBean
import com.huanchengfly.tieba.post.api.models.SearchThreadBean
import com.huanchengfly.tieba.post.api.models.SearchUserBean
import com.huanchengfly.tieba.post.api.models.SignResultBean
import com.huanchengfly.tieba.post.api.models.SubFloorListBean
import com.huanchengfly.tieba.post.api.models.Sync
import com.huanchengfly.tieba.post.api.models.ThreadContentBean
import com.huanchengfly.tieba.post.api.models.ThreadStoreBean
import com.huanchengfly.tieba.post.api.models.UserLikeForumBean
import com.huanchengfly.tieba.post.api.models.UserPostBean
import com.huanchengfly.tieba.post.api.models.WebReplyResultBean
import com.huanchengfly.tieba.post.api.models.WebUploadPicBean
import com.huanchengfly.tieba.post.api.models.protos.addPost.AddPostRequest
import com.huanchengfly.tieba.post.api.models.protos.addPost.AddPostRequestData
import com.huanchengfly.tieba.post.api.models.protos.addPost.AddPostResponse
import com.huanchengfly.tieba.post.api.models.protos.forumRecommend.ForumRecommendRequest
import com.huanchengfly.tieba.post.api.models.protos.forumRecommend.ForumRecommendRequestData
import com.huanchengfly.tieba.post.api.models.protos.forumRecommend.ForumRecommendResponse
import com.huanchengfly.tieba.post.api.models.protos.frsPage.FrsPageRequest
import com.huanchengfly.tieba.post.api.models.protos.frsPage.FrsPageRequestData
import com.huanchengfly.tieba.post.api.models.protos.frsPage.FrsPageResponse
import com.huanchengfly.tieba.post.api.models.protos.hotThreadList.HotThreadListRequest
import com.huanchengfly.tieba.post.api.models.protos.hotThreadList.HotThreadListRequestData
import com.huanchengfly.tieba.post.api.models.protos.hotThreadList.HotThreadListResponse
import com.huanchengfly.tieba.post.api.models.protos.pbFloor.PbFloorRequest
import com.huanchengfly.tieba.post.api.models.protos.pbFloor.PbFloorRequestData
import com.huanchengfly.tieba.post.api.models.protos.pbFloor.PbFloorResponse
import com.huanchengfly.tieba.post.api.models.protos.pbPage.PbPageRequest
import com.huanchengfly.tieba.post.api.models.protos.pbPage.PbPageRequestData
import com.huanchengfly.tieba.post.api.models.protos.pbPage.PbPageResponse
import com.huanchengfly.tieba.post.api.models.protos.personalized.PersonalizedRequest
import com.huanchengfly.tieba.post.api.models.protos.personalized.PersonalizedRequestData
import com.huanchengfly.tieba.post.api.models.protos.personalized.PersonalizedResponse
import com.huanchengfly.tieba.post.api.models.protos.profile.ProfileRequest
import com.huanchengfly.tieba.post.api.models.protos.profile.ProfileRequestData
import com.huanchengfly.tieba.post.api.models.protos.profile.ProfileResponse
import com.huanchengfly.tieba.post.api.models.protos.threadList.AdParam
import com.huanchengfly.tieba.post.api.models.protos.threadList.ThreadListRequest
import com.huanchengfly.tieba.post.api.models.protos.threadList.ThreadListRequestData
import com.huanchengfly.tieba.post.api.models.protos.threadList.ThreadListResponse
import com.huanchengfly.tieba.post.api.models.protos.topicList.TopicListRequest
import com.huanchengfly.tieba.post.api.models.protos.topicList.TopicListRequestData
import com.huanchengfly.tieba.post.api.models.protos.topicList.TopicListResponse
import com.huanchengfly.tieba.post.api.models.protos.userLike.UserLikeRequest
import com.huanchengfly.tieba.post.api.models.protos.userLike.UserLikeRequestData
import com.huanchengfly.tieba.post.api.models.protos.userLike.UserLikeResponse
import com.huanchengfly.tieba.post.api.models.web.ForumBean
import com.huanchengfly.tieba.post.api.models.web.ForumHome
import com.huanchengfly.tieba.post.api.models.web.HotMessageListBean
import com.huanchengfly.tieba.post.api.retrofit.ApiResult
import com.huanchengfly.tieba.post.api.retrofit.RetrofitTiebaApi
import com.huanchengfly.tieba.post.api.retrofit.body.MyMultipartBody
import com.huanchengfly.tieba.post.api.urlEncode
import com.huanchengfly.tieba.post.models.DislikeBean
import com.huanchengfly.tieba.post.models.MyInfoBean
import com.huanchengfly.tieba.post.models.PhotoInfoBean
import com.huanchengfly.tieba.post.toJson
import com.huanchengfly.tieba.post.utils.AccountUtil
import com.huanchengfly.tieba.post.utils.ImageUtil
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.flow.Flow
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.net.URLEncoder

object MixedTiebaApiImpl : ITiebaApi {
    override fun personalized(loadType: Int, page: Int): Call<PersonalizedBean> =
        RetrofitTiebaApi.MINI_TIEBA_API.personalized(loadType, page)

    override fun personalizedAsync(
        loadType: Int,
        page: Int
    ): Deferred<ApiResult<PersonalizedBean>> =
        RetrofitTiebaApi.MINI_TIEBA_API.personalizedAsync(loadType, page)

    override fun personalizedFlow(loadType: Int, page: Int): Flow<PersonalizedBean> {
        return RetrofitTiebaApi.OFFICIAL_TIEBA_API.personalizedFlow(loadType, page)
    }

    override fun personalizedProtoFlow(loadType: Int, page: Int): Flow<PersonalizedResponse> {
        return RetrofitTiebaApi.OFFICIAL_PROTOBUF_TIEBA_V12_API.personalizedFlow(
            buildProtobufRequestBody(
                data = PersonalizedRequest(
                    PersonalizedRequestData(
                        app_pos = buildAppPosInfo(),
                        common = buildCommonRequest(clientVersion = ClientVersion.TIEBA_V12),
                        load_type = loadType,
                        pn = page,
                        need_tags = 0,
                        page_thread_count = 11,
                        pre_ad_thread_count = 0,
                        sug_count = 0,
                        tag_code = 0,
                        q_type = 1,
                        need_forumlist = 0,
                        new_net_type = 1,
                        new_install = 0,
                        request_times = 0,
                        invoke_source = "",
                        scr_dip = App.ScreenInfo.DENSITY.toDouble(),
                        scr_h = getScreenHeight(),
                        scr_w = getScreenWidth()
                    )
                ),
                clientVersion = ClientVersion.TIEBA_V12
            )
        )
    }

    override fun myProfileAsync(): Deferred<ApiResult<com.huanchengfly.tieba.post.api.models.web.Profile>> =
        RetrofitTiebaApi.WEB_TIEBA_API.myProfileAsync("json", "", "")

    override fun opAgree(
        threadId: String,
        postId: String,
        opType: Int
    ): Call<AgreeBean> =
        RetrofitTiebaApi.MINI_TIEBA_API.agree(postId, threadId, op_type = opType)

    override fun disagree(
        threadId: String,
        postId: String,
        opType: Int
    ): Call<AgreeBean> = RetrofitTiebaApi.MINI_TIEBA_API.disagree(postId, threadId, op_type = opType)

    override fun opAgreeFlow(
        threadId: String,
        postId: String,
        opType: Int,
        objType: Int,
        agreeType: Int,
    ): Flow<AgreeBean> =
        RetrofitTiebaApi.MINI_TIEBA_API.opAgreeFlow(
            threadId,
            postId,
            opType = opType,
            objType = objType,
            agreeType = agreeType
        )

    override fun disagreeFlow(
        threadId: String,
        postId: String,
        opType: Int
    ): Flow<AgreeBean> = RetrofitTiebaApi.MINI_TIEBA_API.disagreeFlow(postId, threadId, op_type = opType)

    override fun forumRecommend(): Call<ForumRecommend> =
        RetrofitTiebaApi.MINI_TIEBA_API.forumRecommend()

    override fun forumRecommendAsync(): Deferred<ApiResult<ForumRecommend>> =
        RetrofitTiebaApi.MINI_TIEBA_API.forumRecommendAsync()

    override fun forumRecommendFlow(): Flow<ForumRecommend> =
        RetrofitTiebaApi.MINI_TIEBA_API.forumRecommendFlow()

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

    override fun forumHomeAsync(sortType: Int, page: Int): Deferred<ApiResult<ForumHome>> {
        return RetrofitTiebaApi.WEB_TIEBA_API.getForumHomeAsync(
            sortType,
            page,
            20,
            "",
            ""
        )
    }

    override fun userLikeForum(
        uid: String, page: Int
    ): Call<UserLikeForumBean> {
        val myUid = AccountUtil.getUid()
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
        prev = if (prev) 10 else 0,
        next = if (prev) 0 else 10,
        not_see_lz = if (seeLz) 0 else 1
    )

    override fun picPageFlow(
        forumId: String,
        forumName: String,
        threadId: String,
        seeLz: Boolean,
        picId: String,
        picIndex: String,
        objType: String,
        prev: Boolean
    ): Flow<PicPageBean> = RetrofitTiebaApi.MINI_TIEBA_API.picPageFlow(
        forumId,
        forumName,
        threadId,
        picId,
        picIndex,
        objType,
        prev = if (prev) 10 else 0,
        next = if (prev) 0 else 10,
        not_see_lz = if (seeLz) 0 else 1
    )

    override fun profile(uid: String): Call<ProfileBean> =
        RetrofitTiebaApi.MINI_TIEBA_API.profile(uid)

    override fun profileFlow(uid: String): Flow<Profile> =
        RetrofitTiebaApi.OFFICIAL_TIEBA_API.profileFlow(uid)

    override fun unlikeForum(
        forumId: String,
        forumName: String,
        tbs: String
    ): Call<CommonResponse> = RetrofitTiebaApi.MINI_TIEBA_API.unlikeForum(forumId, forumName, tbs)

    override fun unlikeForumFlow(
        forumId: String,
        forumName: String,
        tbs: String
    ): Flow<CommonResponse> =
        RetrofitTiebaApi.OFFICIAL_TIEBA_API.unfavolike(forumId, forumName, tbs)

    override fun likeForum(
        forumId: String, forumName: String, tbs: String
    ): Call<LikeForumResultBean> =
        RetrofitTiebaApi.MINI_TIEBA_API.likeForum(forumId, forumName, tbs)

    override fun likeForumFlow(
        forumId: String,
        forumName: String,
        tbs: String
    ): Flow<LikeForumResultBean> =
        RetrofitTiebaApi.MINI_TIEBA_API.likeForumFlow(forumId, forumName, tbs)

    override fun signAsync(forumName: String, tbs: String): Deferred<ApiResult<SignResultBean>> =
        RetrofitTiebaApi.MINI_TIEBA_API.signAsync(forumName, tbs)

    override fun signFlow(forumId: String, forumName: String, tbs: String): Flow<SignResultBean> =
        RetrofitTiebaApi.OFFICIAL_TIEBA_API.signFlow(forumId, forumName, tbs)

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
        keyword: String,
        forumName: String,
        onlyThread: Boolean,
        sortMode: Int,
        page: Int,
        pageSize: Int
    ): Call<SearchPostBean> = RetrofitTiebaApi.MINI_TIEBA_API.searchPost(
        keyword,
        forumName,
        page,
        pageSize,
        only_thread = if (onlyThread) 1 else 0,
        sortMode = sortMode
    )

    override fun searchPostAsync(
        keyword: String,
        forumName: String,
        onlyThread: Boolean,
        sortMode: Int,
        page: Int,
        pageSize: Int
    ): Deferred<ApiResult<SearchPostBean>> = RetrofitTiebaApi.MINI_TIEBA_API.searchPostAsync(
        keyword,
        forumName,
        page,
        pageSize,
        only_thread = if (onlyThread) 1 else 0,
        sortMode = sortMode
    )

    override fun searchUser(keyword: String): Call<SearchUserBean> =
        RetrofitTiebaApi.MINI_TIEBA_API.searchUser(keyword)

    override fun msg(): Call<MsgBean> = RetrofitTiebaApi.NEW_TIEBA_API.msg()

    override fun msgFlow(): Flow<MsgBean> = RetrofitTiebaApi.NEW_TIEBA_API.msgFlow()

    override fun threadStore(page: Int, pageSize: Int): Call<ThreadStoreBean> =
        RetrofitTiebaApi.NEW_TIEBA_API.threadStore(
            pageSize,
            pageSize * page,
            AccountUtil.getUid()
        )

    override fun threadStoreFlow(page: Int, pageSize: Int): Flow<ThreadStoreBean> =
        RetrofitTiebaApi.OFFICIAL_TIEBA_API.threadStoreFlow(
            pageSize,
            pageSize * page
        )

    override fun removeStore(threadId: String, tbs: String): Call<CommonResponse> =
        RetrofitTiebaApi.NEW_TIEBA_API.removeStore(threadId, tbs)

    override fun removeStoreFlow(
        threadId: Long,
        forumId: Long,
        tbs: String?
    ): Flow<CommonResponse> =
        RetrofitTiebaApi.OFFICIAL_TIEBA_API.removeStoreFlow(
            threadId.toString(),
            forumId.toString(),
            tbs ?: AccountUtil.getLoginInfo()!!.tbs
        )

    override fun removeStoreFlow(threadId: String): Flow<CommonResponse> =
        RetrofitTiebaApi.OFFICIAL_TIEBA_API.removeStoreFlow(threadId)

    override fun addStore(threadId: String, postId: String, tbs: String): Call<CommonResponse> =
        RetrofitTiebaApi.NEW_TIEBA_API.addStore(
            listOf(
                CollectDataBean(
                    threadId,
                    postId,
                    "0",
                    "0"
                )
            ).toJson(),
            tbs
        )

    override fun addStoreFlow(threadId: Long, postId: Long): Flow<CommonResponse> =
        RetrofitTiebaApi.OFFICIAL_TIEBA_API.addStoreFlow(
            listOf(
                NewCollectDataBean(
                    threadId.toString(),
                    postId.toString(),
                    status = 1
                )
            ).toJson()
        )


    override fun replyMe(page: Int): Call<MessageListBean> =
        RetrofitTiebaApi.NEW_TIEBA_API.replyMe(page)

    override fun replyMeAsync(page: Int): Deferred<ApiResult<MessageListBean>> =
        RetrofitTiebaApi.NEW_TIEBA_API.replyMeAsync(page)

    override fun replyMeFlow(page: Int): Flow<MessageListBean> =
        RetrofitTiebaApi.NEW_TIEBA_API.replyMeFlow(page)

    override fun atMe(page: Int): Call<MessageListBean> = RetrofitTiebaApi.NEW_TIEBA_API.atMe(page)

    override fun atMeAsync(page: Int): Deferred<ApiResult<MessageListBean>> =
        RetrofitTiebaApi.NEW_TIEBA_API.atMeAsync(page)

    override fun atMeFlow(page: Int): Flow<MessageListBean> = RetrofitTiebaApi.NEW_TIEBA_API.atMeFlow(page)

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

    override fun threadContentAsync(
        threadId: String,
        page: Int,
        seeLz: Boolean,
        reverse: Boolean
    ): Deferred<ApiResult<ThreadContentBean>> =
        RetrofitTiebaApi.OFFICIAL_TIEBA_API.threadContentAsync(
            threadId,
            page,
            last = if (reverse) "1" else null,
            r = if (reverse) "1" else null,
            lz = if (seeLz) 1 else 0
        )

    override fun threadContentAsync(
        threadId: String,
        postId: String?,
        seeLz: Boolean,
        reverse: Boolean
    ): Deferred<ApiResult<ThreadContentBean>> =
        RetrofitTiebaApi.OFFICIAL_TIEBA_API.threadContentAsync(
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
        RetrofitTiebaApi.OFFICIAL_TIEBA_API.submitDislike(listOf(dislikeBean).toJson(), stoken = stoken)

    override fun submitDislikeFlow(dislikeBean: DislikeBean): Flow<CommonResponse> =
        RetrofitTiebaApi.OFFICIAL_TIEBA_API.submitDislikeFlow(listOf(dislikeBean).toJson())

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
        portrait: String,
        tbs: String
    ): Call<CommonResponse> = RetrofitTiebaApi.WEB_TIEBA_API.follow(
        "https://tieba.baidu.com/i/?portrait=${
            URLEncoder.encode(
                portrait,
                "UTF-8"
            )
        }&cuid=&auth=&uid=&ssid=&from=&uid=&pu=&bd_page_type=2&auth=&originid=&mo_device=1&tbs=${tbs}&action=follow&op=unfollow"
    )

    override fun followFlow(
        portrait: String,
        tbs: String
    ): Flow<FollowBean> = RetrofitTiebaApi.OFFICIAL_TIEBA_API.followFlow(portrait, tbs)

    override fun unfollowFlow(
        portrait: String,
        tbs: String
    ): Flow<CommonResponse> = RetrofitTiebaApi.OFFICIAL_TIEBA_API.unfollowFlow(portrait, tbs)

    override fun hotMessageList(): Call<HotMessageListBean> =
        RetrofitTiebaApi.WEB_TIEBA_API.hotMessageList()

    override fun myInfo(cookie: String): Call<MyInfoBean> =
        RetrofitTiebaApi.WEB_TIEBA_API.myInfo(cookie)

    override fun myInfoAsync(cookie: String): Deferred<ApiResult<MyInfoBean>> =
        RetrofitTiebaApi.WEB_TIEBA_API.myInfoAsync(cookie)

    override fun myInfoFlow(cookie: String): Flow<MyInfoBean> =
        RetrofitTiebaApi.WEB_TIEBA_API.myInfoFlow(cookie)

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
                App.INSTANCE.contentResolver.openAssetFileDescriptor(
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
        RetrofitTiebaApi.OFFICIAL_TIEBA_API.checkReport(
            category = "1",
            reportParam = mapOf(
                "pid" to postId
            )
        )

    override fun initNickNameFlow(): Flow<InitNickNameBean> =
        RetrofitTiebaApi.OFFICIAL_TIEBA_API.initNickNameFlow()

    override fun initNickNameFlow(bduss: String, sToken: String): Flow<InitNickNameBean> =
        RetrofitTiebaApi.OFFICIAL_TIEBA_API.initNickNameFlow(bduss, sToken)

    override fun loginFlow(): Flow<LoginBean> =
        RetrofitTiebaApi.OFFICIAL_TIEBA_API.loginFlow()

    override fun loginFlow(bduss: String, sToken: String): Flow<LoginBean> =
        RetrofitTiebaApi.OFFICIAL_TIEBA_API.loginFlow("$bduss|", sToken, null)

    override fun profileModifyFlow(
        birthdayShowStatus: Boolean,
        birthdayTime: String,
        intro: String,
        sex: String
    ): Flow<CommonResponse> =
        RetrofitTiebaApi.OFFICIAL_TIEBA_API.profileModify(
            birthdayShowStatus.booleanToString(),
            birthdayTime,
            intro,
            sex
        )

    override fun imgPortrait(file: File): Flow<CommonResponse> {
        return RetrofitTiebaApi.OFFICIAL_TIEBA_API.imgPortrait(
            MyMultipartBody.Builder("--------7da3d81520810*").apply {
                setType(MyMultipartBody.FORM)
                addFormDataPart(Param.CLIENT_VERSION, "11.10.8.6")
                addFormDataPart("pic", "file", file.asRequestBody())
            }.build()
        )
    }

    override fun getForumListFlow(): Flow<GetForumListBean> =
        RetrofitTiebaApi.OFFICIAL_TIEBA_API.getForumListFlow()

    override fun mSign(
        forumIds: String,
        tbs: String
    ): Flow<MSignBean> =
        RetrofitTiebaApi.OFFICIAL_TIEBA_API.mSignFlow(forumIds, tbs)

    override fun userLikeFlow(
        pageTag: String,
        lastRequestUnix: Long,
        loadType: Int
    ): Flow<UserLikeResponse> {
        return RetrofitTiebaApi.OFFICIAL_PROTOBUF_TIEBA_API.userLikeFlow(
            buildProtobufRequestBody(
                UserLikeRequest(
                    UserLikeRequestData(
                        common = buildCommonRequest(),
                        pageTag = pageTag,
                        lastRequestUnix = lastRequestUnix,
                        followType = 1,
                        loadType = loadType
                    )
                )
            )
        )
    }

    override fun hotThreadListFlow(tabCode: String): Flow<HotThreadListResponse> {
        return RetrofitTiebaApi.OFFICIAL_PROTOBUF_TIEBA_API.hotThreadListFlow(
            buildProtobufRequestBody(
                HotThreadListRequest(
                    HotThreadListRequestData(
                        common = buildCommonRequest(),
                        tabCode = tabCode,
                        tabId = "1"
                    )
                )
            )
        )
    }

    override fun topicListFlow(): Flow<TopicListResponse> {
        return RetrofitTiebaApi.OFFICIAL_PROTOBUF_TIEBA_API.topicListFlow(
            buildProtobufRequestBody(
                TopicListRequest(
                    TopicListRequestData(
                        common = buildCommonRequest(),
                        call_from = "newbang",
                        list_type = "all",
                        need_tab_list = "0",
                        fid = 0L
                    )
                )
            )
        )
    }

    override fun forumRecommendNewFlow(
        sortType: Int
    ): Flow<ForumRecommendResponse> {
        return RetrofitTiebaApi.OFFICIAL_PROTOBUF_TIEBA_API.forumRecommendFlow(
            buildProtobufRequestBody(
                ForumRecommendRequest(
                    ForumRecommendRequestData(
                        common = buildCommonRequest(),
                        like_forum = 1,
                        recommend = 1,
                        sort_type = sortType,
                        topic = 0
                    )
                )
            )
        )
    }

    override fun frsPage(
        forumName: String,
        page: Int,
        loadType: Int,
        sortType: Int,
        goodClassifyId: Int?
    ): Flow<FrsPageResponse> {
        return RetrofitTiebaApi.OFFICIAL_PROTOBUF_TIEBA_V12_API.frsPageFlow(
            buildProtobufRequestBody(
                FrsPageRequest(
                    FrsPageRequestData(
                        ad_param = buildAdParam(),
                        app_pos = buildAppPosInfo(),
                        call_from = 0,
                        category_id = 0,
                        cid = goodClassifyId ?: 0,
                        common = buildCommonRequest(clientVersion = ClientVersion.TIEBA_V12),
                        ctime = 0,
                        data_size = 0,
                        hot_thread_id = 0,
                        is_default_navtab = 0,
                        is_good = if (goodClassifyId != null) 1 else 0,
                        is_selection = 0,
                        kw = forumName,
                        last_click_tid = 0,
                        load_type = loadType,
                        net_error = 0,
                        pn = page,
                        q_type = 2,
                        rn = 90,
                        rn_need = 30,
                        scr_dip = App.ScreenInfo.DENSITY.toDouble(),
                        scr_h = getScreenHeight(),
                        scr_w = getScreenWidth(),
                        sort_type = sortType,
                        st_param = 0,
                        st_type = "recom_flist",
                        up_schema = "",
                        with_group = 1,
                        yuelaou_locate = ""
                    )
                ),
                clientVersion = ClientVersion.TIEBA_V12
            ),
            forumName = forumName.urlEncode()
        )
    }

    override fun threadList(
        forumId: Long,
        forumName: String,
        page: Int,
        sortType: Int,
        threadIds: String
    ): Flow<ThreadListResponse> {
        return RetrofitTiebaApi.OFFICIAL_PROTOBUF_TIEBA_API.threadListFlow(
            buildProtobufRequestBody(
                ThreadListRequest(
                    ThreadListRequestData(
                        ad_param = AdParam(3, 0, null),
                        app_pos = buildAppPosInfo(),
                        common = buildCommonRequest(),
                        scr_dip = App.ScreenInfo.DENSITY.toDouble(),
                        scr_h = getScreenHeight(),
                        scr_w = getScreenWidth(),
                        forum_id = forumId,
                        forum_name = forumName,
                        pn = page,
                        q_type = 2,
                        user_id = AccountUtil.getUid()?.toLongOrNull(),
                        thread_ids = threadIds,
                        sort_type = sortType,
                        need_abstract = 0,
                        st_type = 0,
                        last_click_tid = 0
                    )
                )
            )
        )
    }

    override fun syncFlow(clientId: String?): Flow<Sync> =
        RetrofitTiebaApi.OFFICIAL_TIEBA_API.sync(clientId)

    override fun addPostFlow(
        content: String,
        forumId: String,
        forumName: String,
        threadId: String,
        tbs: String?,
        nameShow: String?,
        postId: String?,
        subPostId: String?,
        replyUserId: String?
    ): Flow<AddPostResponse> {
        return RetrofitTiebaApi.OFFICIAL_PROTOBUF_TIEBA_POST_API
            .addPostFlow(
                buildProtobufRequestBody(
                    AddPostRequest(
                        AddPostRequestData(
                            anonymous = "1",
                            barrage_time = "0".takeIf { postId.isNullOrEmpty() },
                            can_no_forum = "0",
                            common = buildCommonRequest(
                                clientVersion = ClientVersion.TIEBA_V12_POST,
                                tbs = tbs ?: AccountUtil.getAccountInfo { this.tbs }
                            ),
                            content = content,
                            entrance_type = "0",
                            fid = forumId,
                            floor_num = "0".takeIf { postId.isNullOrEmpty() },
                            is_ad = "0",
                            is_addition = "0".takeIf { postId.isNullOrEmpty() },
                            is_barrage = "0".takeIf { postId.isNullOrEmpty() },
                            is_feedback = "0",
                            is_giftpost = "0".takeIf { postId.isNullOrEmpty() },
                            is_pictxt = "0",
                            is_show_bless = 0,
                            is_twzhibo_thread = "0".takeIf { postId.isNullOrEmpty() },
                            name_show = nameShow ?: AccountUtil.getAccountInfo { this.nameShow }
                                .orEmpty(),
                            new_vcode = "1",
                            post_from = if (postId.isNullOrEmpty() && subPostId.isNullOrEmpty()) "13" else if (subPostId.isNullOrEmpty()) "0" else null,
                            quote_id = postId,
                            reply_uid = replyUserId.takeIf { !postId.isNullOrEmpty() },
                            repostid = postId,
                            sub_post_id = subPostId,
                            show_custom_figure = 0,
                            takephoto_num = "0",
                            tid = threadId,
                            v_fid = "".takeIf { postId.isNullOrEmpty() },
                            v_fname = "".takeIf { postId.isNullOrEmpty() },
                            vcode_tag = "12",
                        )
                    ),
                    clientVersion = ClientVersion.TIEBA_V12_POST
                )
            )
    }

    override fun userProfileFlow(uid: Long): Flow<ProfileResponse> {
        val selfUid = AccountUtil.getUid()?.toLongOrNull()
        val isSelf = selfUid == uid
        return RetrofitTiebaApi.OFFICIAL_PROTOBUF_TIEBA_API.profileFlow(
            buildProtobufRequestBody(
                ProfileRequest(
                    ProfileRequestData(
                        common = buildCommonRequest(),
                        friend_uid = uid.takeIf { !isSelf },
                        friend_uid_portrait = "",
                        has_plist = 1,
                        is_from_usercenter = 1,
                        is_guest = if (isSelf) 0 else 1,
                        need_post_count = 1,
                        page = 2,
                        pn = 1,
                        q_type = 0,
                        rn = 20,
                        scr_dip = App.ScreenInfo.DENSITY.toDouble(),
                        scr_h = getScreenHeight(),
                        scr_w = getScreenWidth(),
                        uid = selfUid,
                    )
                )
            )
        )
    }

    override fun pbPageFlow(
        threadId: Long,
        page: Int,
        postId: Long,
        seeLz: Boolean,
        back: Boolean,
        sortType: Int,
        forumId: Long?,
        stType: String,
        mark: Int,
    ): Flow<PbPageResponse> {
        return RetrofitTiebaApi.OFFICIAL_PROTOBUF_TIEBA_V12_API.pbPageFlow(
            buildProtobufRequestBody(
                PbPageRequest(
                    PbPageRequestData(
                        common = buildCommonRequest(clientVersion = ClientVersion.TIEBA_V12),
                        kz = threadId,
                        pid = postId,
                        pn = page,
                        r = sortType,
                        lz = if (seeLz) 1 else 0,
                        forum_id = forumId ?: 0,
                        ad_param = com.huanchengfly.tieba.post.api.models.protos.pbPage.AdParam(
                            load_count = 0,
                            refresh_count = 1,
                            is_req_ad = 1
                        ),
                        mark = mark,
                        app_pos = buildAppPosInfo(),
                        back = if (back) 1 else 0,
                        banner = 1,
                        broadcast_id = 0,
                        floor_rn = 4,
                        floor_sort_type = 1,
                        from_push = 0,
                        from_smart_frs = 0,
                        immersion_video_comment_source = 0,
                        is_comm_reverse = 0,
                        is_fold_comment_req = 0,
                        is_jumpfloor = 0,
                        jumpfloor_num = 0,
                        need_repost_recommend_forum = 0,
                        obj_locate = "",
                        obj_param1 = "2",
                        obj_source = "",
                        ori_ugc_type = 0,
                        pb_rn = 0,
                        q_type = 1,
                        rn = 15,
                        s_model = 0,
                        scr_dip = App.ScreenInfo.DENSITY.toDouble(),
                        scr_h = getScreenHeight(),
                        scr_w = getScreenWidth(),
                        source_type = 2,
                        st_type = stType,
                        thread_type = 0,
                        weipost = 0,
                        with_floor = 1
                    )
                ),
                clientVersion = ClientVersion.TIEBA_V12
            )
        )
    }

    override fun pbFloorFlow(
        threadId: Long,
        postId: Long,
        forumId: Long,
        page: Int,
        subPostId: Long
    ): Flow<PbFloorResponse> {
        return RetrofitTiebaApi.OFFICIAL_PROTOBUF_TIEBA_V12_API.pbFloorFlow(
            buildProtobufRequestBody(
                PbFloorRequest(
                    PbFloorRequestData(
                        common = buildCommonRequest(clientVersion = ClientVersion.TIEBA_V12),
                        forum_id = forumId,
                        kz = threadId,
                        pid = postId,
                        pn = page,
                        spid = subPostId,
                        scr_dip = App.ScreenInfo.DENSITY.toDouble(),
                        scr_h = getScreenHeight(),
                        scr_w = getScreenWidth(),
                        is_comm_reverse = 0,
                        ori_ugc_type = 0
                    )
                ),
                clientVersion = ClientVersion.TIEBA_V12,
                needSToken = false
            )
        )
    }
}