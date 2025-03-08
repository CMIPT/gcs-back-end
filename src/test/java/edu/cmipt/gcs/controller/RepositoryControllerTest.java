package edu.cmipt.gcs.controller;

import static org.hamcrest.Matchers.greaterThan;
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
import edu.cmipt.gcs.enumeration.AddCollaboratorTypeEnum;
import edu.cmipt.gcs.enumeration.UserQueryTypeEnum;
import edu.cmipt.gcs.pojo.other.PageVO;
import edu.cmipt.gcs.pojo.repository.RepositoryVO;
import java.util.function.BiFunction;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.Ordered;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Tests for RepositoryController
 *
 * @author Kaiser
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles({ApplicationConstant.TEST_PROFILE})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RepositoryControllerTest {
  @Autowired private ObjectMapper objectMapper;
  @Autowired private MockMvc mvc;
  private BiFunction<String, String, Exception> repositoryCreator;
  private BiFunction<String, String, Exception> repositoryPager;

  @BeforeAll
  public void init() {
    repositoryCreator =
        (accessToken, repositoryDTO) -> {
          try {
            mvc.perform(
                    post(ApiPathConstant.REPOSITORY_CREATE_REPOSITORY_API_PATH)
                        .header(HeaderParameter.ACCESS_TOKEN, accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(repositoryDTO))
                .andExpect(status().isOk());
          } catch (Exception e) {
            return e;
          }
          return null;
        };
    repositoryPager =
        (accessToken, userID) -> {
          try {
            var content =
                mvc.perform(
                        get(ApiPathConstant.REPOSITORY_PAGE_REPOSITORY_API_PATH)
                            .header(HeaderParameter.ACCESS_TOKEN, accessToken)
                            .param("user", userID)
                            .param("userType", UserQueryTypeEnum.ID.name())
                            .param("page", "1")
                            .param("size", TestConstant.REPOSITORY_SIZE.toString()))
                    .andExpectAll(
                        status().isOk(),
                        jsonPath("$.total").value(greaterThan(0)),
                        jsonPath("$.records").isArray(),
                        jsonPath("$.records.length()").value(TestConstant.REPOSITORY_SIZE))
                    .andReturn()
                    .getResponse()
                    .getContentAsString();
            var pageVO =
                objectMapper.readValue(content, new TypeReference<PageVO<RepositoryVO>>() {});
            if (userID.equals(TestConstant.ID)) {
              TestConstant.REPOSITORY_ID = pageVO.records().get(0).id();
              TestConstant.REPOSITORY_NAME = pageVO.records().get(0).repositoryName();
            } else {
              TestConstant.OTHER_REPOSITORY_ID = pageVO.records().get(0).id();
              TestConstant.OTHER_PRIVATE_REPOSITORY_ID =
                  pageVO.records().stream()
                      .filter(RepositoryVO::isPrivate)
                      .findFirst()
                      .map(RepositoryVO::id)
                      .orElse(null);
            }
          } catch (Exception e) {
            return e;
          }
          return null;
        };
  }

  @Test
  @Order(Ordered.HIGHEST_PRECEDENCE)
  public void testCreateRepositoryValid() throws Exception {
    for (int i = 0; i < TestConstant.REPOSITORY_SIZE; i++) {
      String repositoryDTO =
          """
          {
              "repositoryName": "%s",
              "isPrivate": %s
          }
          """
              .formatted(String.valueOf(i), i % 2 == 0 ? "false" : "true");
      var result1 = repositoryCreator.apply(TestConstant.ACCESS_TOKEN, repositoryDTO);
      if (result1 != null) {
        throw result1;
      }
      var result2 = repositoryCreator.apply(TestConstant.OTHER_ACCESS_TOKEN, repositoryDTO);
      if (result2 != null) {
        throw result2;
      }
    }
    var result1 = repositoryPager.apply(TestConstant.ACCESS_TOKEN, TestConstant.ID);
    if (result1 != null) {
      throw result1;
    }
    var result2 = repositoryPager.apply(TestConstant.OTHER_ACCESS_TOKEN, TestConstant.OTHER_ID);
    if (result2 != null) {
      throw result2;
    }
  }

  @Test
  public void testCreateRepositoryInvalid() throws Exception {
    mvc.perform(
            post(ApiPathConstant.REPOSITORY_CREATE_REPOSITORY_API_PATH)
                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "repositoryName": "",
                        "isPrivate": false
                    }
                    """))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void testGetRepositoryDetailsValid() throws Exception {
    mvc.perform(
            get(ApiPathConstant.REPOSITORY_GET_REPOSITORY_API_PATH)
                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                .param("id", TestConstant.REPOSITORY_ID)
                .param("ref", "master")
                .param("path", "/"))
        .andExpectAll(
            status().isOk(),
            jsonPath("$.repositoryVO.id").value(TestConstant.REPOSITORY_ID),
            jsonPath("$.repositoryVO.repositoryName").value(TestConstant.REPOSITORY_NAME),
            jsonPath("$.repositoryVO.userId").value(TestConstant.ID),
            jsonPath("$.repositoryVO.star").value(0),
            jsonPath("$.repositoryVO.fork").value(0),
            jsonPath("$.repositoryVO.watcher").value(0),
            jsonPath("$.branchList").isArray(),
            jsonPath("$.branchList.length()").value(1),
            jsonPath("$.branchList[0]").value("refs/heads/master"),
            jsonPath("$.tagList").isArray(),
            jsonPath("$.tagList.length()").value(0),
            jsonPath("$.defaultRef").value("refs/heads/master"),
            jsonPath("$.path.isDirectory").value(true),
            jsonPath("$.path.content").value(""),
            jsonPath("$.path.readmeContent").value(""),
            jsonPath("$.path.licenseContent").value(""),
            jsonPath("$.path.directoryList").isArray(),
            jsonPath("$.path.directoryList.length()").value(1));
  }

  @Test
  public void testGetRepositoryDetailsInvalid() throws Exception {
    mvc.perform(
            get(ApiPathConstant.REPOSITORY_GET_REPOSITORY_API_PATH)
                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                .param("id", TestConstant.REPOSITORY_ID)
                .param("ref", "invalid ref"))
        .andExpectAll(status().isNotFound());
  }

  @Test
  public void testGetOtherPrivateRepositoryInvalid() throws Exception {
    mvc.perform(
            get(ApiPathConstant.REPOSITORY_GET_REPOSITORY_API_PATH)
                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                .param("id", TestConstant.OTHER_PRIVATE_REPOSITORY_ID))
        .andExpect(status().isNotFound());
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
                        .formatted(TestConstant.REPOSITORY_ID, newDescription)))
        .andExpectAll(status().isOk());
  }

  @Test
  public void testUpdateOtherRepositoryInvalid() throws Exception {
    mvc.perform(
            post(ApiPathConstant.REPOSITORY_UPDATE_REPOSITORY_API_PATH)
                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "id": "%s",
                        "repositoryDescription": "This is a test description"
                    }
                    """
                        .formatted(TestConstant.OTHER_REPOSITORY_ID)))
        .andExpect(status().isForbidden());
  }

  @Test
  @Order(Ordered.HIGHEST_PRECEDENCE + 2)
  public void testAddCollaboratorValid() throws Exception {
    mvc.perform(
            post(ApiPathConstant.REPOSITORY_ADD_COLLABORATOR_API_PATH)
                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                .param("repositoryId", TestConstant.REPOSITORY_ID)
                .param("collaborator", TestConstant.OTHER_ID)
                .param("collaboratorType", AddCollaboratorTypeEnum.ID.name()))
        .andExpect(status().isOk());
  }

  @Test
  public void testAddCollaboratorInvalid() throws Exception {
    // add self to other's public repository
    mvc.perform(
            post(ApiPathConstant.REPOSITORY_ADD_COLLABORATOR_API_PATH)
                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                .param("repositoryId", TestConstant.OTHER_REPOSITORY_ID)
                .param("collaborator", TestConstant.USERNAME)
                .param("collaboratorType", AddCollaboratorTypeEnum.USERNAME.name()))
        .andExpect(status().isForbidden());
    // add self to other's private repository
    mvc.perform(
            post(ApiPathConstant.REPOSITORY_ADD_COLLABORATOR_API_PATH)
                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                .param("repositoryId", TestConstant.OTHER_PRIVATE_REPOSITORY_ID)
                .param("collaborator", TestConstant.USERNAME)
                .param("collaboratorType", AddCollaboratorTypeEnum.USERNAME.name()))
        .andExpect(status().isNotFound());
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
            jsonPath("$.total").value(greaterThan(0)),
            jsonPath("$.records").isArray(),
            jsonPath("$.records.length()").value(1),
            jsonPath("$.records[0].id").value(TestConstant.OTHER_ID),
            jsonPath("$.records[0].username").value(TestConstant.OTHER_USERNAME),
            jsonPath("$.records[0].email").value(TestConstant.OTHER_EMAIL));
  }

  @Test
  public void testPageOtherCollaboratorInvalid() throws Exception {
    mvc.perform(
            get(ApiPathConstant.REPOSITORY_PAGE_COLLABORATOR_API_PATH)
                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                .param("repositoryId", TestConstant.OTHER_PRIVATE_REPOSITORY_ID)
                .param("page", "1")
                .param("size", "10"))
        .andExpect(status().isNotFound());
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
  @Order(Ordered.HIGHEST_PRECEDENCE + 4)
  public void testRemoveCollaborationInvalid() throws Exception {
    // remove self from other's public repository
    mvc.perform(
            delete(ApiPathConstant.REPOSITORY_REMOVE_COLLABORATION_API_PATH)
                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                .param("repositoryId", TestConstant.OTHER_REPOSITORY_ID)
                .param("collaboratorId", TestConstant.ID))
        .andExpect(status().isForbidden());
    // remove self from other's private repository
    mvc.perform(
            delete(ApiPathConstant.REPOSITORY_REMOVE_COLLABORATION_API_PATH)
                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                .param("repositoryId", TestConstant.OTHER_PRIVATE_REPOSITORY_ID)
                .param("collaboratorId", TestConstant.ID))
        .andExpect(status().isNotFound());
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

  @Test
  public void testDeleteOtherRepositoryInvalid() throws Exception {
    mvc.perform(
            delete(ApiPathConstant.REPOSITORY_DELETE_REPOSITORY_API_PATH)
                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                .param("id", TestConstant.OTHER_REPOSITORY_ID))
        .andExpect(status().isForbidden());
  }

  @Test
  @Order(Ordered.HIGHEST_PRECEDENCE + 6)
  public void testPageRepositoryValid() throws Exception {
    var result = repositoryPager.apply(TestConstant.ACCESS_TOKEN, TestConstant.ID);
    if (result != null) {
      throw result;
    }
  }

  @Test
  @Order(Ordered.HIGHEST_PRECEDENCE + 7)
  public void testPageOtherUserRepositoryValid() throws Exception {
    mvc.perform(
            get(ApiPathConstant.REPOSITORY_PAGE_REPOSITORY_API_PATH)
                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.OTHER_ACCESS_TOKEN)
                .param("user", TestConstant.ID)
                .param("userType", UserQueryTypeEnum.ID.name())
                .param("page", "1")
                .param("size", TestConstant.REPOSITORY_SIZE.toString()))
        .andExpectAll(
            status().isOk(),
            jsonPath("$.total").value(greaterThan(0)),
            jsonPath("$.records").isArray(),
            jsonPath("$.records.length()").value(TestConstant.REPOSITORY_SIZE / 2));
  }

  @Test
  public void testCheckRepositoryNameValidityValid() throws Exception {
    mvc.perform(
            get(ApiPathConstant.REPOSITORY_CHECK_REPOSITORY_NAME_VALIDITY_API_PATH)
                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                .param("repositoryName", "test"))
        .andExpect(status().isOk());
  }

  @Test
  public void testCheckRepositoryNameValidityInvalid() throws Exception {
    mvc.perform(
            get(ApiPathConstant.REPOSITORY_CHECK_REPOSITORY_NAME_VALIDITY_API_PATH)
                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                .param("repositoryName", "??!!"))
        .andExpect(status().isBadRequest());
  }
}
