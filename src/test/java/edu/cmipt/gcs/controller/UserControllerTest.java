package edu.cmipt.gcs.controller;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import edu.cmipt.gcs.util.MessageSourceUtil;
import edu.cmipt.gcs.validation.ConstantProperty;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

/**
 * Tests for UserController
 *
 * @author Kaiser
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class UserControllerTest {

    @Autowired private MockMvc mvc;

    /**
     * Tests for createUser with invalid input
     *
     * @autor Kaiser
     */
    @Test
    public void testCreateUserInvalid() throws Exception {
        String user =
                """
                {
                    "name": "test",
                    "email": "invalid email address",
                    "userPassword": ""
                }
                """;
        mvc.perform(
                        MockMvcRequestBuilders.post("/user")
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
                                                ConstantProperty.MIN_PASSWORD_LENGTH,
                                                ConstantProperty.MAX_PASSWORD_LENGTH))),
                        content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    public void testCreateUserValid() throws Exception {}
}
