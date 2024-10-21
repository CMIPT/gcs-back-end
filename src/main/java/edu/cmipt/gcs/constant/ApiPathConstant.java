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

    public static final String USER_GET_USER_API_PATH = USER_API_PREFIX + "/get";
    public static final String USER_UPDATE_USER_API_PATH = USER_API_PREFIX + "/update";
    public static final String USER_CHECK_EMAIL_VALIDITY_API_PATH = USER_API_PREFIX + "/email";
    public static final String USER_CHECK_USERNAME_VALIDITY_API_PATH =
            USER_API_PREFIX + "/username";
    public static final String USER_CHECK_USER_PASSWORD_VALIDITY_API_PATH =
            USER_API_PREFIX + "/user-password";
    public static final String USER_DELETE_USER_API_PATH = USER_API_PREFIX + "/delete";
    public static final String USER_PAGE_USER_REPOSITORY_API_PATH =
            USER_API_PREFIX + "/page/repository";

    public static final String REPOSITORY_API_PREFIX = ALL_API_PREFIX + "/repository";
    public static final String REPOSITORY_CREATE_REPOSITORY_API_PATH =
            REPOSITORY_API_PREFIX + "/create";
    public static final String REPOSITORY_DELETE_REPOSITORY_API_PATH =
            REPOSITORY_API_PREFIX + "/delete";
    public static final String REPOSITORY_UPDATE_REPOSITORY_API_PATH =
            REPOSITORY_API_PREFIX + "/update";
    public static final String REPOSITORY_CHECK_REPOSITORY_NAME_VALIDITY_API_PATH =
            REPOSITORY_API_PREFIX + "/repository-name";
    public static final String REPOSITORY_PAGE_COLLABORATOR_API_PATH =
            REPOSITORY_API_PREFIX + "/page/collaborator";
    public static final String REPOSITORY_ADD_COLLABORATOR_API_PREFIX =
            REPOSITORY_API_PREFIX + "/add-collaborator";
    public static final String REPOSITORY_ADD_COLLABORATOR_BY_NAME_API_PATH =
            REPOSITORY_ADD_COLLABORATOR_API_PREFIX + "/name";
    public static final String REPOSITORY_ADD_COLLABORATOR_BY_EMAIL_API_PATH =
            REPOSITORY_ADD_COLLABORATOR_API_PREFIX + "/email";
    public static final String REPOSITORY_ADD_COLLABORATOR_BY_ID_API_PATH =
            REPOSITORY_ADD_COLLABORATOR_API_PREFIX + "/id";
    public static final String REPOSITORY_REMOVE_COLLABORATION_API_PATH =
            REPOSITORY_API_PREFIX + "/remove-collaborator";

    public static final String SSH_KEY_API_PREFIX = ALL_API_PREFIX + "/ssh";
    public static final String SSH_KEY_UPLOAD_SSH_KEY_API_PATH = SSH_KEY_API_PREFIX + "/upload";
    public static final String SSH_KEY_UPDATE_SSH_KEY_API_PATH = SSH_KEY_API_PREFIX + "/update";
    public static final String SSH_KEY_DELETE_SSH_KEY_API_PATH = SSH_KEY_API_PREFIX + "/delete";
    public static final String SSH_KEY_PAGE_SSH_KEY_API_PATH = SSH_KEY_API_PREFIX + "/page";
}
