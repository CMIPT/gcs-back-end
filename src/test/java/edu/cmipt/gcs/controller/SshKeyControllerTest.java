package edu.cmipt.gcs.controller;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.cmipt.gcs.constant.ApiPathConstant;
import edu.cmipt.gcs.constant.ApplicationConstant;
import edu.cmipt.gcs.constant.HeaderParameter;
import edu.cmipt.gcs.constant.TestConstant;
import edu.cmipt.gcs.pojo.other.PageVO;
import edu.cmipt.gcs.pojo.ssh.SshKeyVO;

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

import java.nio.file.Files;
import java.nio.file.Paths;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles({ApplicationConstant.TEST_PROFILE})
public class SshKeyControllerTest {
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Test
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public void testUploadSshKeyValid() throws Exception {
        for (int i = 0; i < TestConstant.SSH_KEY_SIZE; i++) {
            String name = "GCS_TEST_SSH_TMP_" + i;
            String publicKey = null;
            // generate ssh key pair to /tmp and read the public key, then delete the key pair
            ProcessBuilder processBuilder =
                    new ProcessBuilder("ssh-keygen", "-t", "rsa", "-f", "/tmp/" + name, "-N", "");
            if (processBuilder.start().waitFor() == 0) {
                publicKey = Files.readAllLines(Paths.get("/tmp/" + name + ".pub")).get(0);
                new ProcessBuilder("rm", "/tmp/" + name, "/tmp/" + name + ".pub").start().waitFor();
            }
            if (publicKey == null) {
                throw new Exception("Failed to generate ssh key pair");
            }
            mockMvc.perform(
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
                                            """
                                                    .formatted(name, TestConstant.ID, publicKey)))
                    .andExpect(status().isOk());
        }
    }

    @Test
    @Order(Ordered.HIGHEST_PRECEDENCE + 1)
    public void testPageSshKeyValid() throws Exception {
        var content =
                mockMvc.perform(
                                get(ApiPathConstant.SSH_KEY_PAGE_SSH_KEY_API_PATH)
                                        .header(
                                                HeaderParameter.ACCESS_TOKEN,
                                                TestConstant.ACCESS_TOKEN)
                                        .param("id", TestConstant.ID)
                                        .param("page", "1")
                                        .param("size", TestConstant.SSH_KEY_SIZE.toString()))
                        .andExpectAll(
                                status().isOk(),
                                jsonPath("$.pages").value(greaterThan(0)),
                                jsonPath("$.total").value(greaterThan(0)),
                                jsonPath("$.records").isArray(),
                                jsonPath("$.records.length()").value(TestConstant.SSH_KEY_SIZE))
                        .andReturn()
                        .getResponse()
                        .getContentAsString();
        var pageVO = objectMapper.readValue(content, new TypeReference<PageVO<SshKeyVO>>() {});
        TestConstant.SSH_KEY_ID = pageVO.records().get(0).id();
    }

    @Test
    @Order(Ordered.HIGHEST_PRECEDENCE + 2)
    public void testUpdateSshKeyValid() throws Exception {
        mockMvc.perform(
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
                                        """
                                                .formatted(
                                                        TestConstant.SSH_KEY_ID, TestConstant.ID)))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.id", is(TestConstant.SSH_KEY_ID)),
                        jsonPath("$.userId", is(TestConstant.ID)),
                        jsonPath("$.name", is("My SSH Key Updated")),
                        jsonPath("$.publicKey", is("This is my public key updated")));
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
        mockMvc.perform(
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
