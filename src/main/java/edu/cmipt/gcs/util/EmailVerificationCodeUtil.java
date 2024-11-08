package edu.cmipt.gcs.util;

import edu.cmipt.gcs.constant.ApplicationConstant;
import edu.cmipt.gcs.constant.ValidationConstant;

public class EmailVerificationCodeUtil {
    public static String generateVerificationCode(String email) {
        String code = String.valueOf((int) ((Math.random() * 9 + 1) * Math.pow(10, ValidationConstant.EMAIL_VERIFICATION_CODE_LENGTH - 1)));
        RedisUtil.set(generateRedisKey(email), code, ApplicationConstant.EMAIL_VERIFICATION_CODE_EXPIRATION);
        return code;
    }

    public static boolean verifyVerificationCode(String email, String verificationCode) {
        if (verificationCode == null || !verificationCode.equals(RedisUtil.get(generateRedisKey(email)))) {
            return false;
        }
        RedisUtil.del(generateRedisKey(email));
        return true;
    }

    private static String generateRedisKey(String email) {
        return "email#" + email;
    }
}
