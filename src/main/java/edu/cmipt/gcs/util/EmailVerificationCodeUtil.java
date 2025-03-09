package edu.cmipt.gcs.util;

import edu.cmipt.gcs.constant.ApplicationConstant;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class EmailVerificationCodeUtil {

  private static RedisTemplate<String, Object> redisTemplate;

  public EmailVerificationCodeUtil(RedisTemplate<String, Object> redisTemplate) {
    EmailVerificationCodeUtil.redisTemplate = redisTemplate;
  }

  public static String generateVerificationCode(String email) {
    String code =
        String.valueOf(
            (int)
                ((Math.random() * 9 + 1)
                    * Math.pow(10, ApplicationConstant.EMAIL_VERIFICATION_CODE_LENGTH - 1)));
    redisTemplate
        .opsForValue()
        .set(
            RedisUtil.generateKey(EmailVerificationCodeUtil.class, email),
            code,
            ApplicationConstant.EMAIL_VERIFICATION_CODE_EXPIRATION,
            java.util.concurrent.TimeUnit.MILLISECONDS);
    return code;
  }

  public static boolean verifyVerificationCode(String email, String verificationCode) {
    var cacheKey = RedisUtil.generateKey(EmailVerificationCodeUtil.class, email);
    if (verificationCode == null
        || !verificationCode.equals(redisTemplate.opsForValue().get(cacheKey))) {
      return false;
    }
    redisTemplate.delete(cacheKey);
    return true;
  }
}
