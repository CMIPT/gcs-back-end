package edu.cmipt.gcs.exception;

import edu.cmipt.gcs.enumeration.ErrorCodeEnum;
import edu.cmipt.gcs.util.MessageSourceUtil;
import lombok.Getter;
import lombok.Setter;

/**
 * GenericException
 *
 * <p>Generic exception class
 *
 * <p>This exception is for the errors should be pass to the client, and the error code should be
 * defined in {@link ErrorCodeEnum}. All bussiness error should throw this exception.
 *
 * @author Kaiser
 */
@Getter
@Setter
public class GenericException extends RuntimeException {
    private ErrorCodeEnum code;

    public GenericException(String message) {
        super(message);
    }

    public GenericException(ErrorCodeEnum code, Object... args) {
        super(MessageSourceUtil.getMessage(code, args));
        this.code = code;
    }
}
