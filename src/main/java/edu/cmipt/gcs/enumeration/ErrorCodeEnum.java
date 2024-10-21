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

    USERNAME_PATTERN_MISMATCH("USERNAME_PATTERN_MISMATCH"),
    PASSWORD_PATTERN_MISMATCH("PASSWORD_PATTERN_MISMATCH"),

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

    REPOSITORYDTO_ID_NULL("RepositoryDTO.id.Null"),
    REPOSITORYDTO_ID_NOTNULL("RepositoryDTO.id.NotNull"),
    REPOSITORYDTO_REPOSITORYNAME_SIZE("RepositoryDTO.repositoryName.Size"),
    REPOSITORYDTO_REPOSITORYNAME_NOTBLANK("RepositoryDTO.repositoryName.NotBlank"),
    REPOSITORYDTO_REPOSITORYDESCRIPTION_SIZE("RepositoryDTO.repositoryDescription.Size"),
    REPOSITORYDTO_STAR_NULL("RepositoryDTO.star.Null"),
    REPOSITORYDTO_STAR_MIN("RepositoryDTO.star.Min"),
    REPOSITORYDTO_FORK_NULL("RepositoryDTO.fork.Null"),
    REPOSITORYDTO_FORK_MIN("RepositoryDTO.fork.Min"),
    REPOSITORYDTO_WATCHER_NULL("RepositoryDTO.watcher.Null"),
    REPOSITORYDTO_WATCHER_MIN("RepositoryDTO.watcher.Min"),

    REPOSITORYNAME_PATTERN_MISMATCH("REPOSITORYNAME_PATTERN_MISMATCH"),
    REPOSITORY_NOT_FOUND("REPOSITORY_NOT_FOUND"),
    REPOSITORY_ALREADY_EXISTS("REPOSITORY_ALREADY_EXISTS"),
    REPOSITORY_CREATE_FAILED("REPOSITORY_CREATE_FAILED"),
    REPOSITORY_UPDATE_FAILED("REPOSITORY_UPDATE_FAILED"),
    REPOSITORY_DELETE_FAILED("REPOSITORY_DELETE_FAILED"),

    COLLABORATION_ADD_FAILED("COLLABORATION_ADD_FAILED"),
    COLLABORATION_REMOVE_FAILED("COLLABORATION_REMOVE_FAILED"),
    COLLABORATION_ALREADY_EXISTS("COLLABORATION_ALREADY_EXISTS"),
    COLLABORATION_NOT_FOUND("COLLABORATION_NOT_FOUND"),

    SSHKEYDTO_ID_NULL("SshKeyDTO.id.Null"),
    SSHKEYDTO_ID_NOTNULL("SshKeyDTO.id.NotNull"),
    SSHKEYDTO_NAME_NOTBLANK("SshKeyDTO.name.NotBlank"),
    SSHKEYDTO_NAME_SIZE("SshKeyDTO.name.Size"),
    SSHKEYDTO_PUBLICKEY_NOTBLANK("SshKeyDTO.publicKey.NotBlank"),
    SSHKEYDTO_PUBLICKEY_SIZE("SshKeyDTO.publicKey.Size"),

    SSH_KEY_UPLOAD_FAILED("SSH_KEY_UPLOAD_FAILED"),
    SSH_KEY_UPDATE_FAILED("SSH_KEY_UPDATE_FAILED"),
    SSH_KEY_DELETE_FAILED("SSH_KEY_DELETE_FAILED"),
    SSH_KEY_NOT_FOUND("SSH_KEY_NOT_FOUND"),

    OPERATION_NOT_IMPLEMENTED("OPERATION_NOT_IMPLEMENTED"),

    SERVER_ERROR("SERVER_ERROR"),

    ILLOGICAL_OPERATION("ILLOGICAL_OPERATION");

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
