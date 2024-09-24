package edu.cmipt.gcs.controller;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import edu.cmipt.gcs.constant.ApiPathConstant;
import edu.cmipt.gcs.constant.HeaderParameter;
import edu.cmipt.gcs.constant.TestConstant;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.Ordered;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SshKeyControllerTest {
    @Autowired MockMvc mockMvc;

    @Test
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public void testUploadSshKeyValid() throws Exception {
        for (int i = 0; i < TestConstant.SSH_KEY_SIZE; i++) {
            String name = "My SSH Key " + i;
            String publicKey = "This is my public key " + i;
            mockMvc
                    .perform(
                            post(ApiPathConstant.SSH_KEY_UPLOAD_SSH_KEY_API_PATH)
                                    .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {
                            "name": "%s",
                            "userId": "%s",
                            "publicKey": "%s"
                        }
                        """.formatted(name, TestConstant.ID, publicKey)
                    ))
                    .andExpect(status().isOk());
        }
    }

    @Test
    @Order(Ordered.HIGHEST_PRECEDENCE + 1)
    public void testPageSshKeyValid() throws Exception {
        var response = mockMvc
                .perform(
                        get(ApiPathConstant.SSH_KEY_PAGE_SSH_KEY_API_PATH)
                                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                                .param("id", TestConstant.ID)
                                .param("page", "1")
                                .param("size", TestConstant.SSH_KEY_SIZE.toString()))
                .andExpectAll(status().isOk(),
            jsonPath("$").isArray(),
            jsonPath("$.length()").value(TestConstant.SSH_KEY_SIZE)).andReturn().getResponse();
        Matcher matcher = Pattern.compile("id=(\\d+)").matcher(JsonParserFactory.getJsonParser().parseList(response.getContentAsString()).get(0).toString());
        matcher.find();
        TestConstant.SSH_KEY_ID = matcher.group(1);
    }

    @Test
    @Order(Ordered.HIGHEST_PRECEDENCE + 2)
    public void testUpdateSshKeyValid() throws Exception {
        mockMvc
                .perform(
                        post(ApiPathConstant.SSH_KEY_UPDATE_SSH_KEY_API_PATH)
                                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {
                                            "id": "%s",
                                            "name": "My SSH Key Updated",
                                            "userId": "%s",
                                            "publicKey": "This is my public key updated"
                                        }
                                        """.formatted(TestConstant.SSH_KEY_ID, TestConstant.ID)))
                .andExpectAll(
                    status().isOk(),
                    jsonPath("$.id", is(TestConstant.SSH_KEY_ID)),
                    jsonPath("$.userId", is(TestConstant.ID)),
                    jsonPath("$.name", is("My SSH Key Updated")),
                    jsonPath("$.publicKey", is("This is my public key updated"))
                );
    }

    /**
     * Test delete ssh-key
     *
     * <p>This must excute after {@link #testUpdateSshKeyValid() testUploadSshKeyValid}
     *
     * @throws Exception
     */
    @Test
    @Order(Ordered.HIGHEST_PRECEDENCE + 3)
    public void testDeleteSshKeyValid() throws Exception {
        mockMvc
                .perform(
                        delete(ApiPathConstant.SSH_KEY_DELETE_SSH_KEY_API_PATH)
                                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                                .param("id", TestConstant.SSH_KEY_ID))
                .andExpect(status().isOk());
        TestConstant.SSH_KEY_ID = null;
        TestConstant.SSH_KEY_SIZE--;
        // check if the size has been decreased
        testPageSshKeyValid();
    }
}
