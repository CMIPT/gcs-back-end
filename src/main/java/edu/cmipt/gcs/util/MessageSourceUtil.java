package edu.cmipt.gcs.util;

import edu.cmipt.gcs.enumeration.ErrorCodeEnum;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

@Component
public class MessageSourceUtil {
  private static MessageSource messageSource;

  MessageSourceUtil(MessageSource messageSource) {
    MessageSourceUtil.messageSource = messageSource;
  }

  public static String getMessage(ErrorCodeEnum code, Object... args) {
    return getMessage(code.getCode(), args);
  }

  public static String getMessage(String code, Object... args) {
    try {
      return messageSource.getMessage(code, args, LocaleContextHolder.getLocale());
    } catch (Exception e) {
      // ignore
    }
    try {
      return messageSource.getMessage(code, null, LocaleContextHolder.getLocale());
    } catch (Exception e) {
      return "";
    }
  }
}
