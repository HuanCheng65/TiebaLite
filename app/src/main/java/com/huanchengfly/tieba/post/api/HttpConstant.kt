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
    const val ERROR_NET = 10
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

    const val ADD_COOKIE = "add_cookie"
    const val ADD_COOKIE_FALSE = "false"

    const val ACCEPT_LANGUAGE = "Accept-Language"
    const val ACCEPT_LANGUAGE_VALUE = "zh-CN,zh;q=0.9"
    const val COOKIE = "cookie"
    const val HOST = "Host"
    const val ORIGIN = "Origin"
    const val REFERER = "Referer"
    const val USER_AGENT = "User-Agent"
    const val PRAGMA = "Pragma"
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
    const val BDUSS = "BDUSS"
    const val CLIENT_VERSION = "_client_version"
    const val CLIENT_TYPE = "_client_type"
    const val CLIENT_ID = "_client_id"
    const val PHONE_IMEI = "_phone_imei"
    const val CUID = "cuid"
    const val CUID_GALAXY2 = "cuid_galaxy2"
    const val CUID_GALAXY3 = "c3_aid"
    const val OAID = "oaid"
    const val CUID_GID = "cuid_gid"
    const val FROM = "from"
    const val NET_TYPE = "net_type"
    const val MODEL = "model"
    const val OS_VERSION = "_os_version"
    const val TIMESTAMP = "timestamp"
    const val SIGN = "sign"
    const val SUBAPP_TYPE = "subapp_type"
    const val STOKEN = "stoken"
}