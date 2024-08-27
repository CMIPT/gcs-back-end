package edu.cmipt.gcs.util;

import java.util.Date;
import java.util.List;

import javax.crypto.SecretKey;

import io.jsonwebtoken.Jwts;
import edu.cmipt.gcs.constant.ApplicationConstant;
import edu.cmipt.gcs.enumeration.TokenTypeEnum;

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
     * @param id       The id of the user
     * @param tokenType The type of the token
     * @return The generated access token
     */
    public static String generateToken(long id, TokenTypeEnum tokenType) {
        return Jwts.builder().issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis()
                        + (tokenType == TokenTypeEnum.ACCESS_TOKEN ? ApplicationConstant.ACCESS_TOKEN_EXPIRATION
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
        return String.valueOf(Jwts.parser().verifyWith(SECRET_KEY).build().parseSignedClaims(token).getPayload()
                .get(ID_CLAIM, Long.class));
    }

    public static TokenTypeEnum getTokenType(String token) {
        return TokenTypeEnum.valueOf(Jwts.parser().verifyWith(SECRET_KEY).build().parseSignedClaims(token)
                .getPayload().get(TOKEN_TYPE_CLAIM, String.class));
    }

    /**
     * Add token to blacklist
     * 
     * @author Kaiser
     * @param token
     */
    public static void blacklistToken(String token) {
        // TODO: add token to blacklist, we will consider this later
    }

    public static void blacklistToken(List<String> tokenList) {
        for (String token : tokenList) {
            blacklistToken(token);
        }
    }
}
