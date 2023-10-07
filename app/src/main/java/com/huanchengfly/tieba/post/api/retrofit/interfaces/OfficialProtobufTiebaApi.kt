package com.huanchengfly.tieba.post.api.retrofit.interfaces

import com.huanchengfly.tieba.post.api.models.protos.addPost.AddPostResponse
import com.huanchengfly.tieba.post.api.models.protos.forumRecommend.ForumRecommendResponse
import com.huanchengfly.tieba.post.api.models.protos.forumRuleDetail.ForumRuleDetailResponse
import com.huanchengfly.tieba.post.api.models.protos.frsPage.FrsPageResponse
import com.huanchengfly.tieba.post.api.models.protos.getBawuInfo.GetBawuInfoResponse
import com.huanchengfly.tieba.post.api.models.protos.getForumDetail.GetForumDetailResponse
import com.huanchengfly.tieba.post.api.models.protos.getLevelInfo.GetLevelInfoResponse
import com.huanchengfly.tieba.post.api.models.protos.getMemberInfo.GetMemberInfoResponse
import com.huanchengfly.tieba.post.api.models.protos.hotThreadList.HotThreadListResponse
import com.huanchengfly.tieba.post.api.models.protos.pbFloor.PbFloorResponse
import com.huanchengfly.tieba.post.api.models.protos.pbPage.PbPageResponse
import com.huanchengfly.tieba.post.api.models.protos.personalized.PersonalizedResponse
import com.huanchengfly.tieba.post.api.models.protos.profile.ProfileResponse
import com.huanchengfly.tieba.post.api.models.protos.searchSug.SearchSugResponse
import com.huanchengfly.tieba.post.api.models.protos.threadList.ThreadListResponse
import com.huanchengfly.tieba.post.api.models.protos.topicList.TopicListResponse
import com.huanchengfly.tieba.post.api.models.protos.userLike.UserLikeResponse
import com.huanchengfly.tieba.post.api.models.protos.userPost.UserPostResponse
import com.huanchengfly.tieba.post.api.retrofit.body.MyMultipartBody
import kotlinx.coroutines.flow.Flow
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface OfficialProtobufTiebaApi {
    @POST("/c/f/excellent/personalized?cmd=309264")
    fun personalizedFlow(
        @Body body: MyMultipartBody,
    ): Flow<PersonalizedResponse>

    @POST("/c/f/concern/userlike?cmd=309474")
    fun userLikeFlow(
        @Body body: MyMultipartBody,
    ): Flow<UserLikeResponse>

    @POST("/c/f/forum/hotThreadList?cmd=309661")
    fun hotThreadListFlow(
        @Body body: MyMultipartBody,
    ): Flow<HotThreadListResponse>

    @POST("/c/f/recommend/topicList?cmd=309289")
    fun topicListFlow(
        @Body body: MyMultipartBody,
    ): Flow<TopicListResponse>

    @POST("/c/f/forum/forumrecommend?cmd=303011")
    fun forumRecommendFlow(
        @Body body: MyMultipartBody,
    ): Flow<ForumRecommendResponse>

    @POST("/c/f/frs/page?cmd=301001")
    fun frsPageFlow(
        @Body body: MyMultipartBody,
        @Header("forum_name") forumName: String? = null,
    ): Flow<FrsPageResponse>

    @POST("/c/f/frs/threadlist?cmd=301002")
    fun threadListFlow(
        @Body body: MyMultipartBody,
    ): Flow<ThreadListResponse>

    @POST("/c/u/user/profile?cmd=303012&format=protobuf")
    fun profileFlow(
        @Body body: MyMultipartBody,
    ): Flow<ProfileResponse>

    @POST("/c/f/pb/page?cmd=302001&format=protobuf")
    fun pbPageFlow(
        @Body body: MyMultipartBody,
    ): Flow<PbPageResponse>

    @POST("/c/f/pb/floor?cmd=302002&format=protobuf")
    fun pbFloorFlow(
        @Body body: MyMultipartBody,
    ): Flow<PbFloorResponse>

    @POST("/c/c/post/add?cmd=309731&format=protobuf")
    fun addPostFlow(
        @Body body: MyMultipartBody,
    ): Flow<AddPostResponse>

    @POST("/c/s/searchSug?cmd=309438&format=protobuf")
    fun searchSugFlow(
        @Body body: MyMultipartBody,
    ): Flow<SearchSugResponse>

    @POST("/c/f/forum/getforumdetail?cmd=303021&format=protobuf")
    fun getForumDetailFlow(
        @Body body: MyMultipartBody,
    ): Flow<GetForumDetailResponse>

    @POST("/c/f/forum/getBawuInfo?cmd=301007&format=protobuf")
    fun getBawuInfoFlow(
        @Body body: MyMultipartBody,
    ): Flow<GetBawuInfoResponse>

    @POST("/c/f/forum/getLevelInfo?cmd=301005&format=protobuf")
    fun getLevelInfoFlow(
        @Body body: MyMultipartBody,
    ): Flow<GetLevelInfoResponse>

    @POST("/c/f/forum/getMemberInfo?cmd=301004&format=protobuf")
    fun getMemberInfoFlow(
        @Body body: MyMultipartBody,
    ): Flow<GetMemberInfoResponse>

    @POST("/c/f/forum/forumRuleDetail?cmd=309690&format=protobuf")
    fun forumRuleDetailFlow(
        @Body body: MyMultipartBody,
    ): Flow<ForumRuleDetailResponse>

    @POST("/c/u/feed/userpost?cmd=303002&format=protobuf")
    fun userPostFlow(
        @Body body: MyMultipartBody,
    ): Flow<UserPostResponse>
}