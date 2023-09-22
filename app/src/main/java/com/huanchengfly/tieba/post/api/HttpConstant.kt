package com.huanchengfly.tieba.post.api


object Method {
    const val GET = "GET"
    const val POST = "POST"
    const val PATCH = "PATCH"
    const val PUT = "PUT"
    const val DELETE = "DELETE"
    const val OPTION = "OPTION"
}

object Error {
    const val ERROR_NETWORK = 10
    const val ERROR_UNKNOWN = -1
    const val ERROR_PARSE = -2
    const val ERROR_NOT_LOGGED_IN = 11
    const val ERROR_LOGGED_IN_EXPIRED = 12
    const val ERROR_UPDATE_NOT_ENABLE = 100
}

object Header {
    const val FORCE_PARAM = "force_param"
    const val FORCE_PARAM_QUERY = "query"

    const val FORCE_LOGIN = "force_login"
    const val FORCE_LOGIN_TRUE = "true"

    const val DROP_PARAMS = "drop_params"
    const val DROP_HEADERS = "drop_headers"
    const val NO_COMMON_PARAMS = "no_common_params"

    const val ADD_WEB_COOKIE = "add_cookie"
    const val ADD_WEB_COOKIE_FALSE = "false"

    const val ACCEPT_LANGUAGE = "Accept-Language"
    const val ACCEPT = "Accept"
    const val ACCEPT_LANGUAGE_VALUE = "zh-CN,zh;q=0.9"
    const val COOKIE = "cookie"
    const val HOST = "Host"
    const val ORIGIN = "Origin"
    const val REFERER = "Referer"
    const val USER_AGENT = "User-Agent"
    const val PRAGMA = "Pragma"
    const val CACHE_CONTROL = "Cache-Control"
    const val CUID = "cuid"
    const val CHARSET = "Charset"
    const val CUID_GALAXY2 = "cuid_galaxy2"
    const val CUID_GALAXY3 = "c3_aid"
    const val CUID_GID = "cuid_gid"
    const val CLIENT_TYPE = "client_type"
    const val CLIENT_USER_TOKEN = "client_user_token"
    const val CLIENT_LOG_ID = "client_logid"
    const val X_BD_DATA_TYPE = "x_bd_data_type"
}


object Param {
    const val ACTIVE_TIMESTAMP = "active_timestamp"
    const val ANDROID_ID = "android_id"
    const val BAIDU_ID = "baiduid"
    const val BDUSS = "BDUSS"
    const val BRAND = "brand"
    const val CLIENT_VERSION = "_client_version"
    const val CLIENT_TYPE = "_client_type"
    const val CLIENT_ID = "_client_id"
    const val PHONE_IMEI = "_phone_imei"
    const val CMODE = "cmode"
    const val CUID = "cuid"
    const val CUID_GALAXY2 = "cuid_galaxy2"
    const val CUID_GALAXY3 = "c3_aid"
    const val OAID = "oaid"
    const val CUID_GID = "cuid_gid"
    const val DEVICE_SCORE = "device_score"
    const val EVENT_DAY = "event_day"
    const val EXTRA = "extra"
    const val FIRST_INSTALL_TIME = "first_install_time"
    const val FROM = "from"
    const val FRAMEWORK_VER = "framework_ver"
    const val IS_TEENAGER = "is_teenager"
    const val LAST_UPDATE_TIME = "last_update_time"
    const val MAC = "mac"
    const val MODEL = "model"
    const val NET_TYPE = "net_type"
    const val OS_VERSION = "_os_version"
    const val SAMPLE_ID = "sample_id"
    const val SDK_VER = "sdk_ver"
    const val SIGN = "sign"
    const val START_SCHEME = "start_scheme"
    const val START_TYPE = "start_type"
    const val SUBAPP_TYPE = "subapp_type"
    const val STOKEN = "stoken"
    const val SWAN_GAME_VER = "swan_game_ver"
    const val TIMESTAMP = "timestamp"
    const val Z_ID = "z_id"
}