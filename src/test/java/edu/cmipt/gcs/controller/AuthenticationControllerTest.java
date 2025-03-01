package edu.cmipt.gcs.controller;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.cmipt.gcs.constant.ApiPathConstant;
import edu.cmipt.gcs.constant.ApplicationConstant;
import edu.cmipt.gcs.constant.HeaderParameter;
import edu.cmipt.gcs.constant.TestConstant;
import edu.cmipt.gcs.enumeration.ErrorCodeEnum;
import edu.cmipt.gcs.pojo.user.UserVO;
import edu.cmipt.gcs.util.EmailVerificationCodeUtil;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Tests for AuthenticationController
 *
 * @author Kaiser
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles({ApplicationConstant.TEST_PROFILE})
public class AuthenticationControllerTest {
    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper objectMapper;

    @Test
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public void testCreateUserValid() throws Exception {
        String userCreateDTO =
                """
                {
                    "username": "%s",
                    "email": "%s",
                    "userPassword": "%s",
                    "emailVerificationCode": "%s"
                }
                """
                        .formatted(
                                TestConstant.USERNAME,
                                TestConstant.EMAIL,
                                TestConstant.USER_PASSWORD,
                                EmailVerificationCodeUtil.generateVerificationCode(
                                        TestConstant.EMAIL));
        String otherUserCreateDTO =
                """
                {
                    "username": "%s",
                    "email": "%s",
                    "userPassword": "%s",
                    "emailVerificationCode": "%s"
                }
                """
                        .formatted(
                                TestConstant.OTHER_USERNAME,
                                TestConstant.OTHER_EMAIL,
                                TestConstant.OTHER_USER_PASSWORD,
                                EmailVerificationCodeUtil.generateVerificationCode(
                                        TestConstant.OTHER_EMAIL));
        mvc.perform(
                        post(ApiPathConstant.USER_CREATE_USER_API_PATH)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(userCreateDTO))
                .andExpect(status().isOk());
        mvc.perform(
                        post(ApiPathConstant.USER_CREATE_USER_API_PATH)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(otherUserCreateDTO))
                .andExpect(status().isOk());
    }

    @Test
    public void testCreateUserInvalid() throws Exception {
        String invalidUserCreateDTO =
                """
                {
                    "username": "test",
                    "email": "invalid email address",
                    "userPassword": "123456"
                }
                """;
        mvc.perform(
                        post(ApiPathConstant.USER_CREATE_USER_API_PATH)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(invalidUserCreateDTO))
                .andExpectAll(
                        status().isBadRequest(),
                        jsonPath("$.code", is(ErrorCodeEnum.VALIDATION_ERROR.ordinal())));
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
        String userSignInDTO =
                """
                {
                    "username": "%s",
                    "userPassword": "%s"
                }
                """
                        .formatted(TestConstant.USERNAME, TestConstant.USER_PASSWORD);
        String otherUserSignInDTO =
                """
                {
                    "username": "%s",
                    "userPassword": "%s"
                }
                """
                        .formatted(TestConstant.OTHER_USERNAME, TestConstant.OTHER_USER_PASSWORD);

        var response =
                mvc.perform(
                                post(ApiPathConstant.AUTHENTICATION_SIGN_IN_API_PATH)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(userSignInDTO))
                        .andExpectAll(
                                status().isOk(),
                                jsonPath("$.username", is(TestConstant.USERNAME)),
                                jsonPath("$.email", is(TestConstant.EMAIL)),
                                jsonPath("$.id").isString(),
                                header().exists(HeaderParameter.ACCESS_TOKEN),
                                header().exists(HeaderParameter.REFRESH_TOKEN))
                        .andReturn()
                        .getResponse();
        TestConstant.ACCESS_TOKEN = response.getHeader(HeaderParameter.ACCESS_TOKEN);
        TestConstant.REFRESH_TOKEN = response.getHeader(HeaderParameter.REFRESH_TOKEN);
        TestConstant.ID = objectMapper.readValue(response.getContentAsString(), UserVO.class).id();
        var otherResponse =
                mvc.perform(
                                post(ApiPathConstant.AUTHENTICATION_SIGN_IN_API_PATH)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(otherUserSignInDTO))
                        .andExpectAll(
                                status().isOk(),
                                jsonPath("$.username", is(TestConstant.OTHER_USERNAME)),
                                jsonPath("$.email", is(TestConstant.OTHER_EMAIL)),
                                jsonPath("$.id").isString(),
                                header().exists(HeaderParameter.ACCESS_TOKEN),
                                header().exists(HeaderParameter.REFRESH_TOKEN))
                        .andReturn()
                        .getResponse();
        TestConstant.OTHER_ID =
                objectMapper.readValue(otherResponse.getContentAsString(), UserVO.class).id();
        TestConstant.OTHER_ACCESS_TOKEN = otherResponse.getHeader(HeaderParameter.ACCESS_TOKEN);
        TestConstant.OTHER_REFRESH_TOKEN = otherResponse.getHeader(HeaderParameter.REFRESH_TOKEN);
    }

    /**
     * Test sign in with invalid user information
     *
     * <p>This must excute before {@link #testSignInValid() testSignInValid}
     *
     * @throws Exception
     */
    @Test
    public void testSignInInvalid() throws Exception {
        String invalidUserSignInDTO =
                """
                {
                    "username": "%s",
                    "userPassword": "%s"
                }
                """
                        .formatted(TestConstant.USERNAME, TestConstant.USER_PASSWORD + "wrong");

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
                                .header(HeaderParameter.REFRESH_TOKEN, TestConstant.REFRESH_TOKEN))
                .andExpectAll(status().isOk(), header().exists(HeaderParameter.ACCESS_TOKEN));
    }

    @Test
    public void testRefreshInvalid() throws Exception {
        String invalidToken = "This is an invalid token";
        mvc.perform(
                        get(ApiPathConstant.AUTHENTICATION_REFRESH_API_PATH)
                                .header(HeaderParameter.REFRESH_TOKEN, invalidToken))
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

    @Test
    public void testSignOutValid() throws Exception {
        mvc.perform(
                        delete(ApiPathConstant.AUTHENTICATION_SIGN_OUT_API_PATH)
                                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN))
                .andExpectAll(status().isOk());
        // Sign in again to make the information consistent
        testSignInValid();
    }
}
