package edu.cmipt.gcs.constant;

public class ApplicationConstant {
    public static final String DEV_PROFILE = "dev";
    public static final String PROD_PROFILE = "prod";
    public static final String TEST_PROFILE = "test";
    public static final long ACCESS_TOKEN_EXPIRATION = 10 * 60 * 1000L; // 10 minutes
    public static final long REFRESH_TOKEN_EXPIRATION = 30 * 24 * 60 * 60 * 1000L; // 30 days
}
