package edu.cmipt.gcs.exception;

import edu.cmipt.gcs.enumeration.ErrorCodeEnum;
import edu.cmipt.gcs.pojo.error.ErrorVO;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.json.JsonParseException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
     * <p>This method is used to handle the MethodArgumentNotValidException, which is thrown when
     * the validation of the request body fails.
     *
     * @param e MethodArgumentNotValidException
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorVO> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e, HttpServletRequest request) {
        return handleValidationException(e.getFieldError().getDefaultMessage(), request);
    }

    /**
     * Handles ConstraintViolationException
     *
     * <p>This method is used to handle the ConstraintViolationException, which is thrown when the
     * validation of the path variables or request parameters fails.
     *
     * @param e ConstraintViolationException
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorVO> handleConstraintViolationException(
            ConstraintViolationException e, HttpServletRequest request) {
        return handleValidationException(
                e.getConstraintViolations().iterator().next().getMessage(), request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorVO> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException e, HttpServletRequest request) {
        return handleGenericException(
                new GenericException(ErrorCodeEnum.MESSAGE_CONVERSION_ERROR), request);
    }

    @ExceptionHandler(GenericException.class)
    public ResponseEntity<ErrorVO> handleGenericException(
            GenericException e, HttpServletRequest request) {
        logger.error("Error caused by {}:\n {}", request.getRemoteAddr(), e.getMessage());
        switch (e.getCode()) {
            case INVALID_TOKEN:
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ErrorVO(e.getCode(), e.getMessage()));
            case ACCESS_DENIED:
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ErrorVO(e.getCode(), e.getMessage()));
            case USER_NOT_FOUND:
            case SSH_KEY_NOT_FOUND:
            case REPOSITORY_NOT_FOUND:
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorVO(e.getCode(), e.getMessage()));
            case OPERATION_NOT_IMPLEMENTED:
                return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                        .body(new ErrorVO(e.getCode(), e.getMessage()));
            case SERVER_ERROR:
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new ErrorVO(e.getCode(), e.getMessage()));
            default:
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorVO(e.getCode(), e.getMessage()));
        }
    }

    @ExceptionHandler(JsonParseException.class)
    public ResponseEntity<ErrorVO> handleJsonParseException(
            JsonParseException e, HttpServletRequest request) {
        GenericException exception = new GenericException(e.getMessage());
        exception.setCode(ErrorCodeEnum.MESSAGE_CONVERSION_ERROR);
        return handleGenericException(exception, request);
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorVO> handleException(Exception e, HttpServletRequest request) {
        logger.error(e.getMessage());
        // TODO: use logger to log the exception
        e.printStackTrace();
        return handleGenericException(new GenericException(ErrorCodeEnum.SERVER_ERROR), request);
    }

    private ResponseEntity<ErrorVO> handleValidationException(
            String codeAndMessage, HttpServletRequest request) {
        int firstSpaceIndex = codeAndMessage.indexOf(" ");
        // There must be a space and not at the end of the message
        assert firstSpaceIndex != -1;
        assert firstSpaceIndex != codeAndMessage.length() - 1;
        var exception = new GenericException(codeAndMessage.substring(firstSpaceIndex + 1));
        exception.setCode(ErrorCodeEnum.valueOf(codeAndMessage.substring(0, firstSpaceIndex)));
        return handleGenericException(exception, request);
    }
}
