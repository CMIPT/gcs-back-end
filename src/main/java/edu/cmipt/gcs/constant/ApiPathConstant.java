package edu.cmipt.gcs.constant;

public class ApiPathConstant {
    public static final String ALL_API_PREFIX = "/gcs";

    public static final String AUTHENTICATION_API_PREFIX = ALL_API_PREFIX + "/auth";
    public static final String AUTHENTICATION_SIGN_IN_API_PATH = AUTHENTICATION_API_PREFIX + "/signin";
    public static final String AUTHENTICATION_SIGN_UP_API_PATH = AUTHENTICATION_API_PREFIX + "/signup";
    public static final String AUTHENTICATION_SIGN_OUT_API_PATH = AUTHENTICATION_API_PREFIX + "/signout";
    public static final String AUTHENTICATION_REFRESH_API_PATH = AUTHENTICATION_API_PREFIX + "/refresh";

    public static final String DEVELOPMENT_API_PREFIX = ALL_API_PREFIX + "/developer";
    public static final String DEVELOPMENT_GET_API_MAP_API_PATH = DEVELOPMENT_API_PREFIX + "/api";
    public static final String DEVELOPMENT_GET_ERROR_MESSAGE_API_PATH = DEVELOPMENT_API_PREFIX + "/error";
}