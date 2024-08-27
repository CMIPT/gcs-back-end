package edu.cmipt.gcs.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
/**
 * ExceptionHandlerFiter
 *
 * <p>Filter to handle exceptions globally
 *
 * <p>We find that the exception thrown by filters will not handled by the handler annotated with
 * {@link ControllerAdvice @ControllerAdvice} or {@link RestControllerAdvice @RestControllerAdvice},
 * this is because the exception thrown by filters is not thrown by the controller method. In order
 * to solve this, we can use {@link HandlerExceptionResolver} to handle the exception thrown by
 * filters, which will be resolved by the handler annotated with {@link
 * ControllerAdvice @ControllerAdvice} or {@link RestControllerAdvice @RestControllerAdvice}.
 *
 * @author Kaiser
 */
public class ExceptionHandlerFiter extends OncePerRequestFilter {
    @Autowired
    @Qualifier("handlerExceptionResolver")
    private HandlerExceptionResolver resolver;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            resolver.resolveException(request, response, null, e);
        }
    }
}
