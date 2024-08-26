package edu.cmipt.gcs.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.cmipt.gcs.constant.ApiPathConstant;
import edu.cmipt.gcs.constant.ErrorMessageConstant;
import edu.cmipt.gcs.pojo.user.UserDTO;
import edu.cmipt.gcs.pojo.user.UserPO;
import edu.cmipt.gcs.pojo.user.UserSignInDTO;
import edu.cmipt.gcs.pojo.user.UserVO;
import edu.cmipt.gcs.service.UserService;
import edu.cmipt.gcs.util.ErrorMessageUtil;
import edu.cmipt.gcs.util.JwtUtil;
import edu.cmipt.gcs.util.MD5Converter;
import edu.cmipt.gcs.validation.group.CreateGroup;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * AuthenticationController
 *
 * Controller for authentication APIs
 *
 * @author Kaiser
 */
@Controller
@Tag(name = "Authentication", description = "Authentication APIs")
public class AuthenticationController {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationController.class);
    @Autowired
    private UserService userService;

    @PostMapping(ApiPathConstant.AUTHENTICATION_SIGN_UP_API_PATH)
    @Operation(summary = "Sign up a user", description = "Sign up a user with the given information", tags = {
            "Authentication", "Post Method" })
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User signed up successfully", content = @Content(schema = @Schema(implementation = UserVO.class))),
            @ApiResponse(responseCode = "400", description = "User sign up failed", content = @Content(schema = @Schema(implementation = ErrorMessageConstant.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> signUp(@Validated(CreateGroup.class) @RequestBody UserDTO user) {
        try {
            QueryWrapper<UserPO> wrapper = new QueryWrapper<UserPO>();
            wrapper.eq("username", user.username());
            if (userService.exists(wrapper)) {
                return ResponseEntity.badRequest()
                        .body(ErrorMessageUtil.generateError(ErrorMessageConstant.USERNAME_ALREADY_EXISTS));
            }
            wrapper.clear();
            wrapper.eq("email", user.email());
            if (userService.exists(wrapper)) {
                return ResponseEntity.badRequest()
                        .body(ErrorMessageUtil.generateError(ErrorMessageConstant.EMAIL_ALREADY_EXISTS));
            }
            boolean res = userService.save(new UserPO(user));
            if (!res) {
                logger.error("Create user failed");
                return ResponseEntity.internalServerError().build();
            }
            return ResponseEntity.ok(new UserVO(userService.getOne(wrapper)));
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping(ApiPathConstant.AUTHENTICATION_SIGN_IN_API_PATH)
    @Operation(summary = "Sign in a user", description = "Sign in a user with the given information", tags = {
            "Authentication", "Post Method" })
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User signed in successfully", content = @Content(schema = @Schema(implementation = UserVO.class))),
            @ApiResponse(responseCode = "400", description = "User sign in failed", content = @Content(schema = @Schema(implementation = ErrorMessageConstant.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> signIn(@Validated @RequestBody UserSignInDTO user) {
        try {
            QueryWrapper<UserPO> wrapper = new QueryWrapper<UserPO>();
            wrapper.eq("username", user.username());
            wrapper.eq("user_password", MD5Converter.convertToMD5(user.userPassword()));
            if (!userService.exists(wrapper)) {
                return ResponseEntity.badRequest()
                        .body(ErrorMessageUtil.generateError(ErrorMessageConstant.WRONG_SIGN_IN_INFORMATION));
            }
            return ResponseEntity.ok(new UserVO(userService.getOne(wrapper)));
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping(ApiPathConstant.AUTHENTICATION_SIGN_OUT_API_PATH)
    @Operation(summary = "Sign out", description = "Sign out with the given token", tags = { "Authentication",
            "Delete Method" })
    @ApiResponse(responseCode = "200", description = "User signed out successfully")
    public ResponseEntity<Void> signOut(@RequestBody List<String> tokenList) {
        JwtUtil.blacklistToken(tokenList);
        return ResponseEntity.ok().build();
    }
}
