package com.huanchengfly.tieba.post.utils.compose

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.huanchengfly.tieba.post.arch.GlobalEvent
import com.huanchengfly.tieba.post.arch.emitGlobalEvent
import kotlinx.coroutines.GlobalScope

data class LaunchActivityRequest(
    val requesterId: String,
    val intent: Intent,
)

data class ActivityResult(
    val requesterId: String,
    val resultCode: Int,
    val intent: Intent?,
)

fun launchActivityForResult(
    requesterId: String,
    intent: Intent,
) {
    GlobalScope.emitGlobalEvent(GlobalEvent.StartActivityForResult(requesterId, intent))
}

class LaunchActivityForResult : ActivityResultContract<LaunchActivityRequest, ActivityResult>() {
    private var currentRequesterId: String? = null

    override fun createIntent(context: Context, input: LaunchActivityRequest): Intent {
        currentRequesterId = input.requesterId
        return input.intent
    }

    override fun parseResult(
        resultCode: Int,
        intent: Intent?,
    ): ActivityResult {
        return ActivityResult(
            requesterId = currentRequesterId ?: "",
            resultCode = resultCode,
            intent = intent
        )
    }
}