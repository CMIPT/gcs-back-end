package edu.cmipt.gcs.controller;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Date;

import edu.cmipt.gcs.constant.ApiPathConstant;
import edu.cmipt.gcs.constant.HeaderParameter;
import edu.cmipt.gcs.constant.TestConstant;
import edu.cmipt.gcs.enumeration.ErrorCodeEnum;
import edu.cmipt.gcs.util.MessageSourceUtil;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Tests for UserController
 *
 * @author Kaiser
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class UserControllerTest {
    @Autowired private MockMvc mvc;

    @Test
    public void testGetUserByNameValid() throws Exception {
        mvc.perform(
                        get(ApiPathConstant.USER_API_PREFIX + "/" + TestConstant.USERNAME)
                                .header(HeaderParameter.TOKEN, TestConstant.ACCESS_TOKEN))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.username", is(TestConstant.USERNAME)),
                        jsonPath("$.email", is(TestConstant.EMAIL)),
                        jsonPath("$.id").isNumber());
    }

    @Test
    public void testGetUserByNameInvalid() throws Exception {
        String invalidUsername = TestConstant.USERNAME + "invalid";
        mvc.perform(
                        get(ApiPathConstant.USER_API_PREFIX + "/" + invalidUsername)
                                .header(HeaderParameter.TOKEN, TestConstant.ACCESS_TOKEN))
                .andExpectAll(
                        status().isNotFound(),
                        content()
                                .json(
                                        """
                                        {
                                            "code": %d,
                                            "message": "%s"
                                        }
                                        """
                                                .formatted(
                                                        ErrorCodeEnum.USER_NOT_FOUND.ordinal(),
                                                        MessageSourceUtil.getMessage(
                                                                ErrorCodeEnum.USER_NOT_FOUND,
                                                                invalidUsername))));
    }

    @Test
    public void testCheckEmailValidityExists() throws Exception {
        mvc.perform(
                        get(ApiPathConstant.USER_CHECK_EMAIL_VALIDITY_API_PATH)
                                .param("email", TestConstant.EMAIL))
            .andExpectAll(status().isBadRequest(), content().json(
                """
                {
                    "code": %d,
                    "message": "%s"
                }
                """.formatted(
                    ErrorCodeEnum.EMAIL_ALREADY_EXISTS.ordinal(),
                    MessageSourceUtil.getMessage(ErrorCodeEnum.EMAIL_ALREADY_EXISTS, TestConstant.EMAIL)
                )
            ));
    }

    @Test
    public void testCheckEmailValidityValid() throws Exception {
        mvc.perform(
                        get(ApiPathConstant.USER_CHECK_EMAIL_VALIDITY_API_PATH)
                                .param("email", new Date().getTime() + "@cmipt.edu"))
            .andExpectAll(status().isOk());
    }

    @Test
    public void testCheckUsernameValidityExists() throws Exception {
        mvc.perform(
                        get(ApiPathConstant.USER_CHECK_USERNAME_VALIDITY_API_PATH)
                                .param("username", TestConstant.USERNAME))
            .andExpectAll(status().isBadRequest(), content().json(
                """
                {
                    "code": %d,
                    "message": "%s"
                }
                """.formatted(
                    ErrorCodeEnum.USERNAME_ALREADY_EXISTS.ordinal(),
                    MessageSourceUtil.getMessage(ErrorCodeEnum.USERNAME_ALREADY_EXISTS, TestConstant.USERNAME)
                )
            ));
    }

    @Test
    public void testCheckUsernameValidityValid() throws Exception {
        mvc.perform(
                        get(ApiPathConstant.USER_CHECK_USERNAME_VALIDITY_API_PATH)
                                .param("username", new Date().getTime() + ""))
            .andExpectAll(status().isOk());
    }
}
