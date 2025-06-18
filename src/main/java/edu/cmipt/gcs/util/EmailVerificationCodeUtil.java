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
    long expireTime = redisTemplate.getExpire(codeKey, TimeUnit.MILLISECONDS);
    assert expireTime !=-1 : "Redis key for email verification code should not be permanent";
    if (expireTime >0) {
      long coolDownTime = expireTime + ApplicationConstant.EMAIL_VERIFICATION_CODE_COOL_DOWN_TIME  -  ApplicationConstant.EMAIL_VERIFICATION_CODE_EXPIRATION ;
      if( coolDownTime > 0) {
        throw new GenericException(ErrorCodeEnum.EMAIL_VERIFICATION_CODE_COOL_DOWN, coolDownTime / 1000);
      }
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
