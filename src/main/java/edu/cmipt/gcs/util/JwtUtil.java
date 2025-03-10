package edu.cmipt.gcs.util;

import edu.cmipt.gcs.constant.ApplicationConstant;
import edu.cmipt.gcs.constant.HeaderParameter;
import edu.cmipt.gcs.enumeration.ErrorCodeEnum;
import edu.cmipt.gcs.enumeration.TokenTypeEnum;
import edu.cmipt.gcs.exception.GenericException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

/**
 * JwtUtil
 *
 * @author Kaiser
 */
@Component
public class JwtUtil {
  private static final String TOKEN_TYPE_CLAIM = "tokenType";
  private static final String ID_CLAIM = "id";
  private static SecretKey SECRET_KEY;

  @Value("${jwt.secret}")
  private String secret;

  private static RedisTemplate<String, Object> redisTemplate;

  public JwtUtil(RedisTemplate<String, Object> redisTemplate) {
    JwtUtil.redisTemplate = redisTemplate;
  }

  @PostConstruct
  public void init() {
    if (secret == null || secret.isEmpty()) {
      SECRET_KEY = Jwts.SIG.HS256.key().build();
      return;
    }
    byte[] secretBytes = Decoders.BASE64.decode(secret);
    SECRET_KEY = Keys.hmacShaKeyFor(secretBytes);
  }

  /**
   * Generate a token
   *
   * @param id The id of the user
   * @param tokenType The type of the token
   * @return The generated access token
   */
  public static String generateToken(long id, TokenTypeEnum tokenType) {
    String token =
        Jwts.builder()
            .issuedAt(new Date())
            .claim(ID_CLAIM, id)
            .claim(TOKEN_TYPE_CLAIM, tokenType.name())
            .signWith(SECRET_KEY)
            .compact();
    setTokenInRedis(token, tokenType);
    return token;
  }

  public static String generateToken(String id, TokenTypeEnum tokenType) {
    return generateToken(Long.valueOf(id), tokenType);
  }

  public static String getId(String token) {
    if (!redisTemplate.hasKey(generateRedisKey(token))) {
      throw new GenericException(ErrorCodeEnum.INVALID_TOKEN, token);
    }
    try {
      return String.valueOf(
          Jwts.parser()
              .verifyWith(SECRET_KEY)
              .build()
              .parseSignedClaims(token)
              .getPayload()
              .get(ID_CLAIM, Long.class));
    } catch (Exception e) {
      throw new GenericException(ErrorCodeEnum.INVALID_TOKEN, token);
    }
  }

  public static TokenTypeEnum getTokenType(String token) {
    if (!redisTemplate.hasKey(generateRedisKey(token))) {
      throw new GenericException(ErrorCodeEnum.INVALID_TOKEN, token);
    }
    try {
      return TokenTypeEnum.valueOf(
          Jwts.parser()
              .verifyWith(SECRET_KEY)
              .build()
              .parseSignedClaims(token)
              .getPayload()
              .get(TOKEN_TYPE_CLAIM, String.class));
    } catch (Exception e) {
      throw new GenericException(ErrorCodeEnum.INVALID_TOKEN, token);
    }
  }

  public static HttpHeaders generateHeaders(String id) {
    return generateHeaders(id, true);
  }

  public static HttpHeaders generateHeaders(String id, boolean addRefreshToken) {
    HttpHeaders headers = new HttpHeaders();
    headers.add(HeaderParameter.ACCESS_TOKEN, generateToken(id, TokenTypeEnum.ACCESS_TOKEN));
    if (addRefreshToken) {
      headers.add(HeaderParameter.REFRESH_TOKEN, generateToken(id, TokenTypeEnum.REFRESH_TOKEN));
    }
    return headers;
  }

  /**
   * Add tokens of a user to blacklist
   *
   * @author Kaiser
   * @param tokens
   */
  public static void blacklistToken(Long id) {
    redisTemplate.delete(generateRedisKey(id, TokenTypeEnum.ACCESS_TOKEN));
    redisTemplate.delete(generateRedisKey(id, TokenTypeEnum.REFRESH_TOKEN));
  }

  public static void blacklistToken(String id) {
    blacklistToken(Long.valueOf(id));
  }

  private static String generateRedisKey(Long id, TokenTypeEnum tokenType) {
    return RedisUtil.generateKey(JwtUtil.class, id + tokenType.name());
  }

  public static void refreshToken(String token) {
    setTokenInRedis(token, getTokenType(token));
  }

  private static void setTokenInRedis(String token, TokenTypeEnum tokenType) {
    redisTemplate
        .opsForValue()
        .set(
            generateRedisKey(token),
            token,
            (tokenType == TokenTypeEnum.ACCESS_TOKEN
                ? ApplicationConstant.ACCESS_TOKEN_EXPIRATION
                : ApplicationConstant.REFRESH_TOKEN_EXPIRATION),
            TimeUnit.MILLISECONDS);
  }

  private static String generateRedisKey(String token) {
    try {
      var payload =
          Jwts.parser().verifyWith(SECRET_KEY).build().parseSignedClaims(token).getPayload();
      Long id = payload.get(ID_CLAIM, Long.class);
      String tokenType = payload.get(TOKEN_TYPE_CLAIM, String.class);
      return generateRedisKey(id, TokenTypeEnum.valueOf(tokenType));
    } catch (Exception e) {
      throw new GenericException(ErrorCodeEnum.INVALID_TOKEN, token);
    }
  }
}
