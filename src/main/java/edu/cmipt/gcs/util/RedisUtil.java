package edu.cmipt.gcs.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RedisUtil {
  private static final Logger logger = LoggerFactory.getLogger(RedisUtil.class);

  /**
   * Generate a key for Redis.
   *
   * @param target the target object
   * @param key the key of the object
   * @return the generated key
   */
  public static String generateKey(Object target, String key) {
    logger.debug("Generate key: {}#{}", target.getClass().getSimpleName(), key);
    return String.format("%s#%s", target.getClass().getSimpleName(), key);
  }

  /**
   * Generate a key for Redis.
   *
   * @param target the target class
   * @param key the key of the object
   * @return the generated key
   */
  public static String generateKey(Class<?> target, String key) {
    logger.debug("Generate key: {}#{}", target.getSimpleName(), key);
    return String.format("%s#%s", target.getSimpleName(), key);
  }
}
