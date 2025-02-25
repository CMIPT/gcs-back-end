package edu.cmipt.gcs.enumeration;

public enum ErrorCodeEnum {
    // This should be ignored, this is to make the ordinal of the enum start from 1
    ZERO_PLACEHOLDER,

    VALIDATION_ERROR("VALIDATION_ERROR"),

    USERNAME_RESERVED("USERNAME_RESERVED"),
    USERNAME_ALREADY_EXISTS("USERNAME_ALREADY_EXISTS"),
    EMAIL_ALREADY_EXISTS("EMAIL_ALREADY_EXISTS"),
    WRONG_SIGN_IN_INFORMATION("WRONG_SIGN_IN_INFORMATION"),

    INVALID_TOKEN("INVALID_TOKEN"),
    ACCESS_DENIED("ACCESS_DENIED"),
    TOKEN_NOT_FOUND("TOKEN_NOT_FOUND"),

    MESSAGE_CONVERSION_ERROR("MESSAGE_CONVERSION_ERROR"),

    USER_NOT_FOUND("USER_NOT_FOUND"),

    USER_CREATE_FAILED("USER_CREATE_FAILED"),
    USER_UPDATE_FAILED("USER_UPDATE_FAILED"),
    USER_DELETE_FAILED("USER_DELETE_FAILED"),
    WRONG_UPDATE_PASSWORD_INFORMATION("WRONG_UPDATE_PASSWORD_INFORMATION"),

    REPOSITORY_NOT_FOUND("REPOSITORY_NOT_FOUND"),
    REPOSITORY_ALREADY_EXISTS("REPOSITORY_ALREADY_EXISTS"),
    REPOSITORY_CREATE_FAILED("REPOSITORY_CREATE_FAILED"),
    REPOSITORY_UPDATE_FAILED("REPOSITORY_UPDATE_FAILED"),
    REPOSITORY_DELETE_FAILED("REPOSITORY_DELETE_FAILED"),

    COLLABORATION_ADD_FAILED("COLLABORATION_ADD_FAILED"),
    COLLABORATION_REMOVE_FAILED("COLLABORATION_REMOVE_FAILED"),
    COLLABORATION_ALREADY_EXISTS("COLLABORATION_ALREADY_EXISTS"),
    COLLABORATION_NOT_FOUND("COLLABORATION_NOT_FOUND"),

    SSH_KEY_UPLOAD_FAILED("SSH_KEY_UPLOAD_FAILED"),
    SSH_KEY_UPDATE_FAILED("SSH_KEY_UPDATE_FAILED"),
    SSH_KEY_DELETE_FAILED("SSH_KEY_DELETE_FAILED"),
    SSH_KEY_NOT_FOUND("SSH_KEY_NOT_FOUND"),
    SSH_KEY_PUBLIC_KEY_INVALID("SSH_KEY_PUBLIC_KEY_INVALID"),
    SSH_KEY_PUBLIC_KEY_ALREADY_EXISTS("SSH_KEY_PUBLIC_KEY_ALREADY_EXISTS"),
    SSH_KEY_NAME_ALREADY_EXISTS("SSH_KEY_NAME_ALREADY_EXISTS"),

    OPERATION_NOT_IMPLEMENTED("OPERATION_NOT_IMPLEMENTED"),

    SERVER_ERROR("SERVER_ERROR"),

    ILLOGICAL_OPERATION("ILLOGICAL_OPERATION"),

    INVALID_EMAIL_VERIFICATION_CODE("INVALID_EMAIL_VERIFICATION_CODE");

    // code means the error code in the message.properties
    private String code;

    ErrorCodeEnum() {}

    ErrorCodeEnum(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
