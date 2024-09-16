package edu.cmipt.gcs.constant;

public class ValidationConstant {
    public static final int MIN_PASSWORD_LENGTH = 6;
    public static final int MAX_PASSWORD_LENGTH = 20;
    public static final int MIN_USERNAME_LENGTH = 1;
    public static final int MAX_USERNAME_LENGTH = 50;
    // the size of username and password will be check by the @Size,
    // so we just use '*' to ignore the length check
    public static final String USERNAME_PATTERN = "^[a-zA-Z0-9_]*$";
    public static final String PASSWORD_PATTERN = "^[a-zA-Z0-9_.@]*$";
}
