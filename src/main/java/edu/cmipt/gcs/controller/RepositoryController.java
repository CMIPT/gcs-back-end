package edu.cmipt.gcs.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import edu.cmipt.gcs.constant.ApiPathConstant;
import edu.cmipt.gcs.constant.HeaderParameter;
import edu.cmipt.gcs.enumeration.ErrorCodeEnum;
import edu.cmipt.gcs.exception.GenericException;
import edu.cmipt.gcs.pojo.repository.RepositoryDTO;
import edu.cmipt.gcs.pojo.repository.RepositoryPO;
import edu.cmipt.gcs.pojo.repository.RepositoryVO;
import edu.cmipt.gcs.service.RepositoryService;
import edu.cmipt.gcs.util.JwtUtil;
import edu.cmipt.gcs.validation.group.CreateGroup;
import edu.cmipt.gcs.validation.group.UpdateGroup;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Repository", description = "Repository Related APIs")
public class RepositoryController {
    private static final Logger logger = LoggerFactory.getLogger(SshKeyController.class);
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
    public void createRepository(
            @Validated(CreateGroup.class) @RequestBody RepositoryDTO repository,
            @RequestHeader(HeaderParameter.ACCESS_TOKEN) String accessToken) {
        String userId = JwtUtil.getId(accessToken);
        RepositoryPO repositoryPO = new RepositoryPO(repository, userId, true);
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

    @DeleteMapping(ApiPathConstant.REPOSITORY_DELETE_REPOSITORY_API_PATH)
    @Operation(
            summary = "Delete a repository",
            description = "Delete a repository with the given id",
            tags = {"Repository", "Delete Method"})
    @Parameters({
        @Parameter(
                name = HeaderParameter.ACCESS_TOKEN,
                description = "Access token",
                required = true,
                in = ParameterIn.HEADER,
                schema = @Schema(implementation = String.class)),
        @Parameter(
                name = "id",
                description = "Repository id",
                required = true,
                in = ParameterIn.QUERY,
                schema = @Schema(implementation = Long.class))
    })
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Repository deleted successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "404", description = "Repository not found")
    })
    public void deleteRepository(
            @RequestHeader(HeaderParameter.ACCESS_TOKEN) String accessToken,
            @RequestParam("id") Long id) {
        var repository = repositoryService.getById(id);
        if (repository == null) {
            throw new GenericException(ErrorCodeEnum.REPOSITORY_NOT_FOUND, id);
        }
        String userId = JwtUtil.getId(accessToken);
        if (!userId.equals(repository.getUserId().toString())) {
            logger.info(
                    "User[{}] tried to delete repository of user[{}]",
                    userId,
                    repository.getUserId());
            throw new GenericException(ErrorCodeEnum.ACCESS_DENIED);
        }
        if (!repositoryService.removeById(id)) {
            throw new GenericException(ErrorCodeEnum.REPOSITORY_DELETE_FAILED, id);
        }
    }

    @PostMapping(ApiPathConstant.REPOSITORY_UPDATE_REPOSITORY_API_PATH)
    @Operation(
            summary = "Update a repository",
            description = "Update a repository with the given information",
            tags = {"Repository", "Post Method"})
    @Parameter(
            name = HeaderParameter.ACCESS_TOKEN,
            description = "Access token",
            required = true,
            in = ParameterIn.HEADER,
            schema = @Schema(implementation = String.class))
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Repository updated successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "404", description = "Repository not found"),
        @ApiResponse(
                responseCode = "501",
                description = "Update repository name is not implemented")
    })
    public ResponseEntity<RepositoryVO> updateRepository(
            @Validated(UpdateGroup.class) @RequestBody RepositoryDTO repository,
            @RequestHeader(HeaderParameter.ACCESS_TOKEN) String accessToken) {
        Long id = null;
        try {
            id = Long.valueOf(repository.id());
        } catch (NumberFormatException e) {
            logger.error(e.getMessage());
            throw new GenericException(ErrorCodeEnum.MESSAGE_CONVERSION_ERROR);
        }
        var repositoryPO = repositoryService.getById(id);
        if (repositoryPO == null) {
            throw new GenericException(ErrorCodeEnum.REPOSITORY_NOT_FOUND, id);
        }
        String userId = JwtUtil.getId(accessToken);
        if (!userId.equals(repositoryPO.getUserId().toString())) {
            logger.info(
                    "User[{}] tried to update repository of user[{}]",
                    userId,
                    repositoryPO.getUserId());
            throw new GenericException(ErrorCodeEnum.ACCESS_DENIED);
        }
        if (repository.repositoryName() != null
                && !repository
                        .repositoryName()
                        .equals(repositoryService.getById(id).getRepositoryName())) {
            throw new GenericException(
                    ErrorCodeEnum.OPERATION_NOT_IMPLEMENTED,
                    "update repository name is not implemented");
        }
        if (!repositoryService.updateById(new RepositoryPO(repository))) {
            throw new GenericException(ErrorCodeEnum.REPOSITORY_UPDATE_FAILED, repository);
        }
        return ResponseEntity.ok().body(new RepositoryVO(repositoryService.getById(id)));
    }
}
