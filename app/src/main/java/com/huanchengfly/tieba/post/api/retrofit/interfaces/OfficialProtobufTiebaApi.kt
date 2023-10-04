package com.huanchengfly.tieba.post.api.retrofit.interfaces

import com.huanchengfly.tieba.post.api.models.protos.addPost.AddPostResponse
import com.huanchengfly.tieba.post.api.models.protos.forumRecommend.ForumRecommendResponse
import com.huanchengfly.tieba.post.api.models.protos.frsPage.FrsPageResponse
import com.huanchengfly.tieba.post.api.models.protos.hotThreadList.HotThreadListResponse
import com.huanchengfly.tieba.post.api.models.protos.pbFloor.PbFloorResponse
import com.huanchengfly.tieba.post.api.models.protos.pbPage.PbPageResponse
import com.huanchengfly.tieba.post.api.models.protos.personalized.PersonalizedResponse
import com.huanchengfly.tieba.post.api.models.protos.profile.ProfileResponse
import com.huanchengfly.tieba.post.api.models.protos.searchSug.SearchSugResponse
import com.huanchengfly.tieba.post.api.models.protos.threadList.ThreadListResponse
import com.huanchengfly.tieba.post.api.models.protos.topicList.TopicListResponse
import com.huanchengfly.tieba.post.api.models.protos.userLike.UserLikeResponse
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

    @POST("/c/u/user/profile?cmd=303012")
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
}