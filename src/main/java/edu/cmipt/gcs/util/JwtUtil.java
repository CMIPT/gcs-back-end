package edu.cmipt.gcs.util;

import java.util.Date;
import java.util.List;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;

import edu.cmipt.gcs.enumeration.TokenTypeEnum;

/**
 * JwtUtil
 *
 * @author Kaiser
 */
public class JwtUtil {
    private static final String TOKEN_TYPE_CLAIM = "tokenType";
    private static final String ID_CLAIM = "id";

    /**
     * Generate a token
     *
     * @param claims
     * @return The generated access token
     */
    public static String generateToken(long id, long expiration, TokenTypeEnum tokenType) {
        return Jwts.builder().issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .claim(ID_CLAIM, id)
                .claim(TOKEN_TYPE_CLAIM, tokenType)
                .signWith(Jwts.SIG.HS256.key().build())
                .compact();
    }

    public static String getID(String token) throws ExpiredJwtException, Exception {
        String id = null;
        try {
            id = Jwts.parser().verifyWith(Jwts.SIG.HS256.key().build()).build().parseSignedClaims(token).getPayload().get(ID_CLAIM, String.class);
        } catch (ExpiredJwtException e) {
            throw e;
        } catch (Exception e) {
            throw e;
        }
        return id;
    }

    public static TokenTypeEnum getTokenType(String token) throws ExpiredJwtException, Exception{
        TokenTypeEnum tokenType = null;
        try {
            tokenType = Jwts.parser().verifyWith(Jwts.SIG.HS256.key().build()).build().parseSignedClaims(token).getPayload().get(TOKEN_TYPE_CLAIM, TokenTypeEnum.class);
        } catch (ExpiredJwtException e) {
            throw e;
        } catch (Exception e) {
            throw e;
        }
        return tokenType;
    }

    /**
     * Add token to blacklist
     * 
     * @author Kaiser
     * @param token
     */
    public static void blacklistToken(String token) {
        // TODO: add token to blacklist
    }

    public static void blacklistToken(List<String> tokenList) {
        for (String token : tokenList) {
            blacklistToken(token);
        }
    }
}
