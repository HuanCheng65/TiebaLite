package com.huanchengfly.tieba.post.components.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

class OKSignWorker(
        context: Context,
        workerParams: WorkerParameters
) : Worker(context, workerParams), CoroutineScope {
    override fun doWork(): Result {
        return Result.success()
    }

    val job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job
}