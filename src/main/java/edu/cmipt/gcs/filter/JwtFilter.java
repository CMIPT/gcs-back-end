package edu.cmipt.gcs.filter;

import edu.cmipt.gcs.constant.ApiPathConstant;
import edu.cmipt.gcs.constant.HeaderParameter;
import edu.cmipt.gcs.enumeration.ErrorCodeEnum;
import edu.cmipt.gcs.enumeration.TokenTypeEnum;
import edu.cmipt.gcs.exception.GenericException;
import edu.cmipt.gcs.util.JwtUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Set;

/**
 * JwtFilter
 *
 * <p>Filter to check the validity of the Access-Token
 *
 * @author Kaiser
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class JwtFilter extends OncePerRequestFilter {
    /**
     * CachedBodyHttpServletRequest
     *
     * <p>The {@link}getInputStream() and {@link}getReader() methods of {@link}HttpServletRequest
     * can only be called once. This class is used to cache the body of the request so that it can
     * be read multiple times.
     */
    private class CachedBodyHttpServletRequest extends HttpServletRequestWrapper {
        private class CachedBodyServletInputStream extends ServletInputStream {
            private final InputStream cacheBodyInputStream;

            public CachedBodyServletInputStream(byte[] cacheBody) {
                this.cacheBodyInputStream = new ByteArrayInputStream(cacheBody);
            }

            @Override
            public boolean isFinished() {
                try {
                    return cacheBodyInputStream.available() == 0;
                } catch (IOException e) {
                    return true;
                }
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setReadListener(ReadListener listener) {
                throw new UnsupportedOperationException();
            }

            @Override
            public int read() throws IOException {
                return cacheBodyInputStream.read();
            }
        }

        private final byte[] cacheBody;

        public CachedBodyHttpServletRequest(HttpServletRequest request) throws IOException {
            super(request);
            InputStream requestInputStream = request.getInputStream();
            this.cacheBody = StreamUtils.copyToByteArray(requestInputStream);
        }

        @Override
        public ServletInputStream getInputStream() {
            return new CachedBodyServletInputStream(this.cacheBody);
        }

        @Override
        public BufferedReader getReader() {
            return new BufferedReader(
                    new InputStreamReader(new ByteArrayInputStream(this.cacheBody)));
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(JwtFilter.class);

    private Set<String> ignorePath =
            Set.of(
                    ApiPathConstant.AUTHENTICATION_SIGN_UP_API_PATH,
                    ApiPathConstant.AUTHENTICATION_SIGN_IN_API_PATH,
                    ApiPathConstant.AUTHENTICATION_SIGN_OUT_API_PATH,
                    ApiPathConstant.DEVELOPMENT_GET_API_MAP_API_PATH,
                    ApiPathConstant.DEVELOPMENT_GET_ERROR_MESSAGE_API_PATH,
                    ApiPathConstant.USER_CHECK_EMAIL_VALIDITY_API_PATH,
                    ApiPathConstant.USER_CHECK_USERNAME_VALIDITY_API_PATH);

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // ignore non business api and some special api
        if (!request.getRequestURI().startsWith(ApiPathConstant.ALL_API_PREFIX)
                || ignorePath.contains(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }
        // throw exception if authorization failed
        CachedBodyHttpServletRequest cachedRequest = new CachedBodyHttpServletRequest(request);
        authorize(
                cachedRequest,
                cachedRequest.getHeader(HeaderParameter.ACCESS_TOKEN),
                cachedRequest.getHeader(HeaderParameter.REFRESH_TOKEN));
        filterChain.doFilter(cachedRequest, response);
    }

    private void authorize(HttpServletRequest request, String accessToken, String refreshToken) {
        if (accessToken != null
                && JwtUtil.getTokenType(accessToken) != TokenTypeEnum.ACCESS_TOKEN) {
            throw new GenericException(ErrorCodeEnum.INVALID_TOKEN, accessToken);
        }
        if (refreshToken != null
                && JwtUtil.getTokenType(refreshToken) != TokenTypeEnum.REFRESH_TOKEN) {
            throw new GenericException(ErrorCodeEnum.INVALID_TOKEN, refreshToken);
        }
        switch (request.getMethod()) {
            case "GET":
                if ((accessToken == null
                                && !request.getRequestURI()
                                        .equals(ApiPathConstant.AUTHENTICATION_REFRESH_API_PATH))
                        || (refreshToken == null
                                && request.getRequestURI()
                                        .equals(ApiPathConstant.AUTHENTICATION_REFRESH_API_PATH))) {
                    throw new GenericException(ErrorCodeEnum.TOKEN_NOT_FOUND);
                }
                break;
            case "POST":
                if (accessToken == null) {
                    throw new GenericException(ErrorCodeEnum.TOKEN_NOT_FOUND);
                }
                if (request.getRequestURI().equals(ApiPathConstant.USER_UPDATE_USER_API_PATH)) {
                    // for update user information, both access token and refresh token are needed
                    if (refreshToken == null) {
                        throw new GenericException(ErrorCodeEnum.TOKEN_NOT_FOUND);
                    }
                    // User can not update other user's information
                    String idInToken = JwtUtil.getId(accessToken);
                    String idInBody = getFromRequestBody(request, "id");
                    if (!idInToken.equals(idInBody)) {
                        logger.info("User[{}] tried to update user[{}]", idInToken, idInBody);
                        throw new GenericException(ErrorCodeEnum.ACCESS_DENIED);
                    }
                } else if (request.getRequestURI()
                                .equals(ApiPathConstant.AUTHENTICATION_REFRESH_API_PATH)
                        && refreshToken == null) {
                    // for refresh token, both access token and refresh token are needed
                    throw new GenericException(ErrorCodeEnum.TOKEN_NOT_FOUND);
                } else if (request.getRequestURI().equals(ApiPathConstant.REPOSITORY_CREATE_REPOSITORY_API_PATH)) {
                    // pass
                } else {
                    throw new GenericException(ErrorCodeEnum.ACCESS_DENIED);
                }
                break;
            case "DELETE":
                if (accessToken == null) {
                    throw new GenericException(ErrorCodeEnum.TOKEN_NOT_FOUND);
                }
                if (request.getRequestURI().equals(ApiPathConstant.USER_DELETE_USER_API_PATH)) {
                    // for delete user, both access token and refresh token are needed
                    if (refreshToken == null) {
                        throw new GenericException(ErrorCodeEnum.TOKEN_NOT_FOUND);
                    }
                    // User can not delete other user
                    String idInToken = JwtUtil.getId(accessToken);
                    String idInParam = request.getParameter("id");
                    if (!idInToken.equals(idInParam)) {
                        logger.info("User[{}] tried to delete user[{}]", idInToken, idInParam);
                        throw new GenericException(ErrorCodeEnum.ACCESS_DENIED);
                    }
                } else {
                    throw new GenericException(ErrorCodeEnum.ACCESS_DENIED);
                }
                break;
            default:
                throw new GenericException(ErrorCodeEnum.ACCESS_DENIED);
        }
    }

    private String getFromRequestBody(HttpServletRequest request, String key) {
        try {
            BufferedReader reader = request.getReader();
            StringBuilder builder = new StringBuilder();
            String line = reader.readLine();
            while (line != null) {
                builder.append(line);
                line = reader.readLine();
            }
            reader.close();
            var json = JsonParserFactory.getJsonParser().parseMap(builder.toString());
            return json.get(key).toString();
        } catch (Exception e) {
            // unlikely to happen
            throw new RuntimeException(e);
        }
    }
}
