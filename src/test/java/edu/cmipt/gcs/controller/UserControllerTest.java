package edu.cmipt.gcs.controller;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import edu.cmipt.gcs.constant.ApiPathConstant;
import edu.cmipt.gcs.constant.ApplicationConstant;
import edu.cmipt.gcs.constant.HeaderParameter;
import edu.cmipt.gcs.constant.TestConstant;
import edu.cmipt.gcs.enumeration.ErrorCodeEnum;
import edu.cmipt.gcs.enumeration.UserQueryTypeEnum;
import edu.cmipt.gcs.util.EmailVerificationCodeUtil;
import edu.cmipt.gcs.util.MessageSourceUtil;
import java.util.Date;
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
 * Tests for UserController
 *
 * @author Kaiser
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles({ApplicationConstant.TEST_PROFILE})
public class UserControllerTest {
  @Autowired private MockMvc mvc;

  @Test
  public void testGetUserValid() throws Exception {
    mvc.perform(
            get(ApiPathConstant.USER_GET_USER_API_PATH)
                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                .param("user", TestConstant.USERNAME)
                .param("userType", UserQueryTypeEnum.USERNAME.name()))
        .andExpectAll(
            status().isOk(),
            jsonPath("$.username", is(TestConstant.USERNAME)),
            jsonPath("$.email", is(TestConstant.EMAIL)),
            jsonPath("$.id").isString());
  }

  @Test
  public void testGetUserInvalid() throws Exception {
    String invalidUsername = TestConstant.USERNAME + "invalid";
    mvc.perform(
            get(ApiPathConstant.USER_GET_USER_API_PATH)
                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                .param("user", invalidUsername)
                .param("userType", UserQueryTypeEnum.USERNAME.name()))
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
                                ErrorCodeEnum.USER_NOT_FOUND, invalidUsername))));
  }

  @Test
  public void testUpdateUserValid() throws Exception {
    mvc.perform(
            post(ApiPathConstant.USER_UPDATE_USER_API_PATH)
                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "avatarUrl": "%s"
                    }
                    """
                        .formatted(TestConstant.AVATAR_URL)))
        .andExpectAll(status().isOk());
  }

  @Test
  public void testUpdateUserInvalid() throws Exception {
    mvc.perform(
            post(ApiPathConstant.USER_UPDATE_USER_API_PATH)
                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "avatarUrl": "this is not a url"
                    }
                    """
                        .formatted(TestConstant.AVATAR_URL)))
        .andExpectAll(
            status().isBadRequest(),
            jsonPath("$.code", is(ErrorCodeEnum.VALIDATION_ERROR.ordinal())),
            jsonPath("$.message", startsWith("Validation error")));
  }

  @Test
  public void testUpdateUserPasswordValid() throws Exception {
    mvc.perform(
            post(ApiPathConstant.USER_UPDATE_USER_PASSWORD_WITH_OLD_PASSWORD_API_PATH)
                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "oldPassword": "%s",
                        "newPassword": "%s"
                    }
                    """
                        .formatted(TestConstant.USER_PASSWORD, TestConstant.USER_PASSWORD + "new")))
        .andExpectAll(status().isOk());
    TestConstant.USER_PASSWORD += "new";
    String userSignInDTO =
        """
        {
            "username": "%s",
            "userPassword": "%s"
        }
        """
            .formatted(TestConstant.USERNAME, TestConstant.USER_PASSWORD);
    // get the new tokens
    var response =
        mvc.perform(
                post(ApiPathConstant.AUTHENTICATION_SIGN_IN_API_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(userSignInDTO))
            .andReturn()
            .getResponse();
    TestConstant.ACCESS_TOKEN = response.getHeader(HeaderParameter.ACCESS_TOKEN);
    TestConstant.REFRESH_TOKEN = response.getHeader(HeaderParameter.REFRESH_TOKEN);
  }

  @Test
  public void testUpdateUserPasswordInvalid() throws Exception {
    mvc.perform(
            post(ApiPathConstant.USER_UPDATE_USER_PASSWORD_WITH_OLD_PASSWORD_API_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                .content(
                    """
                    {
                        "oldPassword": "%s",
                        "newPassword": "%s"
                    }
                    """
                        .formatted(
                            TestConstant.USER_PASSWORD + "wrong",
                            TestConstant.USER_PASSWORD + "new")))
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
                            ErrorCodeEnum.WRONG_UPDATE_PASSWORD_INFORMATION.ordinal(),
                            MessageSourceUtil.getMessage(
                                ErrorCodeEnum.WRONG_UPDATE_PASSWORD_INFORMATION))));
  }

  @Test
  public void testResetUserPasswordValid() throws Exception {
    mvc.perform(
            post(ApiPathConstant.USER_UPDATE_USER_PASSWORD_WITH_EMAIL_VERIFICATION_CODE_API_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "email": "%s",
                        "emailVerificationCode": "%s",
                        "newPassword": "%s"
                    }
                    """
                        .formatted(
                            TestConstant.EMAIL,
                            EmailVerificationCodeUtil.generateVerificationCode(TestConstant.EMAIL),
                            TestConstant.USER_PASSWORD + "new")))
        .andExpectAll(status().isOk());
    TestConstant.USER_PASSWORD += "new";
    String userSignInDTO =
        """
        {
            "username": "%s",
            "userPassword": "%s"
        }
        """
            .formatted(TestConstant.USERNAME, TestConstant.USER_PASSWORD);
    // get the new tokens
    var response =
        mvc.perform(
                post(ApiPathConstant.AUTHENTICATION_SIGN_IN_API_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(userSignInDTO))
            .andReturn()
            .getResponse();
    TestConstant.ACCESS_TOKEN = response.getHeader(HeaderParameter.ACCESS_TOKEN);
    TestConstant.REFRESH_TOKEN = response.getHeader(HeaderParameter.REFRESH_TOKEN);
  }

  @Test
  public void testResetUserPasswordInvalid() throws Exception {
    mvc.perform(
            post(ApiPathConstant.USER_UPDATE_USER_PASSWORD_WITH_EMAIL_VERIFICATION_CODE_API_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "email": "%s",
                        "emailVerificationCode": "%s",
                        "newPassword": "%s"
                    }
                    """
                        .formatted(
                            TestConstant.EMAIL, "123456", TestConstant.USER_PASSWORD + "new")))
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
                            ErrorCodeEnum.INVALID_EMAIL_VERIFICATION_CODE.ordinal(),
                            MessageSourceUtil.getMessage(
                                ErrorCodeEnum.INVALID_EMAIL_VERIFICATION_CODE, "123456"))));
  }

  @Test
  @Order(Ordered.LOWEST_PRECEDENCE)
  public void testDeleteUserValid() throws Exception {
    mvc.perform(
            delete(ApiPathConstant.USER_DELETE_USER_API_PATH)
                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                .header(HeaderParameter.REFRESH_TOKEN, TestConstant.REFRESH_TOKEN))
        .andExpectAll(status().isNotImplemented());
  }

  @Test
  public void testCheckEmailValidityExists() throws Exception {
    mvc.perform(
            get(ApiPathConstant.USER_CHECK_EMAIL_VALIDITY_API_PATH)
                .param("email", TestConstant.EMAIL))
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
                            ErrorCodeEnum.EMAIL_ALREADY_EXISTS.ordinal(),
                            MessageSourceUtil.getMessage(
                                ErrorCodeEnum.EMAIL_ALREADY_EXISTS, TestConstant.EMAIL))));
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
                            ErrorCodeEnum.USERNAME_ALREADY_EXISTS.ordinal(),
                            MessageSourceUtil.getMessage(
                                ErrorCodeEnum.USERNAME_ALREADY_EXISTS, TestConstant.USERNAME))));
  }

  @Test
  public void testCheckUsernameValidityValid() throws Exception {
    mvc.perform(
            get(ApiPathConstant.USER_CHECK_USERNAME_VALIDITY_API_PATH)
                .param("username", new Date().getTime() + ""))
        .andExpectAll(status().isOk());
  }

  @Test
  public void testCheckPasswordValidityValid() throws Exception {
    mvc.perform(
            get(ApiPathConstant.USER_CHECK_USER_PASSWORD_VALIDITY_API_PATH)
                .param("userPassword", "123456"))
        .andExpectAll(status().isOk());
  }

  @Test
  public void testCheckPasswordValidityInvalid() throws Exception {
    mvc.perform(
            get(ApiPathConstant.USER_CHECK_USER_PASSWORD_VALIDITY_API_PATH)
                .param("userPassword", "???!!!!"))
        .andExpectAll(status().isBadRequest());
  }
}
