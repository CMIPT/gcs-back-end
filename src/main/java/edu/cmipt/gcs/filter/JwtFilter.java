package edu.cmipt.gcs.filter;

import edu.cmipt.gcs.constant.ApiPathConstant;
import edu.cmipt.gcs.constant.HeaderParameter;
import edu.cmipt.gcs.enumeration.ErrorCodeEnum;
import edu.cmipt.gcs.exception.GenericException;
import edu.cmipt.gcs.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.boot.json.JsonParserFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.BufferedReader;
import java.io.IOException;
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
    private Set<String> ignorePath =
            Set.of(
                    ApiPathConstant.AUTHENTICATION_SIGN_UP_API_PATH,
                    ApiPathConstant.AUTHENTICATION_SIGN_IN_API_PATH,
                    ApiPathConstant.AUTHENTICATION_SIGN_OUT_API_PATH,
                    ApiPathConstant.DEVELOPMENT_GET_API_MAP_API_PATH,
                    ApiPathConstant.DEVELOPMENT_GET_ERROR_MESSAGE_API_PATH);

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
        authorize(request, request.getHeader(HeaderParameter.TOKEN));
        filterChain.doFilter(request, response);
    }

    private void authorize(HttpServletRequest request, String token) {
        switch (JwtUtil.getTokenType(token)) {
            case ACCESS_TOKEN:
                // ACCESS_TOKEN can not be used for refresh
                if (request.getRequestURI()
                        .equals(ApiPathConstant.AUTHENTICATION_REFRESH_API_PATH)) {
                    throw new GenericException(ErrorCodeEnum.INVALID_TOKEN, token);
                }
                String idInToken = JwtUtil.getID(token);
                switch (request.getMethod()) {
                    case "GET":
                        break;
                    case "POST":
                        // User can not update other user's information
                        if (request.getRequestURI().startsWith(ApiPathConstant.USER_API_PREFIX)
                                && !idInToken.equals(getFromRequestBody(request, "id"))) {
                            throw new GenericException(ErrorCodeEnum.INVALID_TOKEN, token);
                        }
                        break;
                    default:
                        throw new GenericException(ErrorCodeEnum.INVALID_TOKEN, token);
                }
                break;
            case REFRESH_TOKEN:
                // REFRESH_TOKEN can only be used for refresh
                if (!request.getRequestURI()
                        .equals(ApiPathConstant.AUTHENTICATION_REFRESH_API_PATH)) {
                    throw new GenericException(ErrorCodeEnum.INVALID_TOKEN, token);
                }
                break;
            default:
                throw new GenericException(ErrorCodeEnum.INVALID_TOKEN, token);
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
