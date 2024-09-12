package edu.cmipt.gcs.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import edu.cmipt.gcs.constant.ApiPathConstant;
import edu.cmipt.gcs.constant.HeaderParameter;
import edu.cmipt.gcs.enumeration.ErrorCodeEnum;
import edu.cmipt.gcs.enumeration.TokenTypeEnum;
import edu.cmipt.gcs.exception.GenericException;
import edu.cmipt.gcs.pojo.error.ErrorVO;
import edu.cmipt.gcs.pojo.user.UserDTO;
import edu.cmipt.gcs.pojo.user.UserPO;
import edu.cmipt.gcs.pojo.user.UserSignInDTO;
import edu.cmipt.gcs.pojo.user.UserVO;
import edu.cmipt.gcs.service.UserService;
import edu.cmipt.gcs.util.JwtUtil;
import edu.cmipt.gcs.util.MD5Converter;
import edu.cmipt.gcs.validation.group.CreateGroup;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * AuthenticationController
 *
 * <p>Controller for authentication APIs
 *
 * @author Kaiser
 */
@RestController
@Tag(name = "Authentication", description = "Authentication APIs")
public class AuthenticationController {
    @Autowired private UserService userService;

    @PostMapping(ApiPathConstant.AUTHENTICATION_SIGN_UP_API_PATH)
    @Operation(
            summary = "Sign up a user",
            description = "Sign up a user with the given information",
            tags = {"Authentication", "Post Method"})
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User signed up successfully"),
        @ApiResponse(
                responseCode = "400",
                description = "User sign up failed",
                content = @Content(schema = @Schema(implementation = ErrorVO.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public void signUp(@Validated(CreateGroup.class) @RequestBody UserDTO user) {
        QueryWrapper<UserPO> wrapper = new QueryWrapper<UserPO>();
        wrapper.eq("username", user.username());
        if (userService.exists(wrapper)) {
            throw new GenericException(ErrorCodeEnum.USERNAME_ALREADY_EXISTS, user.username());
        }
        wrapper.clear();
        wrapper.eq("email", user.email());
        if (userService.exists(wrapper)) {
            throw new GenericException(ErrorCodeEnum.EMAIL_ALREADY_EXISTS, user.email());
        }
        boolean res = userService.save(new UserPO(user));
        if (!res) {
            throw new RuntimeException("Failed to sign up user");
        }
    }

    @PostMapping(ApiPathConstant.AUTHENTICATION_SIGN_IN_API_PATH)
    @Operation(
            summary = "Sign in a user",
            description = "Sign in a user with the given information",
            tags = {"Authentication", "Post Method"})
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "User signed in successfully",
                content = @Content(schema = @Schema(implementation = UserVO.class))),
        @ApiResponse(
                responseCode = "400",
                description = "User sign in failed",
                content = @Content(schema = @Schema(implementation = ErrorVO.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<UserVO> signIn(@Validated @RequestBody UserSignInDTO user) {
        QueryWrapper<UserPO> wrapper = new QueryWrapper<UserPO>();
        wrapper.eq("username", user.username());
        wrapper.eq("user_password", MD5Converter.convertToMD5(user.userPassword()));
        if (!userService.exists(wrapper)) {
            throw new GenericException(ErrorCodeEnum.WRONG_SIGN_IN_INFORMATION);
        }
        UserVO userVO = new UserVO(userService.getOne(wrapper));
        HttpHeaders headers = new HttpHeaders();
        headers.add(
                HeaderParameter.ACCESS_TOKEN,
                JwtUtil.generateToken(userVO.id(), TokenTypeEnum.ACCESS_TOKEN));
        headers.add(
                HeaderParameter.REFRESH_TOKEN,
                JwtUtil.generateToken(userVO.id(), TokenTypeEnum.REFRESH_TOKEN));
        return ResponseEntity.ok().headers(headers).body(userVO);
    }

    @DeleteMapping(ApiPathConstant.AUTHENTICATION_SIGN_OUT_API_PATH)
    @Operation(
            summary = "Sign out",
            description = "Sign out with the given token",
            tags = {"Authentication", "Delete Method"})
    @ApiResponse(responseCode = "200", description = "User signed out successfully")
    public void signOut(@RequestBody List<String> tokenList) {
        JwtUtil.blacklistToken(tokenList);
    }

    @GetMapping(ApiPathConstant.AUTHENTICATION_REFRESH_API_PATH)
    @Operation(
            summary = "Refresh token",
            description = "Return an access token with given refresh token",
            tags = {"Authentication", "Get Method"})
    @Parameter(
            name = HeaderParameter.TOKEN,
            description = "Refresh token",
            required = true,
            in = ParameterIn.HEADER,
            schema = @Schema(implementation = String.class))
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Token refreshed successfully",
                content = @Content(schema = @Schema(implementation = String.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Void> refreshToken(@RequestHeader(HeaderParameter.TOKEN) String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(
                HeaderParameter.ACCESS_TOKEN,
                JwtUtil.generateToken(JwtUtil.getID(token), TokenTypeEnum.ACCESS_TOKEN));
        return ResponseEntity.ok().headers(headers).build();
    }
}
