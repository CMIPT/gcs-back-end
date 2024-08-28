package edu.cmipt.gcs.controller;

import edu.cmipt.gcs.constant.ApiPathConstant;
import edu.cmipt.gcs.constant.ApplicationConstant;
import edu.cmipt.gcs.enumeration.ErrorCodeEnum;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.annotation.PostConstruct;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * DevelopmentController
 *
 * <p>Controller for development APIs
 *
 * @author Kaiser
 */
@RestController
@Profile(ApplicationConstant.DEV_PROFILE)
@Tag(name = "Development", description = "Some Useful APIs for Development")
public class DevelopmentController {
    private Map<Integer, String> errorCodeConstant = new HashMap<>();

    private Map<String, String> apiPathConstant = new HashMap<>();

    @PostConstruct
    public void init() {
        for (ErrorCodeEnum code : ErrorCodeEnum.values()) {
            if (code == ErrorCodeEnum.ZERO_PLACEHOLDER) { continue; }
            errorCodeConstant.put(code.ordinal(), code.name());
        }
        for (Field field : ApiPathConstant.class.getFields()) {
            try {
                if (field.getName().endsWith("API_PATH")) {
                    apiPathConstant.put(field.getName(), (String) field.get(null));
                }
            } catch (Exception e) {
                // impossible
                throw new RuntimeException(e);
            }
        }
    }

    @GetMapping(ApiPathConstant.DEVELOPMENT_GET_API_MAP_API_PATH)
    @Operation(
            summary = "Get all API paths",
            description = "Get all API paths in the application",
            tags = {"Development", "Get Method"})
    @ApiResponse(responseCode = "200", description = "API paths retrieved successfully")
    public Map<String, String> getApiMap() {
        return apiPathConstant;
    }

    @GetMapping(ApiPathConstant.DEVELOPMENT_GET_ERROR_MESSAGE_API_PATH)
    @Operation(
            summary = "Get all error messages",
            description = "Get all error messages in the application",
            tags = {"Development", "Get Method"})
    @ApiResponse(responseCode = "200", description = "Error messages retrieved successfully")
    public Map<Integer, String> getErrorMessage() {
        return errorCodeConstant;
    }
}
