package edu.cmipt.gcs.exception;

import edu.cmipt.gcs.enumeration.ErrorCodeEnum;
import edu.cmipt.gcs.pojo.error.ErrorVO;
import edu.cmipt.gcs.util.MessageSourceUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.json.JsonParseException;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
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
     * Handle MethodArgumentNotValidException
     *
     * <p>This method is used to handle the MethodArgumentNotValidException, which is thrown when
     * the validation of the request body fails.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorVO> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e, HttpServletRequest request) {
        var fieldError = e.getBindingResult().getFieldError();
        return handleValidationException(
                MessageSourceUtil.getMessage(fieldError.getCodes()[0], fieldError.getArguments()),
                request);
    }

    /**
     * Handle ConstraintViolationException
     *
     * <p>This method is used to handle the ConstraintViolationException, which is thrown when the
     * validation of the path variables or request parameters fails.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorVO> handleConstraintViolationException(
            ConstraintViolationException e, HttpServletRequest request) {
        return handleValidationException(
                e.getConstraintViolations().iterator().next().getMessage(), request);
    }

    /**
     * Handle HttpMessageNotReadableException
     * 
     * <p>This method is used to handle the HttpMessageNotReadableException, which is thrown when
     * the request body is not readable. For example, when the request body is not a valid JSON.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorVO> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException e, HttpServletRequest request) {
        logger.error("Http message not readable: ", e);
        return handleGenericException(
                new GenericException(ErrorCodeEnum.MESSAGE_CONVERSION_ERROR), request);
    }

    /**
     * Handle ConversionFailedException
     *
     * <p>This method is used to handle the ConversionFailedException, which is thrown when the
     * conversion of the request fails, such as converting a string to an integer or a string to an
     * enum.
     */
    @ExceptionHandler(ConversionFailedException.class)
    public ResponseEntity<ErrorVO> handleConversionFailedException(
            ConversionFailedException e, HttpServletRequest request) {
        logger.error("Conversion failed: ", e);
        return handleGenericException(
                new GenericException(ErrorCodeEnum.MESSAGE_CONVERSION_ERROR), request);
    }

    /**
     * Handle GenericException
     *
     * <p>This method is used to handle the GenericException, which is thrown when an error occurs
     * during the execution of the program.
     */
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

    /**
     * Handle MissingServletRequestParameterException
     *
     * <p>This method is used to handle the MissingServletRequestParameterException, which is thrown
     * when the required request parameter is missing.
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorVO> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException e, HttpServletRequest request) {
        logger.error("Missing request parameter: {}", e.getParameterName());
        return handleValidationException(e.getParameterName(), request);
    }

    /**
     * Handle JsonParseException
     *
     * <p>This method is used to handle the JsonParseException, which is thrown when the JSON
     * parsing fails.
     */
    @ExceptionHandler(JsonParseException.class)
    public ResponseEntity<ErrorVO> handleJsonParseException(
            JsonParseException e, HttpServletRequest request) {
        logger.error("Json parse exception: ", e);
        return handleGenericException(new GenericException(ErrorCodeEnum.MESSAGE_CONVERSION_ERROR), request);
    }

    /**
     * Handle Exception
     *
     * <p>This method is used to handle the Exception, which is thrown when an unexpected error
     * occurs.
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorVO> handleException(Exception e, HttpServletRequest request) {
        logger.error("Internal server error: ", e);
        return handleGenericException(new GenericException(ErrorCodeEnum.SERVER_ERROR), request);
    }

    private ResponseEntity<ErrorVO> handleValidationException(
            String message, HttpServletRequest request) {
        return handleGenericException(
                new GenericException(ErrorCodeEnum.VALIDATION_ERROR, message), request);
    }
}
