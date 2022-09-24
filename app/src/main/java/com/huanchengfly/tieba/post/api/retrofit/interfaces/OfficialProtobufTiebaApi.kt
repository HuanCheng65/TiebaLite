package com.huanchengfly.tieba.post.api.retrofit.interfaces

import com.huanchengfly.tieba.post.api.models.protos.personalized.PersonalizedResponse
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
}