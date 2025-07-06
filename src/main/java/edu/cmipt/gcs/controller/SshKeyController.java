package edu.cmipt.gcs.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import edu.cmipt.gcs.constant.ApiPathConstant;
import edu.cmipt.gcs.constant.ApplicationConstant;
import edu.cmipt.gcs.constant.HeaderParameter;
import edu.cmipt.gcs.constant.ValidationConstant;
import edu.cmipt.gcs.enumeration.ErrorCodeEnum;
import edu.cmipt.gcs.enumeration.SshKeyOrderByEnum;
import edu.cmipt.gcs.exception.GenericException;
import edu.cmipt.gcs.pojo.error.ErrorVO;
import edu.cmipt.gcs.pojo.other.PageVO;
import edu.cmipt.gcs.pojo.ssh.SshKeyDTO;
import edu.cmipt.gcs.pojo.ssh.SshKeyPO;
import edu.cmipt.gcs.pojo.ssh.SshKeyVO;
import edu.cmipt.gcs.service.SshKeyService;
import edu.cmipt.gcs.util.JwtUtil;
import edu.cmipt.gcs.util.TypeConversionUtil;
import edu.cmipt.gcs.validation.group.CreateGroup;
import edu.cmipt.gcs.validation.group.UpdateGroup;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.nio.file.Files;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
@Tag(name = "SSH", description = "SSH APIs")
public class SshKeyController {
  private static final Logger logger = LoggerFactory.getLogger(SshKeyController.class);

  @Autowired private SshKeyService sshKeyService;

  @PostMapping(ApiPathConstant.SSH_KEY_UPLOAD_SSH_KEY_API_PATH)
  @Operation(
      summary = "Upload SSH key",
      description = "Upload SSH key with the given information",
      tags = {"SSH", "Post Method"})
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "SSH key uploaded successfully"),
    @ApiResponse(
        description = "SSH key upload failed",
        content = @Content(schema = @Schema(implementation = ErrorVO.class))),
  })
  public void uploadSshKey(
      @Validated(CreateGroup.class) @RequestBody SshKeyDTO sshKeyDTO,
      @RequestHeader(HeaderParameter.ACCESS_TOKEN) String accessToken) {
    checkSshKeyNameValidity(sshKeyDTO.name(), accessToken);
    checkSshKeyPublicKeyValidity(sshKeyDTO.publicKey(), accessToken);
    if (!sshKeyService.save(new SshKeyPO(sshKeyDTO, JwtUtil.getId(accessToken)))) {
      throw new GenericException(ErrorCodeEnum.SSH_KEY_UPLOAD_FAILED, sshKeyDTO);
    }
  }

  @DeleteMapping(ApiPathConstant.SSH_KEY_DELETE_SSH_KEY_API_PATH)
  @Operation(
      summary = "Delete SSH key",
      description = "Delete SSH key with the given information",
      tags = {"SSH", "Delete Method"})
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "SSH key deleted successfully"),
    @ApiResponse(
        description = "SSH key deletion failed",
        content = @Content(schema = @Schema(implementation = ErrorVO.class)))
  })
  public void deleteSshKey(
      @RequestHeader(HeaderParameter.ACCESS_TOKEN) String accessToken,
      @RequestParam("id") Long id) {
    var sshKeyPO = sshKeyService.getById(id);
    if (sshKeyPO == null) {
      throw new GenericException(ErrorCodeEnum.SSH_KEY_NOT_FOUND, id);
    }
    String idInToken = JwtUtil.getId(accessToken);
    if (!idInToken.equals(sshKeyPO.getUserId().toString())) {
      logger.info("User[{}] tried to delete SSH key of user[{}]", idInToken, sshKeyPO.getUserId());
      throw new GenericException(ErrorCodeEnum.ACCESS_DENIED);
    }
    if (!sshKeyService.removeById(id)) {
      throw new GenericException(ErrorCodeEnum.SSH_KEY_DELETE_FAILED, id);
    }
  }

  @PostMapping(ApiPathConstant.SSH_KEY_UPDATE_SSH_KEY_API_PATH)
  @Operation(
      summary = "Update SSH key",
      description = "Update SSH key with the given information",
      tags = {"SSH", "Post Method"})
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "SSH key updated successfully"),
    @ApiResponse(
        description = "SSH key update failed",
        content = @Content(schema = @Schema(implementation = ErrorVO.class)))
  })
  public void updateSshKey(
      @Validated(UpdateGroup.class) @RequestBody SshKeyDTO sshKeyDTO,
      @RequestHeader(HeaderParameter.ACCESS_TOKEN) String accessToken) {
    Long id = TypeConversionUtil.convertToLong(sshKeyDTO.id(),true);
    var sshKeyPO = sshKeyService.getById(id);
    if (sshKeyPO == null) {
      throw new GenericException(ErrorCodeEnum.SSH_KEY_NOT_FOUND, id);
    }
    String idInToken = JwtUtil.getId(accessToken);
    if (!idInToken.equals(sshKeyPO.getUserId().toString())) {
      logger.info("User[{}] tried to update SSH key of user[{}]", idInToken, sshKeyPO.getUserId());
      throw new GenericException(ErrorCodeEnum.ACCESS_DENIED);
    }
    if (sshKeyDTO.name() != null) {
      checkSshKeyNameValidity(sshKeyDTO.name(), accessToken);
    }
    if (sshKeyDTO.publicKey() != null) {
      checkSshKeyPublicKeyValidity(sshKeyDTO.publicKey(), accessToken);
    }
    if (!sshKeyService.updateById(new SshKeyPO(sshKeyDTO))) {
      throw new GenericException(ErrorCodeEnum.SSH_KEY_UPDATE_FAILED, sshKeyDTO);
    }
  }

  @GetMapping(ApiPathConstant.SSH_KEY_PAGE_SSH_KEY_API_PATH)
  @Operation(
      summary = "Page SSH key",
      description = "Page SSH key with the given access token",
      tags = {"SSH", "Get Method"})
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "SSH key paged successfully"),
    @ApiResponse(
        description = "SSH key page failed",
        content = @Content(schema = @Schema(implementation = ErrorVO.class)))
  })
  public PageVO<SshKeyVO> pageSshKey(
      @RequestHeader(HeaderParameter.ACCESS_TOKEN) String accessToken,
      @RequestParam("page") @Min(1) Integer page,
      @RequestParam("size") @Min(1) Integer size,
      @RequestParam("orderBy") SshKeyOrderByEnum orderBy,
      @RequestParam("isAsc") Boolean isAsc) {
    if (1L * page * size > ApplicationConstant.MAX_PAGE_TOTAL_COUNT) {
      throw new GenericException(ErrorCodeEnum.ACCESS_DENIED);
    }
    Long idInToken = TypeConversionUtil.convertToLong(JwtUtil.getId(accessToken),true);
    var wrapper = new QueryWrapper<SshKeyPO>();
    wrapper.eq("user_id", idInToken);
    wrapper.orderBy(true, isAsc, orderBy.getFieldName());
    var iPage = sshKeyService.page(new Page<>(page, size), wrapper);
    return new PageVO<>(iPage.getTotal(), iPage.getRecords().stream().map(SshKeyVO::new).toList());
  }

  @GetMapping(ApiPathConstant.SSH_KEY_CHECK_SSH_KEY_NAME_VALIDITY_API_PATH)
  @Operation(
      summary = "Check SSH key name validity",
      description = "Check SSH key name validity with the given information",
      tags = {"SSH", "Get Method"})
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "SSH key name is valid"),
    @ApiResponse(
        responseCode = "400",
        description = "SSH key name is invalid",
        content = @Content(schema = @Schema(implementation = ErrorVO.class)))
  })
  public void checkSshKeyNameValidity(
      @RequestParam("name")
          @Size(
              min = ValidationConstant.MIN_SSH_KEY_NAME_LENGTH,
              max = ValidationConstant.MAX_SSH_KEY_NAME_LENGTH,
              message = "{Size.sshKeyController#checkSshKeyNameValidity.name}")
          @NotBlank(message = "{NotBlank.sshKeyController#checkSshKeyNameValidity.name}")
          String name,
      @RequestHeader(HeaderParameter.ACCESS_TOKEN) String accessToken) {
    Long idInToken = TypeConversionUtil.convertToLong(JwtUtil.getId(accessToken),true);
    if (sshKeyService.getOneByUserIdAndName(idInToken, name) != null) {
      throw new GenericException(ErrorCodeEnum.SSH_KEY_NAME_ALREADY_EXISTS, name);
    }
  }

  @GetMapping(ApiPathConstant.SSH_KEY_CHECK_SSH_KEY_PUBLIC_KEY_VALIDITY_API_PATH)
  @Operation(
      summary = "Check SSH key public key validity",
      description = "Check SSH key public key validity with the given information",
      tags = {"SSH", "Get Method"})
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "SSH key public key is valid"),
    @ApiResponse(
        responseCode = "400",
        description = "SSH key public key is invalid",
        content = @Content(schema = @Schema(implementation = ErrorVO.class)))
  })
  public void checkSshKeyPublicKeyValidity(
      @RequestParam("publicKey")
          @Size(
              min = ValidationConstant.MIN_SSH_KEY_PUBLIC_KEY_LENGTH,
              max = ValidationConstant.MAX_SSH_KEY_PUBLIC_KEY_LENGTH,
              message = "{Size.sshKeyController#checkSshKeyPublicKeyValidity.publicKey}")
          @NotBlank(message = "{NotBlank.sshKeyController#checkSshKeyPublicKeyValidity.publicKey}")
          String publicKey,
      @RequestHeader(HeaderParameter.ACCESS_TOKEN) String accessToken) {
    boolean ok = true;
    try {
      Path tempFile = Files.createTempFile(String.valueOf(System.currentTimeMillis()), ".pub");
      Files.writeString(tempFile, publicKey);
      ProcessBuilder processBuilder = new ProcessBuilder("ssh-keygen", "-lf", tempFile.toString());
      Process process = processBuilder.start();
      if (process.waitFor() != 0) {
        ok = false;
      }
      tempFile.toFile().delete();
    } catch (Exception e) {
      logger.error(e.getMessage());
      throw new GenericException(ErrorCodeEnum.SERVER_ERROR);
    }
    if (!ok) {
      throw new GenericException(ErrorCodeEnum.SSH_KEY_PUBLIC_KEY_INVALID, publicKey);
    }
    Long idInToken = TypeConversionUtil.convertToLong(JwtUtil.getId(accessToken),true);
    if (sshKeyService.getOneByUserIdAndPublicKey(idInToken, publicKey) != null) {
      throw new GenericException(ErrorCodeEnum.SSH_KEY_PUBLIC_KEY_ALREADY_EXISTS, publicKey);
    }
  }
}
