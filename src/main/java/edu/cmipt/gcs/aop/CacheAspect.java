package edu.cmipt.gcs.aop;

import edu.cmipt.gcs.constant.ApplicationConstant;
import edu.cmipt.gcs.util.RedisUtil;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * CacheAspect
 *
 * <p>Aspect for cache, this class is used to add cache for the used methods of service layer. You
 * must implement the methods in the service implementation class explicitly.
 *
 * @author Kaiser
 */
@Aspect
@Component
public class CacheAspect {
  private static final Logger logger = LoggerFactory.getLogger(CacheAspect.class);

  @Autowired RedisTemplate<String, Object> redisTemplate;

  @Around("execution(* edu.cmipt.gcs.service.*ServiceImpl.getById(java.io.Serializable))")
  public Object getByIdAdvice(ProceedingJoinPoint joinPoint) throws Throwable {
    String id = joinPoint.getArgs()[0].toString();
    String cacheKey = RedisUtil.generateKey(joinPoint.getTarget(), id);
    Object cacheValue = redisTemplate.opsForValue().get(cacheKey);
    if (cacheValue == null) {
      logger.info("Cache miss, key: {}", cacheKey);
      cacheValue = joinPoint.proceed();
    } else {
      logger.debug("Cache hit, key: {}, value: {}", cacheKey, cacheValue);
    }
    redisTemplate
        .opsForValue()
        .set(
            cacheKey,
            cacheValue,
            ApplicationConstant.SERVICE_CACHE_EXPIRATION,
            TimeUnit.MILLISECONDS);
    return cacheValue;
  }

  @Around("execution(* edu.cmipt.gcs.service.*ServiceImpl.getOneBy*(..))")
  public Object getOneAdvice(ProceedingJoinPoint joinPoint) throws Throwable {
    // We first cache the parameters hash code as the cache key, the value is the result PO's id
    String cacheKey = null;
    Object cacheValue = null;
    String argsId = String.valueOf(Arrays.deepHashCode(joinPoint.getArgs()));
    String argsIdKey = RedisUtil.generateKey(joinPoint.getTarget(), argsId);
    Object argsIdValue = redisTemplate.opsForValue().get(argsIdKey);
    if (argsIdValue == null) {
      logger.info("Cache miss, key: {}", argsIdKey);
      cacheValue = joinPoint.proceed();
      if (cacheValue == null) {
        logger.info("PO not found in getOne method, return null");
        return null;
      }
      argsIdValue = cacheValue.getClass().getMethod("getId").invoke(cacheValue);
    } else {
      logger.debug("Cache hit, key: {}, value: {}", argsIdKey, argsIdValue);
    }
    // Cache the argsId to PO id
    redisTemplate
        .opsForValue()
        .set(
            argsIdKey,
            argsIdValue,
            ApplicationConstant.SERVICE_CACHE_EXPIRATION,
            TimeUnit.MILLISECONDS);
    cacheKey = RedisUtil.generateKey(joinPoint.getTarget(), argsIdValue.toString());
    if (cacheValue == null) {
      cacheValue = redisTemplate.opsForValue().get(cacheKey);
      if (cacheValue == null) {
        logger.info("Cache miss, key: {}", cacheKey);
        cacheValue = joinPoint.proceed();
        if (cacheValue == null) {
          logger.info("PO not found in getOne method, return null");
          return null;
        }
      } else {
        logger.debug("Cache hit, key: {}, value: {}", cacheKey, cacheValue);
      }
    }
    // Cache id of PO to the PO
    redisTemplate
        .opsForValue()
        .set(
            cacheKey,
            cacheValue,
            ApplicationConstant.SERVICE_CACHE_EXPIRATION,
            TimeUnit.MILLISECONDS);
    return cacheValue;
  }

  @Around(
      "execution(* edu.cmipt.gcs.service.*ServiceImpl.updateById(..)) || "
          + "execution(* edu.cmipt.gcs.service.*ServiceImpl.removeById(java.io.Serializable))")
  public Object updateOrRemoveAdvice(ProceedingJoinPoint joinPoint) throws Throwable {
    boolean result = (boolean) joinPoint.proceed();
    if (result) {
      String id;
      if (joinPoint.getSignature().getName().equals("removeById")) {
        id = joinPoint.getArgs()[0].toString();
      } else {
        var po = joinPoint.getArgs()[0];
        id = po.getClass().getMethod("getId").invoke(po).toString();
      }
      String cacheKey = RedisUtil.generateKey(joinPoint.getTarget(), id);
      redisTemplate.delete(cacheKey);
      logger.info("Cache delete, key: {}", cacheKey);
    }
    return result;
  }
}
