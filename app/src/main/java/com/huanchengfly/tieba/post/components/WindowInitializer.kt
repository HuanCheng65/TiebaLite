package com.huanchengfly.tieba.post.components

import android.content.Context
import androidx.startup.Initializer
import androidx.window.core.ExperimentalWindowApi
import androidx.window.embedding.RuleController
import com.huanchengfly.tieba.post.R

@ExperimentalWindowApi
class WindowInitializer : Initializer<RuleController> {
    override fun create(context: Context): RuleController {
        return RuleController.getInstance(context).apply {
            setRules(RuleController.parseRules(context, R.xml.main_split_config))
        }
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return emptyList()
    }
}