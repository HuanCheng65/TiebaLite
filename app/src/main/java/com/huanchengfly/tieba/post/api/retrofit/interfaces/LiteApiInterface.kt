package com.huanchengfly.tieba.post.api.retrofit.interfaces

import com.huanchengfly.tieba.post.api.retrofit.ApiResult
import kotlinx.coroutines.Deferred
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST

interface LiteApiInterface {
    @GET("https://huancheng65.github.io/TiebaLite/wallpapers.json")
    fun wallpapersAsync(): Deferred<ApiResult<List<String>>>

    @POST("https://pastebin.com/api/api_post.php")
    @FormUrlEncoded
    fun pastebinAsync(
        @Field("api_paste_name") name: String,
        @Field("api_paste_code") content: String,
        @Field("api_dev_key") apiDevKey: String = "CB4NlNwnukUaLURHqIbL-IQgdCJTkB7l",
        @Field("api_paste_private") private: String = "1",
        @Field("api_paste_expire_date") expireDate: String = "1W",
        @Field("api_paste_format") format: String = "markdown",
    ): Deferred<ApiResult<String>>
}