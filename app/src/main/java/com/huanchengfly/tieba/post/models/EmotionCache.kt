package com.huanchengfly.tieba.post.models

data class EmotionCache(
    var ids: List<String> = emptyList(),
    var mapping: Map<String, String> = emptyMap()
)
