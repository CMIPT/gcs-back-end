package edu.cmipt.gcs.constant;

import java.util.Date;

public class TestConstant {
    public static String USERNAME = new Date().getTime() + "";
    public static String USER_PASSWORD = "123456";
    public static String EMAIL = USERNAME + "@cmipt.edu";
    public static String ACCESS_TOKEN;
    public static String REFRESH_TOKEN;
}
