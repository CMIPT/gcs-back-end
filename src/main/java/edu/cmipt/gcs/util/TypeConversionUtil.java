package edu.cmipt.gcs.util;

import edu.cmipt.gcs.enumeration.ErrorCodeEnum;

public class TypeConversionUtil {

    public static Long convertToLong(String value)
    {
        return convertToLong(value, false, null);
    }
    public static Long convertToLong(String value, boolean throwWhenFailed, Long defaultValue)
    {
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException e) {
            if (throwWhenFailed) {
                throw new IllegalArgumentException(
                    MessageSourceUtil.getMessage(ErrorCodeEnum.INVALID_LONG_VALUE, value));
            }
            return defaultValue;
        }
    }
}
