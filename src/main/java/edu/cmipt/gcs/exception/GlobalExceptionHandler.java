package edu.cmipt.gcs.exception;

import edu.cmipt.gcs.constant.ErrorMessageConstant;
import edu.cmipt.gcs.pojo.error.ErrorVO;
import edu.cmipt.gcs.util.ErrorMessageUtil;

import io.jsonwebtoken.JwtException;

import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * GlobalExceptionHandler Handles exceptions globally
 *
 * @author: Kaiser
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles MethodArgumentNotValidException
     *
     * @param e MethodArgumentNotValidException
     * @return Map<String, String>
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ErrorVO handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e, HttpServletRequest request) {
        String firstErrorMessage = e.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
        logger.error("Invalid input from {}:\n {}", request.getRemoteAddr(), firstErrorMessage);
        return ErrorMessageUtil.generateError(firstErrorMessage);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IllegalArgumentException.class)
    public ErrorVO handleIllegalArgumentException(
            IllegalArgumentException e, HttpServletRequest request) {
        logger.error("Invalid input from {}:\n {}", request.getRemoteAddr(), e.getMessage());
        return new ErrorVO(e.getMessage());
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(JwtException.class)
    public ErrorVO handleJwtException(JwtException e, HttpServletRequest request) {
        logger.error("Invalid Token from {}", request.getRemoteAddr());
        return new ErrorVO(ErrorMessageConstant.INVALID_TOKEN);
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(AccessDeniedException.class)
    public ErrorVO handleAccessDeniedException(
            AccessDeniedException e, HttpServletRequest request) {
        logger.error("Operation without previlege from {}", request.getRemoteAddr());
        return new ErrorVO(e.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ErrorVO handleHttpMessageNotReadableException(
            HttpMessageNotReadableException e, HttpServletRequest request) {
        logger.error("Invalid input from {}:\n {}", request.getRemoteAddr(), e.getMessage());
        return ErrorMessageUtil.generateError(e.getMessage());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public void handleException(Exception e) {
        logger.error(e.getMessage());
    }
}
