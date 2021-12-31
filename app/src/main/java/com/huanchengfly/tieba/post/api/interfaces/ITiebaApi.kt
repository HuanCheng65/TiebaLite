package com.huanchengfly.tieba.post.api.interfaces

import com.huanchengfly.tieba.post.api.ForumSortType
import com.huanchengfly.tieba.post.api.SearchThreadFilter
import com.huanchengfly.tieba.post.api.SearchThreadOrder
import com.huanchengfly.tieba.post.api.models.*
import com.huanchengfly.tieba.post.api.models.web.ForumBean
import com.huanchengfly.tieba.post.api.models.web.HotMessageListBean
import com.huanchengfly.tieba.post.api.retrofit.ApiResult
import com.huanchengfly.tieba.post.models.DislikeBean
import com.huanchengfly.tieba.post.models.MyInfoBean
import com.huanchengfly.tieba.post.models.PhotoInfoBean
import kotlinx.coroutines.Deferred
import retrofit2.Call

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
     * 给贴子/回复点赞
     *
     * **需登录**
     *
     * @param threadId 贴子 ID
     * @param postId 回复 ID
     */
    fun agree(
        threadId: String,
        postId: String
    ): Call<AgreeBean>

    /**
     * 给贴子/回复点踩
     *
     * **需登录**
     *
     * @param threadId 贴子 ID
     * @param postId 回复 ID
     */
    fun disagree(
        threadId: String,
        postId: String
    ): Call<AgreeBean>

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
     * @param objType 原页面类型（pb - 贴页面 frs - 吧页面）
     * @param prev 不明，默认为 false
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
     * 用户信息
     *
     * @param uid 用户 ID
     */
    fun profile(
        uid: String
    ): Call<ProfileBean>

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
     * @param page 分页页码（从 1 开始）
     * @param pageSize 每页贴数（默认 30）
     */
    fun searchPost(
        keyword: String,
        forumName: String,
        onlyThread: Boolean = false,
        page: Int = 1,
        pageSize: Int = 30
    ): Call<SearchPostBean>

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
}