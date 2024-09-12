package edu.cmipt.gcs.controller;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import edu.cmipt.gcs.constant.ApiPathConstant;
import edu.cmipt.gcs.constant.HeaderParameter;
import edu.cmipt.gcs.constant.TestConstant;
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

/**
 * Tests for AuthenticationController
 *
 * @author Kaiser
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AuthenticationControllerTest {
    @Autowired private MockMvc mvc;

    private static String userDTO =
            """
            {
                "username": "%s",
                "email": "%s",
                "userPassword": "%s"
            }
            """
                    .formatted(
                            TestConstant.USERNAME, TestConstant.EMAIL, TestConstant.USER_PASSWORD);
    private static String userSignInDTO =
            """
            {
                "username": "%s",
                "userPassword": "%s"
            }
            """
                    .formatted(TestConstant.USERNAME, TestConstant.USER_PASSWORD);

    private static String invalidUserDTO =
            """
            {
                "username": "test",
                "email": "invalid email address",
                "userPassword": "123456"
            }
            """;
    private static String invalidUserSignInDTO =
            """
            {
                "username": "%s",
                "userPassword": "%s"
            }
            """
                    .formatted(TestConstant.USERNAME, TestConstant.USER_PASSWORD + "wrong");

    /**
     * Test sign in with invalid user information
     *
     * <p>This must excute before {@link #testSignInValid() testSignInValid}
     *
     * @throws Exception
     */
    @Test
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public void testSignUpValid() throws Exception {
        mvc.perform(
                        post(ApiPathConstant.AUTHENTICATION_SIGN_UP_API_PATH)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(userDTO))
                .andExpect(status().isOk());
    }

    /**
     * Test sign in with valid user information
     *
     * <p>This must excute after {@link #testSignInValid() testSignInValid}, and before {@link
     * #testRefreshValid() testRefreshValid}
     *
     * @throws Exception
     */
    @Test
    @Order(Ordered.HIGHEST_PRECEDENCE + 1)
    public void testSignInValid() throws Exception {
        var response =
                mvc.perform(
                                post(ApiPathConstant.AUTHENTICATION_SIGN_IN_API_PATH)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(userSignInDTO))
                        .andExpectAll(
                                status().isOk(),
                                jsonPath("$.username", is(TestConstant.USERNAME)),
                                jsonPath("$.email", is(TestConstant.EMAIL)),
                                jsonPath("$.id").isNumber(),
                                header().exists(HeaderParameter.ACCESS_TOKEN),
                                header().exists(HeaderParameter.REFRESH_TOKEN))
                        .andReturn()
                        .getResponse();
        TestConstant.ACCESS_TOKEN = response.getHeader(HeaderParameter.ACCESS_TOKEN);
        TestConstant.REFRESH_TOKEN = response.getHeader(HeaderParameter.REFRESH_TOKEN);
    }

    /**
     * Test refresh token with valid refresh token
     *
     * <p>This must excute after {@link #testSignInValid() testSignInValid}
     *
     * @throws Exception
     */
    @Test
    @Order(Ordered.HIGHEST_PRECEDENCE + 2)
    public void testRefreshValid() throws Exception {
        mvc.perform(
                        get(ApiPathConstant.AUTHENTICATION_REFRESH_API_PATH)
                                .header(HeaderParameter.TOKEN, TestConstant.REFRESH_TOKEN))
                .andExpectAll(status().isOk(), header().exists(HeaderParameter.ACCESS_TOKEN));
    }

    @Test
    public void testSignInInvalid() throws Exception {
        mvc.perform(
                        post(ApiPathConstant.AUTHENTICATION_SIGN_IN_API_PATH)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(invalidUserSignInDTO))
                .andExpectAll(
                        status().isBadRequest(),
                        content()
                                .json(
                                        """
                                        {
                                            "code": %d,
                                            "message": "%s"
                                        }
                                        """
                                                .formatted(
                                                        ErrorCodeEnum.WRONG_SIGN_IN_INFORMATION
                                                                .ordinal(),
                                                        MessageSourceUtil.getMessage(
                                                                ErrorCodeEnum
                                                                        .WRONG_SIGN_IN_INFORMATION))));
    }

    @Test
    public void testSignUpInvalid() throws Exception {
        mvc.perform(
                        post(ApiPathConstant.AUTHENTICATION_SIGN_UP_API_PATH)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(invalidUserDTO))
                .andExpectAll(
                        status().isBadRequest(),
                        content()
                                .json(
                                        """
                                        {
                                            "code": %d,
                                            "message": "%s"
                                        }
                                        """
                                                .formatted(
                                                        ErrorCodeEnum.USERDTO_EMAIL_EMAIL.ordinal(),
                                                        MessageSourceUtil.getMessage(
                                                                ErrorCodeEnum
                                                                        .USERDTO_EMAIL_EMAIL))));
    }

    @Test
    public void testRefreshInvalid() throws Exception {
        String invalidToken = "This is a invalid token";
        mvc.perform(
                        get(ApiPathConstant.AUTHENTICATION_REFRESH_API_PATH)
                                .header(HeaderParameter.TOKEN, invalidToken))
                .andExpectAll(
                        status().isUnauthorized(),
                        content()
                                .json(
                                        """
                                        {
                                            "code": %d,
                                            "message": "%s"
                                        }
                                        """
                                                .formatted(
                                                        ErrorCodeEnum.INVALID_TOKEN.ordinal(),
                                                        MessageSourceUtil.getMessage(
                                                                ErrorCodeEnum.INVALID_TOKEN,
                                                                invalidToken))));
    }
}
