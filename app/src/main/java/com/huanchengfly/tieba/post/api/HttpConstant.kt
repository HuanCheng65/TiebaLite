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


object Url {
    const val UPDATE_INFO = "https://huancheng65.github.io/TiebaLite/update_v2.json"
    const val CHECK_UPDATE = "https://tblite.huanchengfly.tk/api/update"
    const val CHANGELOG = "https://tblite.huanchengfly.tk/api/changelog/"
}


object Header {
    const val FORCE_PARAM = "force_param"
    const val FORCE_PARAM_QUERY = "query"

    const val FORCE_LOGIN = "force_login"
    const val FORCE_LOGIN_TRUE = "true"

    const val ADD_COOKIE = "add_cookie"
    const val ADD_COOKIE_FALSE = "false"

    const val COOKIE = "cookie"
    const val HOST = "Host"
    const val ORIGIN = "Origin"
    const val REFERER = "Referer"
    const val USER_AGENT = "User-Agent"
    const val PRAGMA = "Pragma"
    const val CUID = "cuid"
    const val CUID_GALAXY2 = "cuid_galaxy2"
    const val CUID_GID = "cuid_gid"
}


object Param {
    const val BDUSS = "BDUSS"
    const val CLIENT_VERSION = "_client_version"
    const val CLIENT_TYPE = "_client_type"
    const val CLIENT_ID = "_client_id"
    const val PHONE_IMEI = "_phone_imei"
    const val CUID = "cuid"
    const val CUID_GALAXY2 = "cuid_galaxy2"
    const val CUID_GID = "cuid_gid"
    const val FROM = "from"
    const val NET_TYPE = "net_type"
    const val MODEL = "model"
    const val OS_VERSION = "_os_version"
    const val TIMESTAMP = "timestamp"
    const val SIGN = "sign"
    const val SUBAPP_TYPE = "subapp_type"
}