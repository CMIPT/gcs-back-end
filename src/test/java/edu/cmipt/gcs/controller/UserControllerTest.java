package edu.cmipt.gcs.controller;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
}
