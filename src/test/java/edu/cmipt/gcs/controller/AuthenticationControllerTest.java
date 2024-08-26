package edu.cmipt.gcs.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import edu.cmipt.gcs.constant.ApiPathConstant;
import edu.cmipt.gcs.constant.ValidationConstant;
import edu.cmipt.gcs.util.MessageSourceUtil;

/**
 * Tests for AuthenticationController
 *
 * @author Kaiser
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class AuthenticationControllerTest {
    @Autowired private MockMvc mvc;

    /**
     * Tests for createUser with invalid input
     *
     * @autor Kaiser
     */
    @Test
    public void testSignUpInvalid() throws Exception {
        String user =
                """
                {
                    "name": "test",
                    "email": "invalid email address",
                    "userPassword": ""
                }
                """;
        mvc.perform(
                        MockMvcRequestBuilders.post(ApiPathConstant.AUTHENTICATION_SIGN_UP_API_PATH)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(user))
                .andExpectAll(
                        status().isBadRequest(),
                        jsonPath(
                                "$.email",
                                equalTo(MessageSourceUtil.getMessage("UserDTO.email.Email"))),
                        jsonPath(
                                "$.userPassword",
                                equalTo(
                                        MessageSourceUtil.getMessage(
                                                "UserDTO.userPassword.Size",
                                                ValidationConstant.MIN_PASSWORD_LENGTH,
                                                ValidationConstant.MAX_PASSWORD_LENGTH))),
                        content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    public void testCreateUserValid() throws Exception {}
}
