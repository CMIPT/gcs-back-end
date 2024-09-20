package edu.cmipt.gcs.controller;

import edu.cmipt.gcs.service.RepositoryService;
import edu.cmipt.gcs.util.JwtUtil;
import edu.cmipt.gcs.validation.group.CreateGroup;
import edu.cmipt.gcs.constant.ApiPathConstant;
import edu.cmipt.gcs.constant.HeaderParameter;
import edu.cmipt.gcs.enumeration.ErrorCodeEnum;
import edu.cmipt.gcs.exception.GenericException;
import edu.cmipt.gcs.pojo.repository.RepositoryDTO;
import edu.cmipt.gcs.pojo.repository.RepositoryPO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Schema;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

@RestController
@Tag(name = "Repository", description = "Repository Related APIs")
public class RepositoryController {
    @Autowired private RepositoryService repositoryService;

    @PostMapping(ApiPathConstant.REPOSITORY_CREATE_REPOSITORY_API_PATH)
    @Operation(
            summary = "Create a repository",
            description = "Create a repository with the given information",
            tags = {"Repository", "Post Method"})
    @Parameters({
        @Parameter(
                name = HeaderParameter.ACCESS_TOKEN,
                description = "Access token",
                required = true,
                in = ParameterIn.HEADER,
                schema = @Schema(implementation = String.class))
    })
    @ApiResponse(responseCode = "200", description = "Repository created successfully")
    public void createRepository(@Validated(CreateGroup.class) @RequestBody RepositoryDTO repository, @RequestHeader(HeaderParameter.ACCESS_TOKEN) String accessToken) {
        String userId = JwtUtil.getId(accessToken);
        RepositoryPO repositoryPO = new RepositoryPO(repository, userId);
        QueryWrapper<RepositoryPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", repositoryPO.getUserId());
        queryWrapper.eq("repository_name", repositoryPO.getRepositoryName());
        if (repositoryService.exists(queryWrapper)) {
            throw new GenericException(ErrorCodeEnum.REPOSITORY_ALREADY_EXISTS, repository);
        }
        if (!repositoryService.save(repositoryPO)) {
            throw new GenericException(ErrorCodeEnum.REPOSITORY_CREATE_FAILED, repository);
        }
    }
}
