package edu.cmipt.gcs.constant;


import java.util.Date;

public class TestConstant {
  public static String ID;
  public static String USERNAME = new Date().getTime() + "";
  public static String AVATAR_URL = "https://avatars.githubusercontent.com/u/58209855?v=4";
  public static String USER_PASSWORD = "123456";
  public static String EMAIL = USERNAME + "@cmipt.edu";
  public static String ACCESS_TOKEN;
  public static String REFRESH_TOKEN;
  public static String OTHER_ID;
  public static String OTHER_USERNAME = new Date().getTime() + "other";
  public static String OTHER_USER_PASSWORD = "123456";
  public static String OTHER_EMAIL = OTHER_USERNAME + "@cmipt.edu";
  public static String OTHER_ACCESS_TOKEN;
  public static String OTHER_REFRESH_TOKEN;
  public static String REPOSITORY_ID;
  public static String REPOSITORY_NAME;
  public static String REPOSITORY_LATEST_COMMIT_HASH;
  public static Integer REPOSITORY_SIZE = 10;
  public static String OTHER_REPOSITORY_ID;
  public static String OTHER_PRIVATE_REPOSITORY_ID;
  public static String COLLABORATION_ID;
  public static Integer SSH_KEY_SIZE = 10;
  public static String SSH_KEY_ID;
  public static Integer ACTIVITY_SIZE = 5;
  public static String REPOSITORY_ACTIVITY_NUMBER;
  public static String OTHER_REPOSITORY_ACTIVITY_NUMBER;
  public static String OTHER_PRIVATE_REPOSITORY_ACTIVITY_NUMBER;
  public static String REPOSITORY_ACTIVITY_ID;
  public static String OTHER_REPOSITORY_ACTIVITY_ID;
  public static String OTHER_PRIVATE_REPOSITORY_ACTIVITY_ID;
  public static String REPOSITORY_ACTIVITY_LABEL_ID;
  public static String DELETE_REPOSITORY_ID;
  public static String ASSIGNEE_ID;
  public static Integer LABEL_SIZE = 10;
  public static String LABEL_NAME  = "test-label";
  public static String LABEL_HEX_COLOR = "#000000";
  public static String LABEL_ID;
  public static String DELETE_LABEL_ID;
  public static String COMMENT_ID;
  public static String COMMENT_CONTENT = new Date().getTime() + "comment";
  public static String REPOSITORY_DELETE_ACTIVITY_ID;
}
