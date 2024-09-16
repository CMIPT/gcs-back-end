package edu.cmipt.gcs.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import edu.cmipt.gcs.constant.ApiPathConstant;
import edu.cmipt.gcs.constant.HeaderParameter;
import edu.cmipt.gcs.constant.ValidationConstant;
import edu.cmipt.gcs.enumeration.ErrorCodeEnum;
import edu.cmipt.gcs.exception.GenericException;
import edu.cmipt.gcs.pojo.error.ErrorVO;
import edu.cmipt.gcs.pojo.user.UserDTO;
import edu.cmipt.gcs.pojo.user.UserPO;
import edu.cmipt.gcs.pojo.user.UserVO;
import edu.cmipt.gcs.service.UserService;
import edu.cmipt.gcs.util.JwtUtil;
import edu.cmipt.gcs.validation.group.UpdateGroup;

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
import jakarta.validation.constraints.Size;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@Tag(name = "User", description = "User Related APIs")
public class UserController {
    @Autowired private UserService userService;

    @GetMapping(ApiPathConstant.USER_GET_USER_BY_NAME_API_PATH)
    @Operation(
            summary = "Get user by name",
            description = "Get user information by user name",
            tags = {"User", "Get Method"})
    @Parameters({
        @Parameter(
                name = HeaderParameter.ACCESS_TOKEN,
                description = "Access token",
                required = true,
                in = ParameterIn.HEADER,
                schema = @Schema(implementation = String.class)),
        @Parameter(
                name = "username",
                description = "User name",
                example = "admin",
                required = true,
                in = ParameterIn.PATH,
                schema = @Schema(implementation = String.class))
    })
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User information returned successfully"),
        @ApiResponse(
                responseCode = "404",
                description = "User not found",
                content = @Content(schema = @Schema(implementation = ErrorVO.class)))
    })
    public UserVO getUserByName(@PathVariable("username") String username) {
        QueryWrapper<UserPO> wrapper = new QueryWrapper<UserPO>();
        wrapper.eq("username", username);
        if (!userService.exists(wrapper)) {
            throw new GenericException(ErrorCodeEnum.USER_NOT_FOUND, username);
        }
        return new UserVO(userService.getOne(wrapper));
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
    @Parameters({
        @Parameter(
                name = HeaderParameter.ACCESS_TOKEN,
                description = "Access token",
                required = true,
                in = ParameterIn.HEADER,
                schema = @Schema(implementation = String.class)),
        @Parameter(
                name = HeaderParameter.REFRESH_TOKEN,
                description = "Refresh token",
                required = true,
                in = ParameterIn.HEADER,
                schema = @Schema(implementation = String.class))
    })
    public ResponseEntity<UserVO> updateUser(
            @RequestHeader(HeaderParameter.ACCESS_TOKEN) String accessToken,
            @RequestHeader(HeaderParameter.REFRESH_TOKEN) String refreshToken,
            @Validated(UpdateGroup.class) @RequestBody UserDTO user) {
        if (user.username() != null) {
            checkUsernameValidity(user.username());
        }
        if (user.email() != null) {
            checkEmailValidity(user.email());
        }
        // for the null fields, mybatis-plus will ignore by default
        assert user.id() != null;
        boolean res = userService.updateById(new UserPO(user));
        if (!res) {
            throw new GenericException(ErrorCodeEnum.USER_UPDATE_FAILED, user.toString());
        }
        UserVO userVO = new UserVO(userService.getById(Long.valueOf(user.id())));
        HttpHeaders headers = null;
        if (user.userPassword() != null) {
            JwtUtil.blacklistToken(accessToken, refreshToken);
            headers = JwtUtil.generateHeaders(userVO.id());
        }
        return ResponseEntity.ok().headers(headers).body(userVO);
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
                    @Email(message = "USERDTO_EMAIL_EMAIL {UserDTO.email.Email}")
                    @NotBlank(message = "USERDTO_EMAIL_NOTBLANK {UserDTO.email.NotBlank}")
                    String email) {
        QueryWrapper<UserPO> wrapper = new QueryWrapper<UserPO>();
        wrapper.eq("email", email);
        if (userService.exists(wrapper)) {
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
                            message = "USERDTO_USERNAME_SIZE {UserDTO.username.Size}")
                    @NotBlank(message = "USERDTO_USERNAME_NOTBLANK {UserDTO.username.NotBlank}")
                    String username) {
        QueryWrapper<UserPO> wrapper = new QueryWrapper<UserPO>();
        wrapper.eq("username", username);
        if (userService.exists(wrapper)) {
            throw new GenericException(ErrorCodeEnum.USERNAME_ALREADY_EXISTS, username);
        }
    }
}
