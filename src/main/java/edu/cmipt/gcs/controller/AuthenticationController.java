package edu.cmipt.gcs.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import edu.cmipt.gcs.constant.ApiPathConstant;
import edu.cmipt.gcs.constant.ApplicationConstant;
import edu.cmipt.gcs.constant.HeaderParameter;
import edu.cmipt.gcs.enumeration.ErrorCodeEnum;
import edu.cmipt.gcs.exception.GenericException;
import edu.cmipt.gcs.pojo.error.ErrorVO;
import edu.cmipt.gcs.pojo.user.UserPO;
import edu.cmipt.gcs.pojo.user.UserSignInDTO;
import edu.cmipt.gcs.pojo.user.UserSignUpDTO;
import edu.cmipt.gcs.pojo.user.UserVO;
import edu.cmipt.gcs.service.UserService;
import edu.cmipt.gcs.util.EmailVerificationCodeUtil;
import edu.cmipt.gcs.util.JwtUtil;
import edu.cmipt.gcs.util.MD5Converter;
import edu.cmipt.gcs.util.MessageSourceUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.mail.internet.MimeMessage;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
    @Value("${spring.mail.username}")
    private String fromEmail;

    @Autowired private JavaMailSender javaMailSender;
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
    public void signUp(@Validated @RequestBody UserSignUpDTO user) {
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

    @GetMapping(ApiPathConstant.AUTHENTICATION_SEND_EMAIL_VERIFICATION_CODE_API_PATH)
    @Operation(
            summary = "Send email verification code",
            description = "Send email verification code to the given email",
            tags = {"Authentication", "Get Method"})
    @Parameters({
        @Parameter(
                name = "email",
                description = "Email",
                required = true,
                in = ParameterIn.QUERY,
                schema = @Schema(implementation = String.class))
    })
    @ApiResponse(responseCode = "200", description = "Email verification code sent successfully")
    public void sendEmailVerificationCode(
            @RequestParam("email")
                    @Email(
                            message =
                                    "{Email.authenticationController#sendEmailVerificationCode.email}")
                    @NotBlank(
                            message =
                                    "{NotBlank.authenticationController#sendEmailVerificationCode.email}")
                    String email) {
        String code = EmailVerificationCodeUtil.generateVerificationCode(email);
        MimeMessage message = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(fromEmail);
            helper.setTo(email);
            helper.setSubject(MessageSourceUtil.getMessage("EMAIL_VERIFICATION_CODE_SUBJECT"));
            helper.setText(
                    MessageSourceUtil.getMessage(
                            "EMAIL_VERIFICATION_CODE_CONTENT",
                            code,
                            ApplicationConstant.EMAIL_VERIFICATION_CODE_EXPIRATION / 60000));
        } catch (Exception e) {
            throw new GenericException(ErrorCodeEnum.SERVER_ERROR, e);
        }
        javaMailSender.send(message);
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
        HttpHeaders headers = JwtUtil.generateHeaders(userVO.id());
        return ResponseEntity.ok().headers(headers).body(userVO);
    }

    @DeleteMapping(ApiPathConstant.AUTHENTICATION_SIGN_OUT_API_PATH)
    @Operation(
            summary = "Sign out",
            description = "Sign out with the given token",
            tags = {"Authentication", "Delete Method"})
    @ApiResponse(responseCode = "200", description = "User signed out successfully")
    @Parameters({
        @Parameter(
                name = "id",
                description = "User ID",
                required = true,
                in = ParameterIn.QUERY,
                schema = @Schema(implementation = Long.class))
    })
    public void signOut(@RequestParam("id") Long id) {
        JwtUtil.blacklistToken(id);
    }

    @GetMapping(ApiPathConstant.AUTHENTICATION_REFRESH_API_PATH)
    @Operation(
            summary = "Refresh token",
            description = "Return an access token with given refresh token",
            tags = {"Authentication", "Get Method"})
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
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Token refreshed successfully",
                content = @Content(schema = @Schema(implementation = String.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Void> refreshToken(
            @RequestHeader(HeaderParameter.REFRESH_TOKEN) String refreshToken) {
        HttpHeaders headers = JwtUtil.generateHeaders(JwtUtil.getId(refreshToken), false);
        return ResponseEntity.ok().headers(headers).build();
    }
}
