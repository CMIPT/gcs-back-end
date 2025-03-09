package edu.cmipt.gcs.aop;

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
      logger.info("Cache hit, key: {}, value: {}", cacheKey, cacheValue);
    }
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
