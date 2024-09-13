package edu.cmipt.gcs.constant;

public class ApiPathConstant {
    public static final String ALL_API_PREFIX = "/gcs";

    public static final String AUTHENTICATION_API_PREFIX = ALL_API_PREFIX + "/auth";
    public static final String AUTHENTICATION_SIGN_IN_API_PATH =
            AUTHENTICATION_API_PREFIX + "/signin";
    public static final String AUTHENTICATION_SIGN_UP_API_PATH =
            AUTHENTICATION_API_PREFIX + "/signup";
    public static final String AUTHENTICATION_SIGN_OUT_API_PATH =
            AUTHENTICATION_API_PREFIX + "/signout";
    public static final String AUTHENTICATION_REFRESH_API_PATH =
            AUTHENTICATION_API_PREFIX + "/refresh";

    public static final String DEVELOPMENT_API_PREFIX = ALL_API_PREFIX + "/developer";
    public static final String DEVELOPMENT_GET_API_MAP_API_PATH = DEVELOPMENT_API_PREFIX + "/api";
    public static final String DEVELOPMENT_GET_ERROR_MESSAGE_API_PATH =
            DEVELOPMENT_API_PREFIX + "/error";

    public static final String USER_API_PREFIX = ALL_API_PREFIX + "/user";

    public static final String USER_GET_USER_BY_NAME_API_PATH = USER_API_PREFIX + "/{username}";
    public static final String USER_CHECK_EMAIL_VALIDITY_API_PATH = USER_API_PREFIX + "/email";
    public static final String USER_CHECK_USERNAME_VALIDITY_API_PATH =
            USER_API_PREFIX + "/username";
}
