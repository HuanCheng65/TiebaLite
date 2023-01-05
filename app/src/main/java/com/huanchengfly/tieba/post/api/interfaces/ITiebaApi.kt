package com.huanchengfly.tieba.post.api.interfaces

import com.huanchengfly.tieba.post.api.ForumSortType
import com.huanchengfly.tieba.post.api.SearchThreadFilter
import com.huanchengfly.tieba.post.api.SearchThreadOrder
import com.huanchengfly.tieba.post.api.models.*
import com.huanchengfly.tieba.post.api.models.protos.frsPage.FrsPageResponse
import com.huanchengfly.tieba.post.api.models.protos.hotThreadList.HotThreadListResponse
import com.huanchengfly.tieba.post.api.models.protos.personalized.PersonalizedResponse
import com.huanchengfly.tieba.post.api.models.protos.userLike.UserLikeResponse
import com.huanchengfly.tieba.post.api.models.web.ForumBean
import com.huanchengfly.tieba.post.api.models.web.ForumHome
import com.huanchengfly.tieba.post.api.models.web.HotMessageListBean
import com.huanchengfly.tieba.post.api.retrofit.ApiResult
import com.huanchengfly.tieba.post.models.DislikeBean
import com.huanchengfly.tieba.post.models.MyInfoBean
import com.huanchengfly.tieba.post.models.PhotoInfoBean
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.flow.Flow
import retrofit2.Call
import java.io.File

interface ITiebaApi {
    /**
     * 个性推荐（每页 15 贴）
     *
     * @param loadType 加载类型（1 - 下拉刷新 2 - 加载更多）
     * @param page 分页页码
     */
    fun personalized(
        loadType: Int,
        page: Int = 1
    ): Call<PersonalizedBean>

    /**
     * 个性推荐（每页 15 贴）
     *
     * @param loadType 加载类型（1 - 下拉刷新 2 - 加载更多）
     * @param page 分页页码
     */
    fun personalizedAsync(
        loadType: Int,
        page: Int = 1
    ): Deferred<ApiResult<PersonalizedBean>>

    /**
     * 个性推荐（每页 15 贴）
     *
     * @param loadType 加载类型（1 - 下拉刷新 2 - 加载更多）
     * @param page 分页页码
     */
    fun personalizedFlow(
        loadType: Int,
        page: Int = 1
    ): Flow<PersonalizedBean>

    /**
     * 个性推荐（每页 15 贴）
     *
     * @param loadType 加载类型（1 - 下拉刷新 2 - 加载更多）
     * @param page 分页页码
     */
    fun personalizedProtoFlow(
        loadType: Int,
        page: Int = 1
    ): Flow<PersonalizedResponse>

    /**
     * 给贴子/回复点赞
     *
     * **需登录**
     *
     * @param threadId 贴子 ID
     * @param postId 回复 ID
     * @param opType 操作 0 = 点赞 1 = 取消点赞
     */
    fun opAgree(
        threadId: String,
        postId: String,
        opType: Int
    ): Call<AgreeBean>

    /**
     * 给贴子/回复点赞/取消点赞
     *
     * **需登录**
     *
     * @param threadId 贴子 ID
     * @param postId 回复 ID
     * @param opType 操作 0 = 点赞 1 = 取消点赞
     */
    fun opAgreeFlow(
        threadId: String,
        postId: String,
        opType: Int
    ): Flow<AgreeBean>

    /**
     * 给贴子/回复点踩
     *
     * **需登录**
     *
     * @param threadId 贴子 ID
     * @param postId 回复 ID
     * @param opType 操作 0 = 点踩 1 = 取消点踩
     */
    fun disagree(
        threadId: String,
        postId: String,
        opType: Int
    ): Call<AgreeBean>

    /**
     * 给贴子/回复点踩
     *
     * **需登录**
     *
     * @param threadId 贴子 ID
     * @param postId 回复 ID
     * @param opType 操作 0 = 点踩 1 = 取消点踩
     */
    fun disagreeFlow(
        threadId: String,
        postId: String,
        opType: Int
    ): Flow<AgreeBean>

    /**
     * 关注吧列表
     *
     * **需登录**
     */
    fun forumRecommend(): Call<ForumRecommend>

    /**
     * 关注吧列表
     *
     * **需登录**
     */
    fun forumRecommendAsync(): Deferred<ApiResult<ForumRecommend>>

    /**
     * 关注吧列表
     *
     * **需登录**
     */
    fun forumRecommendFlow(): Flow<ForumRecommend>

    /**
     * 吧页面
     *
     * @param forumName 吧名
     * @param page 分页页码（从 1 开始）
     * @param sortType 排序类型 [com.huanchengfly.tieba.api.ForumSortType]
     * @param goodClassifyId 精品贴分类 ID
     */
    fun forumPage(
        forumName: String,
        page: Int = 1,
        sortType: ForumSortType = ForumSortType.REPLY_TIME,
        goodClassifyId: String? = null
    ): Call<ForumPageBean>

    /**
     * 吧页面（异步）
     *
     * @param forumName 吧名
     * @param page 分页页码（从 1 开始）
     * @param sortType 排序类型 [com.huanchengfly.tieba.api.ForumSortType]
     * @param goodClassifyId 精品贴分类 ID
     */
    fun forumPageAsync(
        forumName: String,
        page: Int = 1,
        sortType: ForumSortType = ForumSortType.REPLY_TIME,
        goodClassifyId: String? = null
    ): Deferred<ApiResult<ForumPageBean>>

    /**
     * 楼中楼页面
     *
     * @param threadId 贴 ID
     * @param page 分页页码
     * @param postId 回复 ID
     * @param subPostId 楼中楼回复 ID
     */
    fun floor(
        threadId: String,
        page: Int = 1,
        postId: String?,
        subPostId: String?
    ): Call<SubFloorListBean>

    /**
     * 获取首页关注吧列表（网页版接口）
     *
     * @param sortType 排序方式（0 = 等级排序，1 = 关注顺序）
     * @param page 分页页码（从 0 开始）
     */
    fun forumHomeAsync(
        sortType: Int,
        page: Int = 0
    ): Deferred<ApiResult<ForumHome>>

    /**
     * 查看用户关注的吧列表
     *
     * @param uid 用户 ID
     * @param page 分页页码（从 1 开始）
     */
    fun userLikeForum(
        uid: String,
        page: Int = 1
    ): Call<UserLikeForumBean>

    /**
     * 查看用户的所有主题贴/回复
     *
     * @param uid 用户 ID
     * @param page 分页页码（从 1 开始）
     * @param isThread 是否查看主题贴
     */
    fun userPost(
        uid: String,
        page: Int = 1,
        isThread: Boolean = true
    ): Call<UserPostBean>

    /**
     * 查看图片
     *
     * @param forumId 吧 ID
     * @param forumName 吧名
     * @param threadId 贴 ID
     * @param seeLz 是否只看楼主
     * @param picId 图片 ID
     * @param picIndex 图片索引
     * @param objType 原页面类型（pb - 贴页面 frs - 吧页面 index - 首页推荐）
     * @param prev 是否向前加载
     */
    fun picPage(
        forumId: String,
        forumName: String,
        threadId: String,
        seeLz: Boolean,
        picId: String,
        picIndex: String,
        objType: String,
        prev: Boolean
    ): Call<PicPageBean>

    /**
     * 查看图片
     *
     * @param forumId 吧 ID
     * @param forumName 吧名
     * @param threadId 贴 ID
     * @param seeLz 是否只看楼主
     * @param picId 图片 ID
     * @param picIndex 图片索引
     * @param objType 原页面类型（pb - 贴页面 frs - 吧页面 index - 首页推荐）
     * @param prev 是否向前加载
     */
    fun picPageFlow(
        forumId: String,
        forumName: String,
        threadId: String,
        seeLz: Boolean,
        picId: String,
        picIndex: String,
        objType: String,
        prev: Boolean
    ): Flow<PicPageBean>

    /**
     * 用户信息
     *
     * @param uid 用户 ID
     */
    fun profile(
        uid: String
    ): Call<ProfileBean>

    /**
     * 用户信息（Flow）
     *
     * @param uid 用户 ID
     */
    fun profileFlow(
        uid: String
    ): Flow<Profile>

    /**
     * 用户信息（异步）
     */
    fun myProfileAsync(): Deferred<ApiResult<com.huanchengfly.tieba.post.api.models.web.Profile>>

    /**
     * 取关一个吧
     *
     * **需登录**
     *
     * @param forumId 吧 ID
     * @param forumName 吧名
     * @param tbs tbs（长）
     */
    fun unlikeForum(
        forumId: String,
        forumName: String,
        tbs: String
    ): Call<CommonResponse>

    /**
     * 取关一个吧
     *
     * **需登录**
     *
     * @param forumId 吧 ID
     * @param forumName 吧名
     * @param tbs tbs（长）
     */
    fun unlikeForumFlow(
        forumId: String,
        forumName: String,
        tbs: String
    ): Flow<CommonResponse>

    /**
     * 关注一个吧
     *
     * **需登录**
     *
     * @param forumId 吧 ID
     * @param forumName 吧名
     * @param tbs tbs（长）
     */
    fun likeForum(
        forumId: String,
        forumName: String,
        tbs: String
    ): Call<LikeForumResultBean>

    /**
     * 吧签到
     *
     * **需登录**
     *
     * @param forumName 吧名
     * @param tbs tbs
     */
    fun signAsync(
        forumName: String,
        tbs: String
    ): Deferred<ApiResult<SignResultBean>>

    /**
     * 删除自己的贴子
     *
     * **需登录**
     *
     * @param forumId 吧 ID
     * @param forumName 吧名
     * @param threadId 贴子 ID
     */
    fun delThread(
        forumId: String,
        forumName: String,
        threadId: String,
        tbs: String
    ): Call<CommonResponse>

    /**
     * 删除贴子中的回复
     *
     * **需登录**
     *
     * @param forumId 吧 ID
     * @param forumName 吧名
     * @param threadId 贴子 ID
     * @param postId 回复 ID
     * @param isFloor 是否为楼中楼回复
     * @param delMyPost 是否为当前登录用户回复
     */
    fun delPost(
        forumId: String,
        forumName: String,
        threadId: String,
        postId: String,
        tbs: String,
        isFloor: Boolean,
        delMyPost: Boolean
    ): Call<CommonResponse>

    /**
     * 吧内搜索
     *
     * @param keyword 搜索关键词
     * @param forumName 搜索吧名
     * @param onlyThread 是否仅搜索主题贴
     * @param sortMode 排序模式（1 = 时间倒序，2 = 相关性排序）
     * @param page 分页页码（从 1 开始）
     * @param pageSize 每页贴数（默认 30）
     */
    fun searchPost(
        keyword: String,
        forumName: String,
        onlyThread: Boolean = false,
        sortMode: Int = 1,
        page: Int = 1,
        pageSize: Int = 30
    ): Call<SearchPostBean>

    /**
     * 吧内搜索（异步）
     *
     * @param keyword 搜索关键词
     * @param forumName 搜索吧名
     * @param onlyThread 是否仅搜索主题贴
     * @param sortMode 排序模式（1 = 时间倒序，2 = 相关性排序）
     * @param page 分页页码（从 1 开始）
     * @param pageSize 每页贴数（默认 30）
     */
    fun searchPostAsync(
        keyword: String,
        forumName: String,
        onlyThread: Boolean = false,
        sortMode: Int = 1,
        page: Int = 1,
        pageSize: Int = 30
    ): Deferred<ApiResult<SearchPostBean>>

    /**
     * 搜索用户
     *
     * @param keyword 搜索关键词
     */
    fun searchUser(
        keyword: String
    ): Call<SearchUserBean>


    /**
     * 消息提醒数
     *
     * **需登录**
     */
    fun msg(): Call<MsgBean>

    /**
     * 消息提醒数
     *
     * **需登录**
     */
    fun msgFlow(): Flow<MsgBean>

    /**
     * 查看收藏贴列表
     *
     * **需登录**
     *
     * @param page 分页页码（从 0 开始）
     * @param pageSize 每页贴数（默认 20）
     */
    fun threadStore(
        page: Int = 0,
        pageSize: Int = 20
    ): Call<ThreadStoreBean>

    /**
     * 移除收藏
     *
     * **需登录**
     *
     * @param threadId 贴子 ID
     * @param tbs tbs
     */
    fun removeStore(
        threadId: String,
        tbs: String
    ): Call<CommonResponse>

    /**
     * 添加/更新收藏
     *
     * **需登录**
     *
     * @param threadId 贴子 ID
     * @param postId 收藏到的回复 ID
     * @param tbs tbs
     */
    fun addStore(
        threadId: String,
        postId: String,
        tbs: String
    ): Call<CommonResponse>

    /**
     * 回复我的消息列表
     *
     * **需登录**
     *
     * @param page 分页页码（从 1 开始）
     */
    fun replyMe(
        page: Int = 1
    ): Call<MessageListBean>

    /**
     * 回复我的消息列表（异步）
     *
     * **需登录**
     *
     * @param page 分页页码（从 1 开始）
     */
    fun replyMeAsync(
        page: Int = 1
    ): Deferred<ApiResult<MessageListBean>>

    /**
     * 回复我的消息列表
     *
     * **需登录**
     *
     * @param page 分页页码（从 1 开始）
     * @return Flow
     */
    fun replyMeFlow(
        page: Int = 1
    ): Flow<MessageListBean>

    /**
     * 提到我的消息列表
     *
     * **需登录**
     *
     * @param page 分页页码（从 1 开始）
     */
    fun atMe(
        page: Int = 1
    ): Call<MessageListBean>

    /**
     * 提到我的消息列表（异步）
     *
     * **需登录**
     *
     * @param page 分页页码（从 1 开始）
     */
    fun atMeAsync(
        page: Int = 1
    ): Deferred<ApiResult<MessageListBean>>

    /**
     * 提到我的消息列表
     *
     * **需登录**
     *
     * @param page 分页页码（从 1 开始）
     * @return Flow
     */
    fun atMeFlow(
        page: Int = 1
    ): Flow<MessageListBean>

    /**
     * 赞我的消息列表
     *
     * **需登录**
     *
     * @param page 分页页码（从 1 开始）
     */
    fun agreeMe(
        page: Int = 1
    ): Call<MessageListBean>

    /**
     * 贴页面
     *
     * @param threadId 贴 ID
     * @param page 分页页码（从 1 开始）
     * @param seeLz 是否只看楼主
     * @param reverse 是否逆序
     */
    fun threadContent(
        threadId: String,
        page: Int = 1,
        seeLz: Boolean = false,
        reverse: Boolean = false
    ): Call<ThreadContentBean>

    /**
     * 贴页面
     *
     * @param threadId 贴 ID
     * @param postId 回复 ID
     * @param seeLz 是否只看楼主
     * @param reverse 是否逆序
     */
    fun threadContent(
        threadId: String,
        postId: String?,
        seeLz: Boolean = false,
        reverse: Boolean = false
    ): Call<ThreadContentBean>

    /**
     * 贴页面
     *
     * @param threadId 贴 ID
     * @param page 分页页码（从 1 开始）
     * @param seeLz 是否只看楼主
     * @param reverse 是否逆序
     */
    fun threadContentAsync(
        threadId: String,
        page: Int = 1,
        seeLz: Boolean = false,
        reverse: Boolean = false
    ): Deferred<ApiResult<ThreadContentBean>>

    /**
     * 贴页面
     *
     * @param threadId 贴 ID
     * @param postId 回复 ID
     * @param seeLz 是否只看楼主
     * @param reverse 是否逆序
     */
    fun threadContentAsync(
        threadId: String,
        postId: String?,
        seeLz: Boolean = false,
        reverse: Boolean = false
    ): Deferred<ApiResult<ThreadContentBean>>

    /**
     * 推荐“不感兴趣”
     *
     * **需登录**
     *
     * @param dislikeBean “不感兴趣”信息 [com.huanchengfly.tieba.post.models.DislikeBean]
     * @param stoken stoken
     */
    fun submitDislike(
        dislikeBean: DislikeBean,
        stoken: String
    ): Call<CommonResponse>

    /**
     * 推荐“不感兴趣”
     *
     * **需登录**
     *
     * @param dislikeBean “不感兴趣”信息 [com.huanchengfly.tieba.post.models.DislikeBean]
     * @param stoken stoken
     */
    fun submitDislikeFlow(
        dislikeBean: DislikeBean
    ): Flow<CommonResponse>

    /**
     * 关注用户（web 接口）
     *
     * **需登录**
     *
     * @param portrait 头像
     * @param tbs tbs
     */
    fun follow(
        portrait: String,
        tbs: String
    ): Call<CommonResponse>

    /**
     * 取关用户（web 接口）
     *
     * **需登录**
     *
     * @param portrait 头像
     * @param tbs tbs
     */
    fun unfollow(
        portrait: String,
        tbs: String
    ): Call<CommonResponse>

    /**
     * 关注用户（客户端接口）
     *
     * **需登录**
     *
     * @param portrait 头像
     * @param tbs tbs
     */
    fun followFlow(
        portrait: String,
        tbs: String
    ): Flow<FollowBean>

    /**
     * 取关用户（客户端接口）
     *
     * **需登录**
     *
     * @param portrait 头像
     * @param tbs tbs
     */
    fun unfollowFlow(
        portrait: String,
        tbs: String
    ): Flow<CommonResponse>

    fun hotMessageList(): Call<HotMessageListBean>

    /**
     * 登录用户信息
     *
     * @param cookie 登录 Cookie 信息
     */
    fun myInfo(
        cookie: String
    ): Call<MyInfoBean>

    /**
     * 登录用户信息
     *
     * @param cookie 登录 Cookie 信息
     */
    fun myInfoAsync(
        cookie: String
    ): Deferred<ApiResult<MyInfoBean>>

    /**
     * 登录用户信息
     *
     * @param cookie 登录 Cookie 信息
     */
    fun myInfoFlow(
        cookie: String
    ): Flow<MyInfoBean>

    /**
     * 搜索吧
     *
     * @param keyword 关键词
     */
    fun searchForum(
        keyword: String
    ): Call<SearchForumBean>

    /**
     * 搜索贴
     *
     * @param keyword 关键词
     * @param page 分页页码
     * @param order 排序设置 [com.huanchengfly.tieba.api.SearchThreadOrder]
     * @param filter 过滤设置 [com.huanchengfly.tieba.api.SearchThreadFilter]
     */
    fun searchThread(
        keyword: String,
        page: Int,
        order: SearchThreadOrder,
        filter: SearchThreadFilter
    ): Call<SearchThreadBean>

    /**
     * 上传图片（web 接口）
     *
     * **需登录**
     */
    fun webUploadPic(
        photoInfoBean: PhotoInfoBean
    ): Call<WebUploadPicBean>


    /**
     * 回贴 - 回复主题贴（web 接口）
     *
     * **需登录**
     *
     * @param forumId 吧 ID
     * @param forumName 吧名
     * @param threadId 贴子 ID
     * @param tbs tbs
     * @param content 回复内容
     * @param imgInfo 图片
     * @param nickName 昵称
     * @param pn 页码
     * @param bsk BSK
     */
    fun webReply(
        forumId: String,
        forumName: String,
        threadId: String,
        tbs: String,
        content: String,
        imgInfo: String?,
        nickName: String,
        pn: String,
        bsk: String
    ): Call<WebReplyResultBean>

    /**
     * 回贴 - 回复别人的回复（web 接口）
     *
     * **需登录**
     *
     * @param forumId 吧 ID
     * @param forumName 吧名
     * @param threadId 贴子 ID
     * @param tbs tbs
     * @param content 回复内容
     * @param imgInfo 图片
     * @param nickName 昵称
     * @param postId 回复 ID
     * @param floor 回复楼层
     * @param pn 页码
     * @param bsk BSK
     */
    fun webReply(
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
    ): Call<WebReplyResultBean>

    /**
     * 回贴 - 回复楼中楼（web 接口）
     *
     * **需登录**
     *
     * @param forumId 吧 ID
     * @param forumName 吧名
     * @param threadId 贴子 ID
     * @param tbs tbs
     * @param content 回复内容
     * @param imgInfo 图片
     * @param nickName 昵称
     * @param postId 回复 ID
     * @param replyPostId 楼中楼 ID
     * @param floor 回复楼层
     * @param pn 页码
     * @param bsk BSK
     */
    fun webReply(
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
    ): Call<WebReplyResultBean>

    /**
     * 回贴 - 回复主题贴（web 接口）（异步）
     *
     * **需登录**
     *
     * @param forumId 吧 ID
     * @param forumName 吧名
     * @param threadId 贴子 ID
     * @param tbs tbs
     * @param content 回复内容
     * @param imgInfo 图片
     * @param nickName 昵称
     * @param pn 页码
     * @param bsk BSK
     */
    fun webReplyAsync(
        forumId: String,
        forumName: String,
        threadId: String,
        tbs: String,
        content: String,
        imgInfo: String?,
        nickName: String,
        pn: String,
        bsk: String
    ): Deferred<ApiResult<WebReplyResultBean>>

    /**
     * 回贴 - 回复别人的回复（web 接口）（异步）
     *
     * **需登录**
     *
     * @param forumId 吧 ID
     * @param forumName 吧名
     * @param threadId 贴子 ID
     * @param tbs tbs
     * @param content 回复内容
     * @param imgInfo 图片
     * @param nickName 昵称
     * @param postId 回复 ID
     * @param floor 回复楼层
     * @param pn 页码
     * @param bsk BSK
     */
    fun webReplyAsync(
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
    ): Deferred<ApiResult<WebReplyResultBean>>

    /**
     * 回贴 - 回复楼中楼（web 接口）（异步）
     *
     * **需登录**
     *
     * @param forumId 吧 ID
     * @param forumName 吧名
     * @param threadId 贴子 ID
     * @param tbs tbs
     * @param content 回复内容
     * @param imgInfo 图片
     * @param nickName 昵称
     * @param postId 回复 ID
     * @param replyPostId 楼中楼 ID
     * @param floor 回复楼层
     * @param pn 页码
     * @param bsk BSK
     */
    fun webReplyAsync(
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
    ): Deferred<ApiResult<WebReplyResultBean>>

    /**
     * 吧页面（web 接口）
     *
     * @param forumName 吧名
     * @param page 分页页码
     * @param goodClassifyId 精品贴分类 ID
     * @param sortType 贴排序类型
     * @param pageSize 每页贴数（默认 30）
     */
    fun webForumPage(
        forumName: String,
        page: Int,
        goodClassifyId: String? = null,
        sortType: ForumSortType = ForumSortType.REPLY_TIME,
        pageSize: Int = 30
    ): Call<ForumBean>

    /**
     * 吧页面（web 接口）
     *
     * @param forumName 吧名
     * @param page 分页页码
     * @param goodClassifyId 精品贴分类 ID
     * @param sortType 贴排序类型
     * @param pageSize 每页贴数（默认 30）
     */
    fun webForumPageAsync(
        forumName: String,
        page: Int,
        goodClassifyId: String? = null,
        sortType: ForumSortType = ForumSortType.REPLY_TIME,
        pageSize: Int = 30
    ): Deferred<ApiResult<ForumBean>>

    /**
     * 获取举报贴子/回贴页面 URL
     *
     * @param postId PID
     */
    fun checkReportPost(
        postId: String
    ): Call<CheckReportBean>

    /**
     * 获得当前用户昵称（需登录）
     */
    fun initNickNameFlow(): Flow<InitNickNameBean>

    /**
     * 获得用户昵称
     */
    fun initNickNameFlow(
        bduss: String,
        sToken: String
    ): Flow<InitNickNameBean>

    /**
     * 更新登录信息（需登录）
     */
    fun loginFlow(): Flow<LoginBean>

    /**
     * 登录
     */
    fun loginFlow(
        bduss: String,
        sToken: String
    ): Flow<LoginBean>

    /**
     * 修改个人资料
     *
     * @param birthdayShowStatus 是否仅显示星座
     * @param birthdayTime 生日时间戳 / 1000
     * @param intro 个人简介（最多 500 字）
     * @param sex 性别（1 = 男，2 = 女）
     */
    fun profileModifyFlow(
        birthdayShowStatus: Boolean,
        birthdayTime: String,
        intro: String,
        sex: String
    ): Flow<CommonResponse>

    /**
     * 上传头像
     *
     * @param file 图片 File 对象
     *
     */
    fun imgPortrait(
        file: File
    ): Flow<CommonResponse>

    /**
     * 获取吧列表
     */
    fun getForumListFlow(): Flow<GetForumListBean>

    /**
     * 一键签到（官方）
     */
    fun mSign(
        forumIds: String,
        tbs: String
    ): Flow<MSignBean>

    /**
     * 关注动态
     *
     * @param pageTag 页码
     * @param lastRequestUnix 上次请求的时间戳（10位）
     * @param loadType 加载类型（1 = 下拉刷新，2 = 加载更多）
     */
    fun userLikeFlow(
        pageTag: String,
        lastRequestUnix: Long,
        loadType: Int
    ): Flow<UserLikeResponse>

    /**
     * 首页热榜
     *
     * @param tabCode Tab
     */
    fun hotThreadListFlow(
        tabCode: String
    ): Flow<HotThreadListResponse>

    /**
     * 吧页面
     *
     * @param forumName 吧名
     * @param goodClassifyId 精品贴分类
     */
    fun frsPage(
        forumName: String,
        goodClassifyId: Int? = 0
    ): Flow<FrsPageResponse>
}