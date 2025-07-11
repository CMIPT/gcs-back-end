package edu.cmipt.gcs.constant;

public class ValidationConstant {
  public static final int MIN_PASSWORD_LENGTH = 6;
  public static final int MAX_PASSWORD_LENGTH = 20;
  public static final int MIN_USERNAME_LENGTH = 1;
  public static final int MAX_USERNAME_LENGTH = 50;
  public static final int MIN_AVATAR_URL_LENGTH = 0;
  public static final int MAX_AVATAR_URL_LENGTH = 1024;

  // the size of username and password will be check by the @Size,
  // so we just use '*' to ignore the length check
  public static final String USERNAME_PATTERN = "^[a-zA-Z0-9_-]*$";
  public static final String PASSWORD_PATTERN = "^[a-zA-Z0-9_.@-]*$";
  public static final int MIN_REPOSITORY_NAME_LENGTH = 1;
  public static final int MAX_REPOSITORY_NAME_LENGTH = 255;
  public static final int MIN_REPOSITORY_DESCRIPTION_LENGTH = 0;
  public static final int MAX_REPOSITORY_DESCRIPTION_LENGTH = 255;

  public static final int MIN_LABEL_NAME_LENGTH = 1;
  public static final int MAX_LABEL_NAME_LENGTH = 50;
  public static final int MIN_LABEL_DESCRIPTION_LENGTH = 0;
  public static final int MAX_LABEL_DESCRIPTION_LENGTH = 100;

  // the length will be checked by @Size
  public static final String REPOSITORY_NAME_PATTERN = "^[a-zA-Z0-9_-]*$";

  public static final int MIN_SSH_KEY_NAME_LENGTH = 1;
  public static final int MAX_SSH_KEY_NAME_LENGTH = 255;

  public static final int MIN_SSH_KEY_PUBLIC_KEY_LENGTH = 1;
  public static final int MAX_SSH_KEY_PUBLIC_KEY_LENGTH = 4096;

  public static final int MIN_ACTIVITY_TITLE_LENGTH = 1;
  public static final int MAX_ACTIVITY_TITLE_LENGTH = 255;
  public static final int MIN_ACTIVITY_DESCRIPTION_LENGTH = 0;
  public static final int MAX_ACTIVITY_DESCRIPTION_LENGTH = 65536;

  public static final int MIN_COMMENT_NAME_LENGTH = 1;
  public static final int MAX_COMMENT_NAME_LENGTH = 65536;
}
