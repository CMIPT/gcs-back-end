package edu.cmipt.gcs.controller;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Date;

import edu.cmipt.gcs.constant.ApiPathConstant;
import edu.cmipt.gcs.constant.HeaderParameter;
import edu.cmipt.gcs.enumeration.ErrorCodeEnum;
import edu.cmipt.gcs.util.MessageSourceUtil;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.Ordered;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.jayway.jsonpath.JsonPath;

/**
 * Tests for AuthenticationController
 *
 * @author Kaiser
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AuthenticationControllerTest {
    @Autowired
    private MockMvc mvc;

    private static String username = new Date().getTime() + "";
    private static String userPassword = "123456";
    private static String email = username + "@cmipt.edu";
    private static String userDTO = """
            {
                "username": "%s",
                "email": "%s",
                "userPassword": "%s"
            }
            """.formatted(username, email, userPassword);
    private static String userSignInDTO = """
            {
                "username": "%s",
                "userPassword": "%s"
            }
            """.formatted(username, userPassword);
    private static String accessToken;
    private static String refreshToken;

    private static String invalidUserDTO = """
            {
                "username": "test",
                "email": "invalid email address",
                "userPassword": "123456"
            }
            """;
    private static String invalidUserSignInDTO = """
            {
                "username": "%s",
                "userPassword": "%s"
            }
            """.formatted(username, userPassword + "wrong");

    /**
     * Test sign in with invalid user information
     *
     * This must excute before {@link #testSignInValid() testSignInValid}
     *
     * @throws Exception
     */
    @Test
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public void testSignUpValid() throws Exception {
        mvc.perform(MockMvcRequestBuilders.post(ApiPathConstant.AUTHENTICATION_SIGN_UP_API_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(userDTO))
                .andExpect(status().isOk());
    }

    /**
     * Test sign in with valid user information
     *
     * This must excute after {@link #testSignInValid() testSignInValid}, and
     * before {@link #testRefreshValid() testRefreshValid}
     *
     * @throws Exception
     */
    @Test
    @Order(Ordered.HIGHEST_PRECEDENCE + 1)
    public void testSignInValid() throws Exception {
        String jsonResponse = mvc.perform(MockMvcRequestBuilders.post(ApiPathConstant.AUTHENTICATION_SIGN_IN_API_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(userSignInDTO))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.username", is(username)),
                        jsonPath("$.email", is(email)),
                        jsonPath("$.id").isNumber(),
                        jsonPath("$.accessToken").isString(),
                        jsonPath("$.refreshToken").isString())
                .andReturn().getResponse().getContentAsString();
        accessToken = JsonPath.read(jsonResponse, "$.accessToken");
        refreshToken = JsonPath.read(jsonResponse, "$.refreshToken");
    }

    /**
     * Test refresh token with valid refresh token
     *
     * This must excute after {@link #testSignInValid() testSignInValid}
     *
     * @throws Exception
     */
    @Test
    @Order(Ordered.HIGHEST_PRECEDENCE + 2)
    public void testRefreshValid() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get(ApiPathConstant.AUTHENTICATION_REFRESH_API_PATH)
                .header(HeaderParameter.TOKEN, refreshToken))
                .andExpect(status().isOk());
    }

    @Test
    public void testSignInInvalid() throws Exception {
        mvc.perform(MockMvcRequestBuilders.post(ApiPathConstant.AUTHENTICATION_SIGN_IN_API_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidUserSignInDTO))
                .andExpectAll(
                        status().isBadRequest(),
                        content().json(
                                """
                                        {
                                            "code": %d,
                                            "message": "%s"
                                        }
                                        """.formatted(ErrorCodeEnum.WRONG_SIGN_IN_INFORMATION.ordinal(),
                                        MessageSourceUtil.getMessage(ErrorCodeEnum.WRONG_SIGN_IN_INFORMATION))));
    }

    @Test
    public void testSignUpInvalid() throws Exception {
        mvc.perform(
                MockMvcRequestBuilders.post(ApiPathConstant.AUTHENTICATION_SIGN_UP_API_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidUserDTO))
                .andExpectAll(
                        status().isBadRequest(),
                        content().json(
                                """
                                        {
                                            "code": %d,
                                            "message": "%s"
                                        }
                                        """.formatted(ErrorCodeEnum.USERDTO_EMAIL_EMAIL.ordinal(),
                                        MessageSourceUtil.getMessage(ErrorCodeEnum.USERDTO_EMAIL_EMAIL))));
    }

    @Test
    public void testRefreshInvalid() throws Exception {
        String invalidToken = "This is a invalid token";
        mvc.perform(MockMvcRequestBuilders.get(ApiPathConstant.AUTHENTICATION_REFRESH_API_PATH)
                .header(HeaderParameter.TOKEN, invalidToken))
                .andExpectAll(
                        status().isBadRequest(),
                        content().json(
                                """
                                        {
                                            "code": %d,
                                            "message": "%s"
                                        }
                                        """.formatted(ErrorCodeEnum.INVALID_TOKEN.ordinal(),
                                        MessageSourceUtil.getMessage(ErrorCodeEnum.INVALID_TOKEN, invalidToken))));
    }
}
