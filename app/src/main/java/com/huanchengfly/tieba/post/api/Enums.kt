package com.huanchengfly.tieba.post.api

enum class ClientVersion(val version: String) {
    TIEBA_V11("11.10.8.6"),
    TIEBA_V12("12.52.1.0"),
    TIEBA_V12_POST("12.35.1.0");

    override fun toString(): String {
        return version
    }
}

enum class ForumSortType(val value: Int) {
    REPLY_TIME(0),
    SEND_TIME(1),
    ONLY_FOLLOWED(2);

    override fun toString(): String {
        return value.toString()
    }

    companion object {
        @JvmStatic
        fun valueOf(value: Int): ForumSortType {
            return when (value) {
                REPLY_TIME.value -> REPLY_TIME
                SEND_TIME.value -> SEND_TIME
                ONLY_FOLLOWED.value -> ONLY_FOLLOWED
                else -> throw IllegalArgumentException()
            }
        }
    }
}

enum class SearchThreadOrder(val value: Int) {
    NEW(1),
    OLD(0),
    RELEVANT(2);

    override fun toString(): String {
        return value.toString()
    }

    companion object {
        @JvmStatic
        fun valueOf(value: Int): SearchThreadOrder {
            return when (value) {
                NEW.value -> NEW
                OLD.value -> OLD
                RELEVANT.value -> RELEVANT
                else -> throw IllegalArgumentException()
            }
        }
    }
}

enum class SearchThreadFilter(val value: Int) {
    ONLY_THREAD(1),
    ALL(2);

    override fun toString(): String {
        return value.toString()
    }

    companion object {
        @JvmStatic
        fun valueOf(value: Int): SearchThreadFilter {
            return when (value) {
                ONLY_THREAD.value -> ONLY_THREAD
                ALL.value -> ALL
                else -> throw IllegalArgumentException()
            }
        }
    }
}