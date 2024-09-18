package edu.cmipt.gcs.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import edu.cmipt.gcs.constant.ApiPathConstant;
import edu.cmipt.gcs.constant.HeaderParameter;
import edu.cmipt.gcs.constant.ValidationConstant;
import edu.cmipt.gcs.enumeration.ErrorCodeEnum;
import edu.cmipt.gcs.exception.GenericException;
import edu.cmipt.gcs.pojo.error.ErrorVO;
import edu.cmipt.gcs.pojo.repository.RepositoryPO;
import edu.cmipt.gcs.pojo.repository.RepositoryVO;
import edu.cmipt.gcs.pojo.user.UserDTO;
import edu.cmipt.gcs.pojo.user.UserPO;
import edu.cmipt.gcs.pojo.user.UserVO;
import edu.cmipt.gcs.service.RepositoryService;
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

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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
    @Autowired private RepositoryService repositoryService;

    @GetMapping(ApiPathConstant.USER_GET_USER_API_PATH)
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
    public UserVO getUser(@RequestParam("username") String username) {
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
            throw new GenericException(ErrorCodeEnum.USER_UPDATE_FAILED, user);
        }
        UserVO userVO = new UserVO(userService.getById(Long.valueOf(user.id())));
        HttpHeaders headers = null;
        if (user.userPassword() != null) {
            JwtUtil.blacklistToken(accessToken, refreshToken);
            headers = JwtUtil.generateHeaders(userVO.id());
        }
        return ResponseEntity.ok().headers(headers).body(userVO);
    }

    @DeleteMapping(ApiPathConstant.USER_DELETE_USER_API_PATH)
    @Operation(
            summary = "Delete user",
            description = "Delete user by id",
            tags = {"User", "Delete Method"})
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
                schema = @Schema(implementation = String.class)),
        @Parameter(
                name = "id",
                description = "User id",
                required = true,
                in = ParameterIn.QUERY,
                schema = @Schema(implementation = Long.class))
    })
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User deleted successfully"),
        @ApiResponse(
                responseCode = "404",
                description = "User not found",
                content = @Content(schema = @Schema(implementation = ErrorVO.class)))
    })
    public void deleteUser(
            @RequestHeader(HeaderParameter.ACCESS_TOKEN) String accessToken,
            @RequestHeader(HeaderParameter.REFRESH_TOKEN) String refreshToken,
            @RequestParam("id") Long id) {
        if (userService.getById(id) == null) {
            throw new GenericException(ErrorCodeEnum.USER_NOT_FOUND, id);
        }
        boolean res = userService.removeById(id);
        if (!res) {
            throw new GenericException(ErrorCodeEnum.USER_DELETE_FAILED, id);
        }
        JwtUtil.blacklistToken(accessToken, refreshToken);
    }

    @GetMapping(ApiPathConstant.USER_PAGE_USER_REPOSITORY_API_PATH)
    @Operation(
            summary = "Page user repositories",
            description = "Page user repositories. If the given token is trying to get other's repositories, only public repositories will be shown",
            tags = {"User", "Get Method"})
    @Parameters({
        @Parameter(
                name = HeaderParameter.ACCESS_TOKEN,
                description = "Access token",
                required = true,
                in = ParameterIn.HEADER,
                schema = @Schema(implementation = String.class)),
        @Parameter(
                name = "id",
                description = "User id",
                required = true,
                in = ParameterIn.QUERY,
                schema = @Schema(implementation = Long.class)),
        @Parameter(
                name = "page",
                description = "Page number",
                example = "1",
                required = true,
                in = ParameterIn.QUERY,
                schema = @Schema(implementation = Integer.class)),
        @Parameter(
                name = "size",
                description = "Page size",
                example = "10",
                required = true,
                in = ParameterIn.QUERY,
                schema = @Schema(implementation = Integer.class))
    })
    @ApiResponse(responseCode = "200", description = "User repositories paged successfully")
    public List<RepositoryVO> pageUserRepositories(
            @RequestParam("id") Long userId,
            @RequestParam("page") Integer page,
            @RequestParam("size") Integer size,
            @RequestHeader(HeaderParameter.ACCESS_TOKEN) String accessToken) {
        QueryWrapper<RepositoryPO> wrapper = new QueryWrapper<RepositoryPO>();
        String idInToken = JwtUtil.getID(accessToken);
        assert idInToken != null;
        if (!idInToken.equals(userId.toString())) {
            // the user only can see the public repositories of others
            wrapper.eq("is_private", false);
        }
        wrapper.eq("user_id", userId);
        return repositoryService.page(new Page<>(page, size), wrapper).getRecords().stream().map(RepositoryVO::new).collect(Collectors.toList());
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
