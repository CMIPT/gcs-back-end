package edu.cmipt.gcs.controller;

import edu.cmipt.gcs.constant.ApiPathConstant;
import edu.cmipt.gcs.constant.ApplicationConstant;
import edu.cmipt.gcs.enumeration.ErrorCodeEnum;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    private static final Logger logger = LoggerFactory.getLogger(DevelopmentController.class);

    private Map<String, Integer> errorCodeConstant = new HashMap<>();

    private Map<String, String> apiPathConstant = new HashMap<>();

    private Map<String, String> voAsTS = new HashMap<>();

    @PostConstruct
    public void init() {
        for (ErrorCodeEnum code : ErrorCodeEnum.values()) {
            if (code == ErrorCodeEnum.ZERO_PLACEHOLDER) {
                continue;
            }
            errorCodeConstant.put(code.name(), code.ordinal());
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
        List<Class<?>> voClassList = findVOClasses();
        for (Class<?> voClass : voClassList) {
            StringBuilder tsDefinitions = new StringBuilder();
            tsDefinitions.append("export type ").append(voClass.getSimpleName());
            TypeVariable<?>[] typeParameters = voClass.getTypeParameters();
            if (typeParameters.length > 0) {
                tsDefinitions.append("<");
                for (int i = 0; i < typeParameters.length; i++) {
                    tsDefinitions.append(typeParameters[i].getName());
                    if (i < typeParameters.length - 1) {
                        tsDefinitions.append(", ");
                    }
                }
                tsDefinitions.append(">");
            }
            tsDefinitions.append(" = {\n");
            for (Field field : voClass.getDeclaredFields()) {
                if (!Modifier.isStatic(field.getModifiers())) {
                    tsDefinitions
                            .append("  ")
                            .append(field.getName())
                            .append(": ")
                            .append(mapJavaTypeToTypeScript(field))
                            .append(";\n");
                }
            }
            tsDefinitions.append("}");
            voAsTS.put(voClass.getSimpleName(), tsDefinitions.toString());
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
    public Map<String, Integer> getErrorMessage() {
        return errorCodeConstant;
    }

    @GetMapping(ApiPathConstant.DEVELOPMENT_GET_VO_AS_TS_API_PATH)
    @Operation(
            summary = "Get VO as TypeScript",
            description = "Get VO as TypeScript in the application",
            tags = {"Development", "Get Method"})
    @Parameter(
            name = "voName",
            description = "Value Object Name, when not provided, all VOs will be returned",
            required = false,
            example = "PageVO",
            schema = @Schema(implementation = String.class))
    @ApiResponse(responseCode = "200", description = "VO as TypeScript retrieved successfully")
    public String getVOAsTS(@RequestParam(required = false) String voName) {
        if (voName == null) {
            return voAsTS.values().stream().reduce("", (a, b) -> a + "\n" + b);
        } else {
            if (voAsTS.containsKey(voName)) {
                return voAsTS.get(voName);
            } else {
                return "No such VO found";
            }
        }
    }

    private List<Class<?>> findVOClasses() {
        List<Class<?>> classes = new ArrayList<>();
        try {
            Files.walk(
                            Paths.get(
                                    getClass()
                                            .getClassLoader()
                                            .getResource("edu/cmipt/gcs/pojo")
                                            .toURI()))
                    .filter(Files::isRegularFile)
                    .forEach(
                            file -> {
                                String fileName = file.getFileName().toString();
                                if (fileName.endsWith("VO.class")) {
                                    String className =
                                            file.toString()
                                                    .replace(
                                                            getClass()
                                                                    .getClassLoader()
                                                                    .getResource("")
                                                                    .getPath(),
                                                            "")
                                                    .replace("/", ".")
                                                    .replace(".class", "");
                                    if (className.startsWith(".")) {
                                        className = className.substring(1);
                                    }
                                    logger.debug("Find VO class: {}", className);
                                    try {
                                        classes.add(Class.forName(className));
                                    } catch (ClassNotFoundException e) {
                                        logger.debug("Class not found: {}", className);
                                    }
                                }
                            });
        } catch (Exception e) {
            logger.debug("Error while finding VO classes: {}", e.getMessage());
        }
        if (classes.isEmpty()) {
            logger.debug("No VO class found");
        }
        return classes;
    }

    private String mapJavaTypeToTypeScript(Field field) {
        Type genericType = field.getGenericType();
        if (genericType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) genericType;
            Class<?> rawType = (Class<?>) parameterizedType.getRawType();
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            if (rawType.equals(List.class) && actualTypeArguments.length == 1) {
                return "Array<" + mapJavaTypeToTypeScript(actualTypeArguments[0]) + ">";
            }
        }
        return mapJavaTypeToTypeScript(field.getType());
    }

    private String mapJavaTypeToTypeScript(Type javaType) {
        if (javaType.equals(String.class)) {
            return "string";
        } else if (javaType.equals(int.class)
                || javaType.equals(Integer.class)
                || javaType.equals(long.class)
                || javaType.equals(Long.class)
                || javaType.equals(double.class)
                || javaType.equals(Double.class)
                || javaType.equals(float.class)
                || javaType.equals(Float.class)) {
            return "number";
        } else if (javaType.equals(boolean.class) || javaType.equals(Boolean.class)) {
            return "boolean";
        } else if (javaType instanceof TypeVariable) {
            return ((TypeVariable<?>) javaType).getName();
        } else {
            return "any"; // Default to 'any' for complex types
        }
    }
}
