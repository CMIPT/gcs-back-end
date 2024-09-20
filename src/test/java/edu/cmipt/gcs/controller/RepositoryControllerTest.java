package edu.cmipt.gcs.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import edu.cmipt.gcs.constant.ApiPathConstant;
import edu.cmipt.gcs.constant.HeaderParameter;
import edu.cmipt.gcs.constant.TestConstant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Tests for RepositoryController
 *
 * @author Kaiser
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class RepositoryControllerTest {
    @Autowired private MockMvc mvc;

    @Test
    public void testCreateRepositoryValid() throws Exception {
        String isPrivate = "";
        String repositoryName = "";
        for (int i = 0; i < TestConstant.REPOSITORY_SIZE; i++) {
            if (i % 2 == 0) {
                isPrivate = "true";
            } else {
                isPrivate = "false";
            }
            repositoryName = String.valueOf(i);
            mvc.perform(
                            post(ApiPathConstant.REPOSITORY_CREATE_REPOSITORY_API_PATH)
                                    .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(
                                            """
                                            {
                                                "repositoryName": "%s",
                                                "isPrivate": %s
                                            }
                                            """
                                                    .formatted(repositoryName, isPrivate)))
                    .andExpect(status().isOk());
        }
    }
}
