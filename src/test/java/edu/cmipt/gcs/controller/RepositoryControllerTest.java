package edu.cmipt.gcs.controller;

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

/**
 * Tests for RepositoryController
 *
 * @author Kaiser
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RepositoryControllerTest {
    @Autowired private MockMvc mvc;

    @Test
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public void testCreateRepositoryValid() throws Exception {
        String repositoryName = "";
        for (int i = 0; i < TestConstant.REPOSITORY_SIZE; i++) {
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
                                                    .formatted(
                                                            repositoryName,
                                                            i % 2 == 0 ? "false" : "true")))
                    .andExpect(status().isOk());
        }
        var content =
                mvc.perform(
                                get(ApiPathConstant.USER_PAGE_USER_REPOSITORY_API_PATH)
                                        .header(
                                                HeaderParameter.ACCESS_TOKEN,
                                                TestConstant.ACCESS_TOKEN)
                                        .param("id", TestConstant.ID)
                                        .param("page", "1")
                                        .param("size", TestConstant.REPOSITORY_SIZE.toString()))
                        .andExpectAll(
                                status().isOk(),
                                jsonPath("$").isArray(),
                                jsonPath("$.length()").value(TestConstant.REPOSITORY_SIZE))
                        .andReturn()
                        .getResponse()
                        .getContentAsString();
        content = JsonParserFactory.getJsonParser().parseList(content).get(0).toString();
        Matcher matcher = Pattern.compile("id=(\\d+),").matcher(content);
        matcher.find();
        TestConstant.REPOSITORY_ID = matcher.group(1);
        matcher = Pattern.compile("repositoryName=(.+?),").matcher(content);
        matcher.find();
        TestConstant.REPOSITORY_NAME = matcher.group(1);
    }

    @Test
    @Order(Ordered.HIGHEST_PRECEDENCE + 1)
    public void testUpdateRepositoryValid() throws Exception {
        String newDescription = "This is a test description";
        mvc.perform(
                        post(ApiPathConstant.REPOSITORY_UPDATE_REPOSITORY_API_PATH)
                                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {
                                            "id": "%s",
                                            "repositoryDescription": "%s"
                                        }
                                        """
                                                .formatted(
                                                        TestConstant.REPOSITORY_ID,
                                                        newDescription)))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.id").value(TestConstant.REPOSITORY_ID),
                        jsonPath("$.repositoryName").value(TestConstant.REPOSITORY_NAME),
                        jsonPath("$.repositoryDescription").value(newDescription),
                        jsonPath("$.userId").value(TestConstant.ID),
                        jsonPath("$.star").value(0),
                        jsonPath("$.fork").value(0),
                        jsonPath("$.watcher").value(0));
    }

    @Test
    @Order(Ordered.HIGHEST_PRECEDENCE + 2)
    public void testAddCollaboratorByIdValid() throws Exception {
        mvc.perform(
                        post(ApiPathConstant.REPOSITORY_ADD_COLLABORATOR_BY_ID_API_PATH)
                                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                                .param("repositoryId", TestConstant.REPOSITORY_ID)
                                .param("collaboratorId", TestConstant.OTHER_ID))
                .andExpect(status().isOk());
    }

    @Test
    @Order(Ordered.HIGHEST_PRECEDENCE + 3)
    public void testPageCollaboratorValid() throws Exception {
        mvc.perform(
                        get(ApiPathConstant.REPOSITORY_PAGE_COLLABORATOR_API_PATH)
                                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                                .param("repositoryId", TestConstant.REPOSITORY_ID)
                                .param("page", "1")
                                .param("size", "10"))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$").isArray(),
                        jsonPath("$.length()").value(1),
                        jsonPath("$[0].id").value(TestConstant.OTHER_ID),
                        jsonPath("$[0].username").value(TestConstant.OTHER_USERNAME),
                        jsonPath("$[0].email").value(TestConstant.OTHER_EMAIL));
    }

    @Test
    @Order(Ordered.HIGHEST_PRECEDENCE + 4)
    public void testRemoveCollaborationValid() throws Exception {
        mvc.perform(
                        delete(ApiPathConstant.REPOSITORY_REMOVE_COLLABORATION_API_PATH)
                                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                                .param("repositoryId", TestConstant.REPOSITORY_ID)
                                .param("collaboratorId", TestConstant.OTHER_ID))
                .andExpect(status().isOk());
    }

    @Test
    @Order(Ordered.HIGHEST_PRECEDENCE + 5)
    public void testDeleteRepositoryValid() throws Exception {
        mvc.perform(
                        delete(ApiPathConstant.REPOSITORY_DELETE_REPOSITORY_API_PATH)
                                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                                .param("id", TestConstant.REPOSITORY_ID))
                .andExpect(status().isOk());
        TestConstant.REPOSITORY_ID = null;
        TestConstant.REPOSITORY_NAME = null;
        TestConstant.REPOSITORY_SIZE--;
    }
}
