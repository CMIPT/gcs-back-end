package edu.cmipt.gcs.filter;

import java.io.IOException;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import edu.cmipt.gcs.constant.ApiPathConstant;
import edu.cmipt.gcs.constant.ErrorMessageConstant;
import edu.cmipt.gcs.exception.AccessDeniedException;
import edu.cmipt.gcs.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * JwtFilter
 *
 * Filter to check the validity of the Access-Token
 *
 * @author Kaiser
 *
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class JwtFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (!request.getRequestURI().startsWith(ApiPathConstant.ALL_API_PREFIX) ||
                (request.getRequestURI().startsWith(ApiPathConstant.AUTHENTICATION_API_PREFIX) &&
                 !request.getRequestURI().startsWith(ApiPathConstant.AUTHENTICATION_REFRESH_API_PATH)) ||
                request.getRequestURI().startsWith(ApiPathConstant.DEVELOPMENT_API_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }
        authorize(request, request.getHeader("Token"));
        filterChain.doFilter(request, response);
    }

    private void authorize(HttpServletRequest request, String token) {
        switch (JwtUtil.getTokenType(token)) {
            case ACCESS_TOKEN:
                // TODO:
                if (!request.getRequestURI().equals(ApiPathConstant.AUTHENTICATION_REFRESH_API_PATH)) {
                    return;
                }
                break;
            case REFRESH_TOKEN:
                if (request.getRequestURI().equals(ApiPathConstant.AUTHENTICATION_REFRESH_API_PATH)) {
                    return;
                }
                break;
        }
        throw new AccessDeniedException(ErrorMessageConstant.ACCESS_DENIED);
    }
}
