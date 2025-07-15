package edu.cmipt.gcs.aop;

import edu.cmipt.gcs.constant.ApplicationConstant;
import edu.cmipt.gcs.util.RedisUtil;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
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
      logger.debug("Cache missed, key: {}", cacheKey);
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
      logger.debug("Cache missed, key: {}", argsIdKey);
      cacheValue = joinPoint.proceed();
      if (cacheValue == null) {
        logger.debug("PO not found in getOne method, return null");
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
        logger.debug("Cache missed, key: {}", cacheKey);
        cacheValue = joinPoint.proceed();
        if (cacheValue == null) {
          logger.debug("PO not found in getOne method, return null");
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

  @AfterReturning(
      pointcut =
          "execution(* edu.cmipt.gcs.service.*ServiceImpl.updateById(..)) || execution(*"
              + " edu.cmipt.gcs.service.*ServiceImpl.removeBy*(..))",
      returning = "result")
  public void updateOrRemoveAdvice(JoinPoint joinPoint, Object result) throws Throwable {
    if (result instanceof Boolean) {
      String id;
      if (joinPoint.getSignature().getName().equals("removeById")) {
        id = joinPoint.getArgs()[0].toString();
      } else{
        var po = joinPoint.getArgs()[0];
        id = po.getClass().getMethod("getId").invoke(po).toString();
      }
      String cacheKey = RedisUtil.generateKey(joinPoint.getTarget(), id);
      redisTemplate.delete(cacheKey);
      logger.debug("Cache deleted, key: {}", cacheKey);
    } else if (result instanceof List<?> idList && !idList.isEmpty()) {
      for (Object id : idList) {
        if (id == null) continue;
        String cacheKey = RedisUtil.generateKey(joinPoint.getTarget(), id.toString());
        redisTemplate.delete(cacheKey);
        logger.debug("Cache deleted, key: {}", cacheKey);
      }
    } else {
      logger.debug("No IDs to remove from cache, skipping cache deletion.");
    }
  }

  @AfterReturning(
          pointcut = "execution(* edu.cmipt.gcs.service.*ServiceImpl.update*State(..))",
          returning = "result")
  public void updateOneStateAdvice(JoinPoint joinPoint, Object result){
    if((boolean) result)
    {
      String id =joinPoint.getArgs()[0].toString();
      String cacheKey = RedisUtil.generateKey(joinPoint.getTarget(), id);
      redisTemplate.delete(cacheKey);
      logger.debug("Cache deleted, key: {}", cacheKey);
    }
  }
}
