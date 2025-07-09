package edu.cmipt.gcs.constant;

public class ApiPathConstant {
  public static final String ALL_API_PREFIX = "/api";

  public static final String AUTHENTICATION_API_PREFIX = ALL_API_PREFIX + "/auth";
  public static final String AUTHENTICATION_SIGN_IN_API_PATH =
      AUTHENTICATION_API_PREFIX + "/signin";
  public static final String AUTHENTICATION_SIGN_OUT_API_PATH =
      AUTHENTICATION_API_PREFIX + "/signout";
  public static final String AUTHENTICATION_REFRESH_API_PATH =
      AUTHENTICATION_API_PREFIX + "/refresh";
  public static final String AUTHENTICATION_SEND_EMAIL_VERIFICATION_CODE_API_PATH =
      AUTHENTICATION_API_PREFIX + "/send-email-verification-code";

  public static final String DEVELOPMENT_API_PREFIX = ALL_API_PREFIX + "/developer";
  public static final String DEVELOPMENT_GET_API_MAP_API_PATH = DEVELOPMENT_API_PREFIX + "/api";
  public static final String DEVELOPMENT_GET_ERROR_MESSAGE_API_PATH =
      DEVELOPMENT_API_PREFIX + "/error";
  public static final String DEVELOPMENT_GET_VO_AS_TS_API_PATH =
      DEVELOPMENT_API_PREFIX + "/vo-as-ts";

  public static final String USER_API_PREFIX = ALL_API_PREFIX + "/user";

  public static final String USER_CREATE_USER_API_PATH = USER_API_PREFIX + "/create";
  public static final String USER_GET_USER_API_PATH = USER_API_PREFIX + "/get";
  public static final String USER_UPDATE_USER_API_PATH = USER_API_PREFIX + "/update";
  public static final String USER_CHECK_EMAIL_VALIDITY_API_PATH = USER_API_PREFIX + "/email";
  public static final String USER_CHECK_USERNAME_VALIDITY_API_PATH = USER_API_PREFIX + "/username";
  public static final String USER_CHECK_USER_PASSWORD_VALIDITY_API_PATH =
      USER_API_PREFIX + "/user-password";
  public static final String USER_DELETE_USER_API_PATH = USER_API_PREFIX + "/delete";
  public static final String USER_UPDATE_USER_PASSWORD_WITH_OLD_PASSWORD_API_PATH =
      USER_API_PREFIX + "/update-password-with-old-password";
  public static final String USER_UPDATE_USER_PASSWORD_WITH_EMAIL_VERIFICATION_CODE_API_PATH =
      USER_API_PREFIX + "/update-password-with-email-verification-code";

  public static final String REPOSITORY_API_PREFIX = ALL_API_PREFIX + "/repository";
  public static final String REPOSITORY_GET_REPOSITORY_API_PATH = REPOSITORY_API_PREFIX + "/get";
  public static final String REPOSITORY_CREATE_REPOSITORY_API_PATH =
      REPOSITORY_API_PREFIX + "/create";
  public static final String REPOSITORY_DELETE_REPOSITORY_API_PATH =
      REPOSITORY_API_PREFIX + "/delete";
  public static final String REPOSITORY_UPDATE_REPOSITORY_API_PATH =
      REPOSITORY_API_PREFIX + "/update";
  public static final String REPOSITORY_PAGE_REPOSITORY_API_PATH = REPOSITORY_API_PREFIX + "/page";
  public static final String REPOSITORY_CHECK_REPOSITORY_NAME_VALIDITY_API_PATH =
      REPOSITORY_API_PREFIX + "/repository-name";
  public static final String REPOSITORY_PAGE_COLLABORATOR_API_PATH =
      REPOSITORY_API_PREFIX + "/page/collaborator";
  public static final String REPOSITORY_ADD_COLLABORATOR_API_PATH =
      REPOSITORY_API_PREFIX + "/add-collaborator";
  public static final String REPOSITORY_REMOVE_COLLABORATION_API_PATH =
      REPOSITORY_API_PREFIX + "/remove-collaborator";
  public static final String REPOSITORY_GET_REPOSITORY_DIRECTORY_WITH_REF_API_PATH =
      REPOSITORY_API_PREFIX + "/get-directory-with-ref";
  public static final String REPOSITORY_GET_REPOSITORY_FILE_WITH_REF_API_PATH =
      REPOSITORY_API_PREFIX + "/get-file-with-ref";
  public static final String REPOSITORY_GET_REPOSITORY_COMMIT_DETAILS_API_PATH =
      REPOSITORY_API_PREFIX + "/get-commit-details";
  public static final String REPOSITORY_CHECK_COLLABORATION_VALIDITY_API_PATH =
      REPOSITORY_API_PREFIX + "/collaboration-validity";
  public static final String REPOSITORY_PAGE_COMMIT_WITH_REF_API_PATH =
      REPOSITORY_API_PREFIX + "/page/commit-with-ref";
  public static final String REPOSITORY_DELETE_LABEL_API_PATH =
      REPOSITORY_API_PREFIX + "/delete-label";
  public static final String REPOSITORY_CREATE_LABEL_API_PATH =
      REPOSITORY_API_PREFIX + "/create-label";
  public static final String REPOSITORY_UPDATE_LABEL_API_PATH =
      REPOSITORY_API_PREFIX + "/update-label";
  public static final String REPOSITORY_PAGE_LABEL_API_PATH = REPOSITORY_API_PREFIX + "/page-label";

  public static final String SSH_KEY_API_PREFIX = ALL_API_PREFIX + "/ssh";
  public static final String SSH_KEY_UPLOAD_SSH_KEY_API_PATH = SSH_KEY_API_PREFIX + "/upload";
  public static final String SSH_KEY_UPDATE_SSH_KEY_API_PATH = SSH_KEY_API_PREFIX + "/update";
  public static final String SSH_KEY_DELETE_SSH_KEY_API_PATH = SSH_KEY_API_PREFIX + "/delete";
  public static final String SSH_KEY_PAGE_SSH_KEY_API_PATH = SSH_KEY_API_PREFIX + "/page";
  public static final String SSH_KEY_CHECK_SSH_KEY_NAME_VALIDITY_API_PATH =
      SSH_KEY_API_PREFIX + "/ssh-key-name";
  public static final String SSH_KEY_CHECK_SSH_KEY_PUBLIC_KEY_VALIDITY_API_PATH =
      SSH_KEY_API_PREFIX + "/ssh-key-publickey";

  public static final String ACTIVITY_API_PREFIX = ALL_API_PREFIX + "/activity";
  public static final String ACTIVITY_GET_ACTIVITY_API_PATH = ACTIVITY_API_PREFIX + "/get";
  public static final String ACTIVITY_CREATE_ACTIVITY_API_PATH = ACTIVITY_API_PREFIX + "/create";
  public static final String ACTIVITY_DELETE_ACTIVITY_API_PATH = ACTIVITY_API_PREFIX + "/delete";
  public static final String ACTIVITY_UPDATE_ACTIVITY_CONTENT_API_PATH = ACTIVITY_API_PREFIX + "/update-content";
  public static final String ACTIVITY_UPDATE_ACTIVITY_LOCK_STATE_API_PATH = ACTIVITY_API_PREFIX + "/update-lock-state";
  public static final String ACTIVITY_UPDATE_ACTIVITY_CLOSE_STATE_API_PATH = ACTIVITY_API_PREFIX + "/update-close-state";
  public static final String ACTIVITY_PAGE_ACTIVITY_API_PATH = ACTIVITY_API_PREFIX + "/page";
  public static final String ACTIVITY_PAGE_SUB_ISSUE_API_PATH = ACTIVITY_API_PREFIX + "/page-sub-issue";
  public static final String ACTIVITY_CREATE_SUB_ISSUE_API_PATH =
      ACTIVITY_API_PREFIX + "/create-sub-issue";
  public static final String ACTIVITY_ADD_SUB_ISSUE_API_PATH = ACTIVITY_API_PREFIX + "/add-sub-issue";
  public static final String ACTIVITY_REMOVE_SUB_ISSUE_API_PATH = ACTIVITY_API_PREFIX + "/remove-sub-issue";
  public static final String ACTIVITY_CREATE_COMMENT_API_PATH =
      ACTIVITY_API_PREFIX + "/create-comment";
  public static final String ACTIVITY_UPDATE_COMMENT_CONTENT_API_PATH =
      ACTIVITY_API_PREFIX + "/update-comment-content";
  public static final String ACTIVITY_UPDATE_COMMENT_RESOLVED_STATE_API_PATH =
          ACTIVITY_API_PREFIX + "/update-comment-resolved-state";
    public static final String ACTIVITY_UPDATE_COMMENT_HIDDEN_STATE_API_PATH =
          ACTIVITY_API_PREFIX + "/update-comment-hidden-state";
  public static final String ACTIVITY_DELETE_COMMENT_API_PATH =
      ACTIVITY_API_PREFIX + "/delete-comment";
  public static final String ACTIVITY_PAGE_COMMENT_API_PATH = ACTIVITY_API_PREFIX + "/page-comment";
  public static final String ACTIVITY_PAGE_SUB_COMMENT_API_PATH = ACTIVITY_API_PREFIX + "/page-sub-comment";
  public static final String ACTIVITY_ADD_ASSIGNEE_API_PATH = ACTIVITY_API_PREFIX + "/add-assignee";
  public static final String ACTIVITY_DELETE_ASSIGNEE_API_PATH =
      ACTIVITY_API_PREFIX + "/delete-assignee";
  public static final String ACTIVITY_PAGE_ASSIGNEE_API_PATH =
      ACTIVITY_API_PREFIX + "/page-assignee";

  public static final String ACTIVITY_DELETE_LABEL_API_PATH = ACTIVITY_API_PREFIX + "/delete-label";
  public static final String ACTIVITY_ADD_LABEL_API_PATH = ACTIVITY_API_PREFIX + "/add-label";
  public static final String ACTIVITY_PAGE_LABEL_API_PATH = ACTIVITY_API_PREFIX + "/page-label";
}
