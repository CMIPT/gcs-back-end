package edu.cmipt.gcs.util;

import edu.cmipt.gcs.constant.ApplicationConstant;
import edu.cmipt.gcs.constant.HeaderParameter;
import edu.cmipt.gcs.enumeration.ErrorCodeEnum;
import edu.cmipt.gcs.enumeration.TokenTypeEnum;
import edu.cmipt.gcs.exception.GenericException;

import io.jsonwebtoken.Jwts;

import java.util.Date;
import java.util.List;

import javax.crypto.SecretKey;

import org.springframework.http.HttpHeaders;

/**
 * JwtUtil
 *
 * @author Kaiser
 */
public class JwtUtil {
    private static final String TOKEN_TYPE_CLAIM = "tokenType";
    private static final String ID_CLAIM = "id";
    // TODO: every restart of the server will invalidate all tokens, may need to
    // change this
    private static final SecretKey SECRET_KEY = Jwts.SIG.HS256.key().build();

    /**
     * Generate a token
     *
     * @param id The id of the user
     * @param tokenType The type of the token
     * @return The generated access token
     */
    public static String generateToken(long id, TokenTypeEnum tokenType) {
        return Jwts.builder()
                .issuedAt(new Date())
                .expiration(
                        new Date(
                                System.currentTimeMillis()
                                        + (tokenType == TokenTypeEnum.ACCESS_TOKEN
                                                ? ApplicationConstant.ACCESS_TOKEN_EXPIRATION
                                                : ApplicationConstant.REFRESH_TOKEN_EXPIRATION)))
                .claim(ID_CLAIM, id)
                .claim(TOKEN_TYPE_CLAIM, tokenType.name())
                .signWith(SECRET_KEY)
                .compact();
    }

    public static String generateToken(String id, TokenTypeEnum tokenType) {
        return generateToken(Long.valueOf(id), tokenType);
    }

    public static String getID(String token) {
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
     * Add token to blacklist
     *
     * @author Kaiser
     * @param tokens
     */
    public static void blacklistToken(String... tokens) {
        // TODO: add token to blacklist, we will consider this later
    }

    public static void blacklistToken(List<String> tokenList) {
        blacklistToken(tokenList.toArray(new String[0]));
    }
}
