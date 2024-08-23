package edu.cmipt.gcs.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

@Component
public class MessageSourceUtil {
    private static MessageSource messageSource;

    MessageSourceUtil(MessageSource messageSource) {
        MessageSourceUtil.messageSource = messageSource;
    }

    public static String getMessage(String code, Object ... args) {
        String message = messageSource.getMessage(code, null, null);
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
