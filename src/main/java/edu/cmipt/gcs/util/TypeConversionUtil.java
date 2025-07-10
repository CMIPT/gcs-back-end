package edu.cmipt.gcs.util;

import edu.cmipt.gcs.enumeration.ErrorCodeEnum;
import edu.cmipt.gcs.exception.GenericException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TypeConversionUtil {
  private static final Logger logger = LoggerFactory.getLogger(TypeConversionUtil.class);

  public static Long convertToLong(String value, boolean throwWhenFailed) {
    return convertToLong(value, throwWhenFailed, null);
  }

  public static Long convertToLong(String value) {
    return convertToLong(value, false, null);
  }

  public static Long convertToLong(String value, boolean throwWhenFailed, Long defaultValue) {
    try {
      return Long.valueOf(value);
    } catch (NumberFormatException e) {
      if (throwWhenFailed) {
        logger.error(e.getMessage());
        throw new GenericException(ErrorCodeEnum.MESSAGE_CONVERSION_ERROR);
      }
      return defaultValue;
    }
  }
}
