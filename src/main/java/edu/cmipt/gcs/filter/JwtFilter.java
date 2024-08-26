package edu.cmipt.gcs.filter;

import java.io.IOException;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import edu.cmipt.gcs.constant.ApiPathConstant;
import edu.cmipt.gcs.util.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
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
@WebFilter
public class JwtFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(JwtFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (!request.getRequestURI().startsWith(ApiPathConstant.ALL_API_PREFIX) ||
                request.getRequestURI().startsWith(ApiPathConstant.AUTHENTICATION_API_PREFIX) ||
                request.getRequestURI().startsWith(ApiPathConstant.DEVELOPMENT_API_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }
        String token = request.getHeader("Access-Token");
        if (!StringUtils.hasText(token)) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Access-Token is missing");
            return;
        }
        try {
            if (!authorize(request, token)) {
                logger.error("Operation without previlege from {}", request.getRemoteAddr());
                response.sendError(HttpStatus.UNAUTHORIZED.value(), "Operation without previlege");
                return;
            }
        } catch (ExpiredJwtException e) {
            logger.error("Expired Access-Token from {}", request.getRemoteAddr());
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Access-Token is expired");
            return;
        } catch (Exception e) {
            logger.error("Invalid Access-Token from {}", request.getRemoteAddr());
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Invalid Access-Token");
            return;
        }
        filterChain.doFilter(request, response);
    }

    private boolean authorize(HttpServletRequest request, String token) throws ExpiredJwtException, Exception {
        try {
            switch (JwtUtil.getTokenType(token)) {
                case ACCESS_TOKEN:
                    // TODO:
                    return true;
                case REFRESH_TOKEN:
                    if (request.getRequestURI().equals(ApiPathConstant.AUTHENTICATION_REFRESH_API_PATH)) {
                        return true;
                    }
            }
        } catch (ExpiredJwtException e) {
            throw e;
        } catch (Exception e) {
            throw e;
        }
        return false;
    }
}
