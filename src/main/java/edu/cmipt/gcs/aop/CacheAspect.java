package edu.cmipt.gcs.aop;

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

  private static final Long CACHE_EXPIRE_TIME = 24 * 60 * 60 * 1000L; // 24 hours

  @Autowired RedisTemplate<String, Object> redisTemplate;

  @Around("execution(* edu.cmipt.gcs.service.*ServiceImpl.getById(java.io.Serializable))")
  public Object getByIdAdvice(ProceedingJoinPoint joinPoint) throws Throwable {
    String id = joinPoint.getArgs()[0].toString();
    String cacheKey = generateCacheKey(joinPoint, id);
    Object cacheValue = redisTemplate.opsForValue().get(cacheKey);
    if (cacheValue == null) {
      logger.info("Cache miss, key: {}", cacheKey);
      cacheValue = joinPoint.proceed();
    } else {
      logger.debug("Cache hit, key: {}, value: {}", cacheKey, cacheValue);
    }
    redisTemplate.opsForValue().set(cacheKey, cacheValue, CACHE_EXPIRE_TIME, TimeUnit.MILLISECONDS);
    return cacheValue;
  }

  @Around("execution(* edu.cmipt.gcs.service.*ServiceImpl.getOneBy*(..))")
  public Object getOneAdvice(ProceedingJoinPoint joinPoint) throws Throwable {
    // We first cache the parameters hash code as the cache key, the value is the result PO's id
    String cacheKey = null;
    Object cacheValue = null;
    String argsId = String.valueOf(Arrays.deepHashCode(joinPoint.getArgs()));
    String argsIdKey = generateCacheKey(joinPoint, argsId);
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
        .set(argsIdKey, argsIdValue, CACHE_EXPIRE_TIME, TimeUnit.MILLISECONDS);
    cacheKey = generateCacheKey(joinPoint, argsIdValue.toString());
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
    redisTemplate.opsForValue().set(cacheKey, cacheValue, CACHE_EXPIRE_TIME, TimeUnit.MILLISECONDS);
    return cacheValue;
  }

  /**
   * Generate cache key
   *
   * <p>Warning: there may be hash collision
   *
   * @param joinPoint join point
   * @return cache key
   */
  private String generateCacheKey(ProceedingJoinPoint joinPoint, String id) {
    return String.format("%s#%s", joinPoint.getTarget().getClass().getSimpleName(), id);
  }
}
