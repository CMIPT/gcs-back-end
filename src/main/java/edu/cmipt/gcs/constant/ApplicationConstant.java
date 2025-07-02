package edu.cmipt.gcs.constant;

import static java.lang.Math.min;

public class ApplicationConstant {
  public static final String DEV_PROFILE = "dev";
  public static final String PROD_PROFILE = "prod";
  public static final String TEST_PROFILE = "test";
  public static final long ACCESS_TOKEN_EXPIRATION = 10 * 60 * 1000L; // 10 minutes
  public static final long REFRESH_TOKEN_EXPIRATION = 30 * 24 * 60 * 60 * 1000L; // 30 days
  public static final long EMAIL_VERIFICATION_CODE_EXPIRATION = 5 * 60 * 1000L; // 5 minutes
  public static final long EMAIL_VERIFICATION_CODE_LENGTH = 6L;
  public static final Long SERVICE_CACHE_EXPIRATION = 24 * 60 * 60 * 1000L; // 24 hours
  public static final int MAX_PAGE_TOTAL_COUNT = 1000; // maximum total count for pagination
  public static final long EMAIL_VERIFICATION_CODE_COOL_DOWN_TIME =
      min(60 * 1000L, EMAIL_VERIFICATION_CODE_EXPIRATION); // 1 minute
  public static final int CREATE_LABEL_MAX_RETRY_TIMES = 5; // maximum retry count for operations
}
