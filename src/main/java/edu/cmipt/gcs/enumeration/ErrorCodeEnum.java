package edu.cmipt.gcs.enumeration;

public enum ErrorCodeEnum {
    // This should be ignored, this is to make the ordinal of the enum start from 1
    ZERO_PLACEHOLDER,

    USERDTO_ID_NULL("UserDTO.id.Null"),
    USERDTO_ID_NOTNULL("UserDTO.id.NotNull"),
    USERDTO_USERNAME_SIZE("UserDTO.username.Size"),
    USERDTO_USERNAME_NOTBLANK("UserDTO.username.NotBlank"),
    USERDTO_EMAIL_NOTBLANK("UserDTO.email.NotBlank"),
    USERDTO_EMAIL_EMAIL("UserDTO.email.Email"),
    USERDTO_USERPASSWORD_SIZE("UserDTO.userPassword.Size"),
    USERDTO_USERPASSWORD_NOTBLANK("UserDTO.userPassword.NotBlank"),

    USERSIGNINDTO_USERNAME_NOTBLANK("UserSignInDTO.username.NotBlank"),
    USERSIGNINDTO_USERPASSWORD_NOTBLANK("UserSignInDTO.userPassword.NotBlank"),

    USERNAME_ALREADY_EXISTS("USERNAME_ALREADY_EXISTS"),
    EMAIL_ALREADY_EXISTS("EMAIL_ALREADY_EXISTS"),
    WRONG_SIGN_IN_INFORMATION("WRONG_SIGN_IN_INFORMATION"),

    INVALID_TOKEN("INVALID_TOKEN"),
    ACCESS_DENIED("ACCESS_DENIED"),

    MESSAGE_CONVERSION_ERROR("MESSAGE_CONVERSION_ERROR");

    // code means the error code in the message.properties
    private String code;

    ErrorCodeEnum(){}

    ErrorCodeEnum(String code) {
        this.code = code;
    }

    public String getCode() { return code; }
}
