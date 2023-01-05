package com.huanchengfly.tieba.post.api.retrofit.interfaces

import com.huanchengfly.tieba.post.api.models.protos.frsPage.FrsPageResponse
import com.huanchengfly.tieba.post.api.models.protos.hotThreadList.HotThreadListResponse
import com.huanchengfly.tieba.post.api.models.protos.personalized.PersonalizedResponse
import com.huanchengfly.tieba.post.api.models.protos.topicList.TopicListResponse
import com.huanchengfly.tieba.post.api.models.protos.userLike.UserLikeResponse
import com.huanchengfly.tieba.post.api.retrofit.body.MyMultipartBody
import kotlinx.coroutines.flow.Flow
import retrofit2.http.Body
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

    @POST("/c/f/frs/page?cmd=301001")
    fun frsPageFlow(
        @Body body: MyMultipartBody,
    ): Flow<FrsPageResponse>
}