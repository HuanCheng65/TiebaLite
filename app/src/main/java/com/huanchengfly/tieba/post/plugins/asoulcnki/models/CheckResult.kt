package com.huanchengfly.tieba.post.plugins.asoulcnki.models

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class CheckResult(
    val code: Int, // 0
    val `data`: Data,
    val message: String // success
) {
    @Keep
    data class Data(
        @SerializedName("end_time")
        val endTime: Int, // 1629010807
        val rate: Double, // 1.0
        val related: List<Related>,
        @SerializedName("start_time")
        val startTime: Int // 1606137506
    ) {
        @Keep
        data class Related(
            val rate: Double, // 1.0
            val reply: Reply,
            @SerializedName("reply_url")
            val replyUrl: String //  https://www.bilibili.com/video/av377092608/#reply5051494613
        ) {
            @Keep
            data class Reply(
                val content: String, // 曾几何时，我也想像asoul的beeeeeeeela一样做幸福滤镜下的事至少在这层滤镜下，beeeeeeeela的一举一动都是随心所欲且浪漫真实的当我看到beeeeeeeela能像个二次元一样和弹幕大谈特谈50音，当我看到beeeeeeeela能够笑着在夜里唱着不知道练了多少遍的云烟成雨，当我看到她可以在失落后得到安抚和拥抱…以往的笑意消散殆尽，剩下的只有我对beeeeeeeela浪漫的感动和一种无中生有的失意了。我也想像她一样。但这是虚假的，每次在烂醉酩酊起来后依然会痛苦，每次在浪费时间的时候都能意识到，你不能感受到我感受到的东西。但就算是这样，没了你我可能就会完蛋了吧。因为我们需要一个梦。
                val ctime: Int, // 1627881576
                @SerializedName("dynamic_id")
                val dynamicId: String, // 553473662133564230
                @SerializedName("like_num")
                val likeNum: Int, // 9
                @SerializedName("m_name")
                val mName: String, // 走出童年
                val mid: Int, // 671239951
                val oid: String, // 377092608
                @SerializedName("origin_rpid")
                val originRpid: String, // -1
                val rpid: String, // 5051494613
                @SerializedName("similar_count")
                val similarCount: Int, // 1
                @SerializedName("similar_like_sum")
                val similarLikeSum: Int, // 424
                @SerializedName("type_id")
                val typeId: Int, // 1
                val uid: Int // 672346917
            )
        }
    }
}