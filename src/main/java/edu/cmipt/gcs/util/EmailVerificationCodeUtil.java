package edu.cmipt.gcs.util;

import edu.cmipt.gcs.constant.ApplicationConstant;
import edu.cmipt.gcs.enumeration.ErrorCodeEnum;
import edu.cmipt.gcs.exception.GenericException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class EmailVerificationCodeUtil {

  private static RedisTemplate<String, Object> redisTemplate;

  public EmailVerificationCodeUtil(RedisTemplate<String, Object> redisTemplate) {
    EmailVerificationCodeUtil.redisTemplate = redisTemplate;
  }

  public static String generateVerificationCode(String email) {
    var codeKey = RedisUtil.generateKey(EmailVerificationCodeUtil.class, email);
    var cooldownKey = codeKey + "_cooldown";
    Long remainingCooldown = redisTemplate.getExpire(cooldownKey, TimeUnit.MILLISECONDS);
    if (remainingCooldown != null && remainingCooldown > 0) {
      throw new GenericException(ErrorCodeEnum.EMAIL_VERIFICATION_CODE_COOL_DOWN,remainingCooldown/ 1000);
    }
    String code =
        String.valueOf(
            (int)
                ((Math.random() * 9 + 1)
                    * Math.pow(10, ApplicationConstant.EMAIL_VERIFICATION_CODE_LENGTH - 1)));
    redisTemplate
        .opsForValue()
        .set(
            codeKey,
            code,
            ApplicationConstant.EMAIL_VERIFICATION_CODE_EXPIRATION,
            TimeUnit.MILLISECONDS);
    redisTemplate.opsForValue().set(
            cooldownKey,
            "LOCK",
            ApplicationConstant.EMAIL_VERIFICATION_CODE_COOL_DOWN_EXPIRATION,  // 1分钟冷却
            TimeUnit.MILLISECONDS);
    return code;
  }

  public static boolean verifyVerificationCode(String email, String verificationCode) {
    var cacheKey = RedisUtil.generateKey(EmailVerificationCodeUtil.class, email);
    var cooldownKey = cacheKey + "_cooldown";
    if (verificationCode == null
        || !verificationCode.equals(redisTemplate.opsForValue().get(cacheKey))) {
      return false;
    }
    redisTemplate.delete(cacheKey);
    redisTemplate.delete(cooldownKey);
    return true;
  }
}
