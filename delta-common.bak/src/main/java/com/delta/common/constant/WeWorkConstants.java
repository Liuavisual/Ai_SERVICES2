package com.delta.common.constant;

public final class WeWorkConstants {

    public static final String API_BASE_URL = "https://qyapi.weixin.qq.com";
    public static final String TOKEN_PATH = "/cgi-bin/gettoken";
    public static final String SEND_MESSAGE_PATH = "/cgi-bin/message/send";
    public static final String SEND_WELCOME_MSG_PATH = "/cgi-bin/externalcontact/send_welcome_msg";
    public static final String GET_EXTERNAL_CONTACT_PATH = "/cgi-bin/externalcontact/get";
    public static final String LIST_EXTERNAL_CONTACT_PATH = "/cgi-bin/externalcontact/list";
    public static final String GET_USER_INFO_PATH = "/cgi-bin/user/get";
    public static final String GET_FOLLOW_USER_LIST_PATH = "/cgi-bin/externalcontact/get_follow_user_list";

    public static final String MSG_TYPE_TEXT = "text";
    public static final String MSG_TYPE_IMAGE = "image";
    public static final String MSG_TYPE_EVENT = "event";

    public static final String EVENT_ADD_EXTERNAL_CONTACT = "add_external_contact";
    public static final String EVENT_DEL_EXTERNAL_CONTACT = "del_external_contact";
    public static final String EVENT_CHANGE_EXTERNAL_CONTACT = "change_external_contact";

    public static final String TOKEN_CACHE_PREFIX = "delta:wework:token:";
    public static final String TOKEN_LOCK_PREFIX = "delta:wework:token_lock:";
    public static final long TOKEN_TTL_SECONDS = 7000L;
    public static final long TOKEN_LOCK_WAIT_SECONDS = 10L;

    public static final String TOKEN_TYPE_APP = "app";
    public static final String TOKEN_TYPE_CONTACT = "contact";

    public static final String ERRCODE_SUCCESS = "0";
    public static final int ERRCODE_INVALID_TOKEN = 40001;
    public static final int ERRCODE_EXPIRED_TOKEN = 42001;
    public static final int ERRCODE_API_LIMIT = 45009;

    public static final int MAX_RETRY_COUNT = 3;
    public static final long RETRY_INITIAL_DELAY_MS = 1000L;
    public static final long RETRY_MAX_DELAY_MS = 10000L;

    private WeWorkConstants() {
    }
}
