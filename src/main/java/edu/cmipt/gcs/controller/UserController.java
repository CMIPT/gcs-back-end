package edu.cmipt.gcs.controller;

import edu.cmipt.gcs.constant.ApiPathConstant;
import edu.cmipt.gcs.enumeration.ErrorCodeEnum;
import edu.cmipt.gcs.exception.GenericException;
import edu.cmipt.gcs.pojo.error.ErrorVO;
import edu.cmipt.gcs.pojo.user.UserPO;
import edu.cmipt.gcs.pojo.user.UserVO;
import edu.cmipt.gcs.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

@RestController
@Tag(name = "User", description = "User Related APIs")
public class UserController {
    @Autowired private UserService userService;

    @GetMapping(ApiPathConstant.USER_GET_BY_NAME_API_PATH)
    @Operation(
            summary = "Get user by name",
            description = "Get user information by user name",
            tags = {"User", "Get Method"})
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
}
