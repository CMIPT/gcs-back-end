package edu.cmipt.gcs.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import edu.cmipt.gcs.constant.ApiPathConstant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

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
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testCreateUserValid() throws Exception {}
}
