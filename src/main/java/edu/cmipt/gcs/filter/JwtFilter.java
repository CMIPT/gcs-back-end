package edu.cmipt.gcs.filter;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.cmipt.gcs.constant.ApiPathConstant;
import edu.cmipt.gcs.constant.HeaderParameter;
import edu.cmipt.gcs.enumeration.ErrorCodeEnum;
import edu.cmipt.gcs.enumeration.TokenTypeEnum;
import edu.cmipt.gcs.exception.GenericException;
import edu.cmipt.gcs.pojo.user.UserUpdateDTO;
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
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.Map;
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
    @Autowired ObjectMapper objectMapper;

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

    // Paths that do not need token
    private Map<String, Set<String>> ignorePath =
            Map.of(
                    "GET",
                            Set.of(
                                    ApiPathConstant.DEVELOPMENT_GET_API_MAP_API_PATH,
                                    ApiPathConstant.DEVELOPMENT_GET_ERROR_MESSAGE_API_PATH,
                                    ApiPathConstant.DEVELOPMENT_GET_VO_AS_TS_API_PATH,
                                    ApiPathConstant.USER_CHECK_USERNAME_VALIDITY_API_PATH,
                                    ApiPathConstant.USER_CHECK_USER_PASSWORD_VALIDITY_API_PATH,
                                    ApiPathConstant.USER_CHECK_EMAIL_VALIDITY_API_PATH,
                                    ApiPathConstant
                                            .REPOSITORY_CHECK_REPOSITORY_NAME_VALIDITY_API_PATH,
                                    ApiPathConstant
                                            .AUTHENTICATION_SEND_EMAIL_VERIFICATION_CODE_API_PATH),
                    "POST",
                            Set.of(
                                    ApiPathConstant.AUTHENTICATION_SIGN_IN_API_PATH,
                                    ApiPathConstant.USER_CREATE_USER_API_PATH,
                                    ApiPathConstant
                                            .USER_UPDATE_USER_PASSWORD_WITH_OLD_PASSWORD_API_PATH,
                                    ApiPathConstant
                                            .USER_UPDATE_USER_PASSWORD_WITH_EMAIL_VERIFICATION_CODE_API_PATH),
                    "DELETE", Set.of(ApiPathConstant.AUTHENTICATION_SIGN_OUT_API_PATH));

    // Paths that do not need authorization in filter
    private Map<String, Set<String>> passPath =
            Map.of(
                    "GET",
                            Set.of(
                                    ApiPathConstant.AUTHENTICATION_REFRESH_API_PATH,
                                    ApiPathConstant
                                            .SSH_KEY_CHECK_SSH_KEY_PUBLIC_KEY_VALIDITY_API_PATH,
                                    ApiPathConstant.SSH_KEY_CHECK_SSH_KEY_NAME_VALIDITY_API_PATH,
                                    ApiPathConstant.REPOSITORY_PAGE_REPOSITORY_API_PATH,
                                    ApiPathConstant.USER_GET_USER_API_PATH,
                                    ApiPathConstant.REPOSITORY_GET_REPOSITORY_API_PATH,
                                    ApiPathConstant.REPOSITORY_PAGE_COLLABORATOR_API_PATH),
                    "POST",
                            Set.of(
                                    ApiPathConstant.REPOSITORY_CREATE_REPOSITORY_API_PATH,
                                    ApiPathConstant.REPOSITORY_UPDATE_REPOSITORY_API_PATH,
                                    ApiPathConstant.REPOSITORY_ADD_COLLABORATOR_API_PATH,
                                    ApiPathConstant.SSH_KEY_UPLOAD_SSH_KEY_API_PATH,
                                    ApiPathConstant.SSH_KEY_UPDATE_SSH_KEY_API_PATH),
                    "DELETE",
                            Set.of(
                                    ApiPathConstant.REPOSITORY_DELETE_REPOSITORY_API_PATH,
                                    ApiPathConstant.REPOSITORY_REMOVE_COLLABORATION_API_PATH,
                                    ApiPathConstant.SSH_KEY_DELETE_SSH_KEY_API_PATH));

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // ignore non business api and some special api
        var ignoreSet = ignorePath.get(request.getMethod());
        if (!request.getRequestURI().startsWith(ApiPathConstant.ALL_API_PREFIX)
                || ignoreSet != null && ignoreSet.contains(request.getRequestURI())) {
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
        if (accessToken != null && JwtUtil.getTokenType(accessToken) != TokenTypeEnum.ACCESS_TOKEN
                || refreshToken != null
                        && JwtUtil.getTokenType(refreshToken) != TokenTypeEnum.REFRESH_TOKEN) {
            throw new GenericException(ErrorCodeEnum.INVALID_TOKEN, accessToken);
        }
        var requestURI = request.getRequestURI();
        var requestMethod = request.getMethod();
        var passSet = passPath.get(requestMethod);
        if (passSet != null && passSet.contains(requestURI)) {
            if (requestURI.equals(ApiPathConstant.AUTHENTICATION_REFRESH_API_PATH)) {
                if (refreshToken == null) {
                    throw new GenericException(ErrorCodeEnum.TOKEN_NOT_FOUND);
                }
                JwtUtil.refreshToken(refreshToken);
            } else {
                if (accessToken == null) {
                    throw new GenericException(ErrorCodeEnum.TOKEN_NOT_FOUND);
                }
                JwtUtil.refreshToken(accessToken);
            }
            return;
        }
        if (accessToken == null) {
            throw new GenericException(ErrorCodeEnum.TOKEN_NOT_FOUND);
        }
        switch (requestMethod) {
            case "GET":
                if (requestURI.equals(ApiPathConstant.SSH_KEY_PAGE_SSH_KEY_API_PATH)) {
                    String idInToken = JwtUtil.getId(accessToken);
                    String idInParam = request.getParameter("id");
                    if (!idInToken.equals(idInParam)) {
                        logger.info(
                                "User[{}] tried to get SSH key of user[{}]", idInToken, idInParam);
                        throw new GenericException(ErrorCodeEnum.ACCESS_DENIED);
                    }
                } else {
                    throw new GenericException(ErrorCodeEnum.ACCESS_DENIED);
                }
                break;
            case "POST":
                if (requestURI.equals(ApiPathConstant.USER_UPDATE_USER_API_PATH)) {
                    // User can not update other user's information
                    String idInToken = JwtUtil.getId(accessToken);
                    String idInBody = getIdFromRequestBody(request);
                    if (!idInToken.equals(idInBody)) {
                        logger.info("User[{}] tried to update user[{}]", idInToken, idInBody);
                        throw new GenericException(ErrorCodeEnum.ACCESS_DENIED);
                    }
                } else {
                    throw new GenericException(ErrorCodeEnum.ACCESS_DENIED);
                }
                break;
            case "DELETE":
                if (requestURI.equals(ApiPathConstant.USER_DELETE_USER_API_PATH)) {
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
        JwtUtil.refreshToken(accessToken);
    }

    private String getIdFromRequestBody(HttpServletRequest request) {
        try {
            BufferedReader reader = request.getReader();
            StringBuilder builder = new StringBuilder();
            String line = reader.readLine();
            while (line != null) {
                builder.append(line);
                line = reader.readLine();
            }
            reader.close();
            return objectMapper.readValue(builder.toString(), UserUpdateDTO.class).id();
        } catch (Exception e) {
            // unlikely to happen
            throw new RuntimeException(e);
        }
    }
}
