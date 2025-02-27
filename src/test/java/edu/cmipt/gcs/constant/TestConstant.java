package edu.cmipt.gcs.constant;

import java.util.Date;

public class TestConstant {
    public static String ID;
    public static String USERNAME = new Date().getTime() + "";
    public static String AVATAR_URL = "https://www.example.com/avatar.jpg";
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
    public static Integer REPOSITORY_SIZE = 10;
    public static Integer SSH_KEY_SIZE = 10;
    public static String SSH_KEY_ID;
}
