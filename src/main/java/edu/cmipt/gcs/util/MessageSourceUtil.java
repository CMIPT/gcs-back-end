package edu.cmipt.gcs.util;

import edu.cmipt.gcs.enumeration.ErrorCodeEnum;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class MessageSourceUtil {
    private static MessageSource messageSource;

    MessageSourceUtil(MessageSource messageSource) {
        MessageSourceUtil.messageSource = messageSource;
    }

    public static String getMessage(ErrorCodeEnum code, Object... args) {
        try {
            return messageSource.getMessage(code.getCode(), args, LocaleContextHolder.getLocale());
        } catch (Exception e) {
            // ignore
        }
        String message =
                messageSource.getMessage(code.getCode(), null, LocaleContextHolder.getLocale());
        Pattern pattern = Pattern.compile("\\{.*?\\}");
        Matcher matcher = pattern.matcher(message);
        int i = 0;
        while (matcher.find()) {
            message = message.replace(matcher.group(), args[i].toString());
            i++;
        }
        return message;
    }
}
