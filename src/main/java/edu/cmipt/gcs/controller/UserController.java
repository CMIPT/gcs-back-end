package edu.cmipt.gcs.controller;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;

import edu.cmipt.gcs.constant.ApiPathConstant;
import edu.cmipt.gcs.constant.HeaderParameter;
import edu.cmipt.gcs.constant.ValidationConstant;
import edu.cmipt.gcs.enumeration.ErrorCodeEnum;
import edu.cmipt.gcs.enumeration.UserQueryTypeEnum;
import edu.cmipt.gcs.exception.GenericException;
import edu.cmipt.gcs.pojo.error.ErrorVO;
import edu.cmipt.gcs.pojo.user.UserCreateDTO;
import edu.cmipt.gcs.pojo.user.UserPO;
import edu.cmipt.gcs.pojo.user.UserResetPasswordDTO;
import edu.cmipt.gcs.pojo.user.UserUpdateDTO;
import edu.cmipt.gcs.pojo.user.UserUpdatePasswordDTO;
import edu.cmipt.gcs.pojo.user.UserVO;
import edu.cmipt.gcs.service.UserService;
import edu.cmipt.gcs.util.EmailVerificationCodeUtil;
import edu.cmipt.gcs.util.JwtUtil;
import edu.cmipt.gcs.util.MD5Converter;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@Validated
@RestController
@Tag(name = "User", description = "User Related APIs")
public class UserController {
    @Autowired private UserService userService;

    private Set<String> reservedUsernames =
            Set.of(
                    "new",
                    "settings",
                    "login",
                    "logout",
                    "admin",
                    "signup",
                    "testing",
                    "gitolite-admin");

    @PostMapping(ApiPathConstant.USER_CREATE_USER_API_PATH)
    @Operation(
            summary = "Create a user",
            description = "Create a user with the given information",
            tags = {"User", "Post Method"})
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User created successfully"),
        @ApiResponse(
                responseCode = "400",
                description = "User created failed",
                content = @Content(schema = @Schema(implementation = ErrorVO.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public void createUser(@Validated @RequestBody UserCreateDTO user) {
        checkUsernameValidity(user.username());
        checkEmailValidity(user.email());
        if (!EmailVerificationCodeUtil.verifyVerificationCode(
                user.email(), user.emailVerificationCode())) {
            throw new GenericException(
                    ErrorCodeEnum.INVALID_EMAIL_VERIFICATION_CODE, user.emailVerificationCode());
        }
        boolean res = userService.save(new UserPO(user));
        if (!res) {
            throw new GenericException(ErrorCodeEnum.USER_CREATE_FAILED, user);
        }
    }

    @GetMapping(ApiPathConstant.USER_GET_USER_API_PATH)
    @Operation(
            summary = "Get a user",
            description = "Get a user's information",
            tags = {"User", "Get Method"})
    @Parameters({
        @Parameter(
                name = HeaderParameter.ACCESS_TOKEN,
                description = "Access token",
                required = true,
                in = ParameterIn.HEADER,
                schema = @Schema(implementation = String.class)),
        @Parameter(
                name = "user",
                description = "User's Information",
                example = "admin",
                required = false,
                in = ParameterIn.QUERY,
                schema = @Schema(implementation = String.class)),
        @Parameter(
                name = "userType",
                description = "User's Type. The value can be 'ID', 'USERNAME', 'EMAIL', or 'TOKEN'",
                example = "USERNAME",
                required = true,
                in = ParameterIn.QUERY,
                schema = @Schema(implementation = UserQueryTypeEnum.class))
    })
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User information returned successfully"),
        @ApiResponse(
                responseCode = "404",
                description = "User not found",
                content = @Content(schema = @Schema(implementation = ErrorVO.class)))
    })
    public UserVO getUser(
            @RequestParam(name = "user", required = false) String user,
            @RequestParam("userType") UserQueryTypeEnum userType,
            @RequestHeader(HeaderParameter.ACCESS_TOKEN) String accessToken) {
        var wrapper = userType.getQueryWrapper(user, accessToken);
        var userPO = userService.getOne(wrapper);
        if (userPO == null) {
            throw new GenericException(
                    ErrorCodeEnum.USER_NOT_FOUND, user != null ? user : accessToken);
        }
        return new UserVO(userPO);
    }

    @PostMapping(ApiPathConstant.USER_UPDATE_USER_API_PATH)
    @Operation(
            summary = "Update user",
            description = "Update user information",
            tags = {"User", "Post Method"})
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User information updated successfully"),
        @ApiResponse(
                responseCode = "400",
                description = "User information update failed",
                content = @Content(schema = @Schema(implementation = ErrorVO.class)))
    })
    @Parameter(
            name = HeaderParameter.ACCESS_TOKEN,
            description = "Access token",
            required = true,
            in = ParameterIn.HEADER,
            schema = @Schema(implementation = String.class))
    public ResponseEntity<UserVO> updateUser(
            @Validated @RequestBody UserUpdateDTO user,
            @RequestHeader(HeaderParameter.ACCESS_TOKEN) String accessToken) {
        if (user.username() != null) {
            throw new GenericException(ErrorCodeEnum.OPERATION_NOT_IMPLEMENTED);
        }
        Long idInToken = Long.valueOf(JwtUtil.getId(accessToken));
        // for the null fields, mybatis-plus will ignore by default
        if (!userService.updateById(new UserPO(user, idInToken))) {
            throw new GenericException(ErrorCodeEnum.USER_UPDATE_FAILED, user);
        }
        var userVO = new UserVO(userService.getById(idInToken));
        return ResponseEntity.ok().body(userVO);
    }

    @PostMapping(ApiPathConstant.USER_UPDATE_USER_PASSWORD_WITH_OLD_PASSWORD_API_PATH)
    @Operation(
            summary = "Update user password",
            description = "Update user password",
            tags = {"User", "Post Method"})
    @Parameter(
            name = HeaderParameter.ACCESS_TOKEN,
            description = "Access token",
            required = true,
            in = ParameterIn.HEADER,
            schema = @Schema(implementation = String.class))
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User password updated successfully"),
        @ApiResponse(
                responseCode = "400",
                description = "User password update failed",
                content = @Content(schema = @Schema(implementation = ErrorVO.class)))
    })
    public void updateUserPassword(
            @Validated @RequestBody UserUpdatePasswordDTO user,
            @RequestHeader(HeaderParameter.ACCESS_TOKEN) String accessToken) {
        var wrapper = new UpdateWrapper<UserPO>();
        Long idInToken = Long.valueOf(JwtUtil.getId(accessToken));
        wrapper.eq("id", idInToken);
        wrapper.eq("user_password", MD5Converter.convertToMD5(user.oldPassword()));
        if (!userService.exists(wrapper)) {
            throw new GenericException(ErrorCodeEnum.WRONG_UPDATE_PASSWORD_INFORMATION);
        }
        checkPasswordValidity(user.newPassword());
        wrapper.set("user_password", MD5Converter.convertToMD5(user.newPassword()));
        if (!userService.update(wrapper)) {
            throw new GenericException(ErrorCodeEnum.USER_UPDATE_FAILED, user);
        }
        JwtUtil.blacklistToken(idInToken);
    }

    @PostMapping(ApiPathConstant.USER_UPDATE_USER_PASSWORD_WITH_EMAIL_VERIFICATION_CODE_API_PATH)
    @Operation(
            summary = "Reset user's password",
            description = "Reset user's password with email verification code",
            tags = {"User", "Post Method"})
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User password updated successfully"),
        @ApiResponse(
                responseCode = "400",
                description = "User password update failed",
                content = @Content(schema = @Schema(implementation = ErrorVO.class)))
    })
    public void resetUserPassword(@RequestBody @Validated UserResetPasswordDTO user) {
        if (!EmailVerificationCodeUtil.verifyVerificationCode(
                user.email(), user.emailVerificationCode())) {
            throw new GenericException(
                    ErrorCodeEnum.INVALID_EMAIL_VERIFICATION_CODE, user.emailVerificationCode());
        }
        if (!userService.emailExists(user.email())) {
            throw new GenericException(ErrorCodeEnum.USER_NOT_FOUND, user.email());
        }
        var wrapper = new UpdateWrapper<UserPO>();
        wrapper.apply("LOWER(email) = LOWER({0})", user.email());
        wrapper.set("user_password", MD5Converter.convertToMD5(user.newPassword()));
        if (!userService.update(wrapper)) {
            throw new GenericException(ErrorCodeEnum.USER_UPDATE_FAILED, user.email());
        }
        JwtUtil.blacklistToken(userService.getOne(wrapper).getId());
    }

    @DeleteMapping(ApiPathConstant.USER_DELETE_USER_API_PATH)
    @Operation(
            summary = "Delete user",
            description = "Delete user by id",
            tags = {"User", "Delete Method"})
    @Parameter(
            name = HeaderParameter.ACCESS_TOKEN,
            description = "Access token",
            required = true,
            in = ParameterIn.HEADER,
            schema = @Schema(implementation = String.class))
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User deleted successfully"),
        @ApiResponse(
                responseCode = "404",
                description = "User not found",
                content = @Content(schema = @Schema(implementation = ErrorVO.class)))
    })
    public void deleteUser() {
        // do not support delete user by now
        throw new GenericException(ErrorCodeEnum.OPERATION_NOT_IMPLEMENTED);
        // if (userService.getById(id) == null) {
        //     throw new GenericException(ErrorCodeEnum.USER_NOT_FOUND, id);
        // }
        // if (!userService.removeById(id)) {
        //     throw new GenericException(ErrorCodeEnum.USER_DELETE_FAILED, id);
        // }
        // JwtUtil.blacklistToken(id);
    }

    @GetMapping(ApiPathConstant.USER_CHECK_EMAIL_VALIDITY_API_PATH)
    @Operation(
            summary = "Check email validity",
            description = "Check if the email is valid",
            tags = {"User", "Get Method"})
    @Parameter(
            name = "email",
            description = "Email",
            example = "admin@cmipt.edu",
            required = true,
            in = ParameterIn.QUERY,
            schema = @Schema(implementation = String.class))
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Email validity checked successfully"),
        @ApiResponse(
                responseCode = "400",
                description = "Email is invalid",
                content = @Content(schema = @Schema(implementation = ErrorVO.class)))
    })
    public void checkEmailValidity(
            @RequestParam("email")
                    @Email(message = "{Email.userController#checkEmailValidity.email}")
                    @NotBlank(message = "{NotBlank.userController#checkEmailValidity.email}")
                    String email) {
        if (userService.emailExists(email)) {
            throw new GenericException(ErrorCodeEnum.EMAIL_ALREADY_EXISTS, email);
        }
    }

    @GetMapping(ApiPathConstant.USER_CHECK_USERNAME_VALIDITY_API_PATH)
    @Operation(
            summary = "Check username validity",
            description = "Check if the username is valid",
            tags = {"User", "Get Method"})
    @Parameter(
            name = "username",
            description = "User name",
            example = "admin",
            required = true,
            in = ParameterIn.QUERY,
            schema = @Schema(implementation = String.class))
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Username validity checked successfully"),
        @ApiResponse(
                responseCode = "400",
                description = "Username is not valid",
                content = @Content(schema = @Schema(implementation = ErrorVO.class)))
    })
    public void checkUsernameValidity(
            @RequestParam("username")
                    @Size(
                            min = ValidationConstant.MIN_USERNAME_LENGTH,
                            max = ValidationConstant.MAX_USERNAME_LENGTH,
                            message = "{Size.userController#checkUsernameValidity.username}")
                    @NotBlank(message = "{NotBlank.userController#checkUsernameValidity.username}")
                    @Pattern(
                            regexp = ValidationConstant.USERNAME_PATTERN,
                            message = "{Pattern.userController#checkUsernameValidity.username}")
                    String username) {
        if (reservedUsernames.contains(username)) {
            throw new GenericException(ErrorCodeEnum.USERNAME_RESERVED, username);
        }
        if (userService.usernameExists(username)) {
            throw new GenericException(ErrorCodeEnum.USERNAME_ALREADY_EXISTS, username);
        }
    }

    @GetMapping(ApiPathConstant.USER_CHECK_USER_PASSWORD_VALIDITY_API_PATH)
    @Operation(
            summary = "Check password validity",
            description = "Check if the password is valid",
            tags = {"User", "Get Method"})
    @Parameter(
            name = "userPassword",
            description = "User's Password",
            example = "123456",
            required = true,
            in = ParameterIn.QUERY,
            schema = @Schema(implementation = String.class))
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Password validity checked successfully"),
        @ApiResponse(
                responseCode = "400",
                description = "Password is not valid",
                content = @Content(schema = @Schema(implementation = ErrorVO.class)))
    })
    public void checkPasswordValidity(
            @RequestParam("userPassword")
                    @Size(
                            min = ValidationConstant.MIN_PASSWORD_LENGTH,
                            max = ValidationConstant.MAX_PASSWORD_LENGTH,
                            message = "{Size.userController#checkPasswordValidity.password}")
                    @NotBlank(message = "{NotBlank.userController#checkPasswordValidity.password}")
                    @Pattern(
                            regexp = ValidationConstant.PASSWORD_PATTERN,
                            message = "{Pattern.userController#checkPasswordValidity.password}")
                    String password) {}
}
