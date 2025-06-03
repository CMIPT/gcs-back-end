package edu.cmipt.gcs.controller;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.cmipt.gcs.constant.ApiPathConstant;
import edu.cmipt.gcs.constant.ApplicationConstant;
import edu.cmipt.gcs.constant.HeaderParameter;
import edu.cmipt.gcs.constant.TestConstant;
import edu.cmipt.gcs.enumeration.AddCollaboratorTypeEnum;
import edu.cmipt.gcs.enumeration.CollaboratorOrderByEnum;
import edu.cmipt.gcs.enumeration.RepositoryOrderByEnum;
import edu.cmipt.gcs.enumeration.UserQueryTypeEnum;
import edu.cmipt.gcs.pojo.collaboration.CollaboratorVO;
import edu.cmipt.gcs.pojo.other.PageVO;
import edu.cmipt.gcs.pojo.repository.RepositoryDetailVO;
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
                            .param("size", TestConstant.REPOSITORY_SIZE.toString())
                            .param("orderBy", RepositoryOrderByEnum.GMT_CREATED.name())
                            .param("isAsc", "false"))
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
              var firstNonPrivateRepository =
                  pageVO.records().stream().filter(r -> !r.isPrivate()).findFirst().orElse(null);
              TestConstant.REPOSITORY_ID = firstNonPrivateRepository.id();
              TestConstant.REPOSITORY_NAME = firstNonPrivateRepository.repositoryName();
            } else {
              TestConstant.OTHER_REPOSITORY_ID =
                  pageVO.records().stream()
                      .filter(r -> !r.isPrivate())
                      .findFirst()
                      .map(RepositoryVO::id)
                      .orElse(null);
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
    var content =
        mvc.perform(
                get(ApiPathConstant.REPOSITORY_GET_REPOSITORY_API_PATH)
                    .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                    .param("id", TestConstant.REPOSITORY_ID)
                    .param("ref", "master")
                    .param("path", "/"))
            .andExpectAll(
                status().isOk(),
                jsonPath("$.id").value(TestConstant.REPOSITORY_ID),
                jsonPath("$.repositoryName").value(TestConstant.REPOSITORY_NAME),
                jsonPath("$.isPrivate").value(false),
                jsonPath("$.userId").value(TestConstant.ID),
                jsonPath("$.username").value(TestConstant.USERNAME),
                jsonPath("$.star").value(0),
                jsonPath("$.fork").value(0),
                jsonPath("$.watcher").value(0),
                jsonPath("$.branchList").isArray(),
                jsonPath("$.branchList.length()").value(1),
                jsonPath("$.branchList[0]").value("refs/heads/master"),
                jsonPath("$.tagList").isArray(),
                jsonPath("$.tagList.length()").value(0),
                jsonPath("$.defaultRef").value("refs/heads/master"),
                jsonPath("$.commit.hash").isString(),
                jsonPath("$.commit.message").value("Initial commit"),
                jsonPath("$.commit.timestamp").isString(),
                jsonPath("$.commit.author.name").isString(),
                jsonPath("$.commit.author.email").isString(),
                jsonPath("$.commit.author.avatarUrl").value(""))
            .andReturn()
            .getResponse()
            .getContentAsString();
    TestConstant.REPOSITORY_LATEST_COMMIT_HASH =
        objectMapper.readValue(content, RepositoryDetailVO.class).commit().hash();
  }

  @Test
  public void testGetCommitDetailsValid() throws Exception {
    var res =
        mvc.perform(
                get(ApiPathConstant.REPOSITORY_GET_REPOSITORY_COMMIT_DETAILS_API_PATH)
                    .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                    .param("id", TestConstant.REPOSITORY_ID)
                    .param("commitHash", TestConstant.REPOSITORY_LATEST_COMMIT_HASH))
            .andExpect(request().asyncStarted())
            .andReturn();
    mvc.perform(asyncDispatch(res))
        .andExpectAll(
            status().isOk(),
            jsonPath("$.hash").isString(),
            jsonPath("$.message").value("Initial commit"),
            jsonPath("$.timestamp").isString(),
            jsonPath("$.author.name").isString(),
            jsonPath("$.author.email").isString(),
            jsonPath("$.author.avatarUrl").value(""),
            jsonPath("$.diffList").isArray(),
            jsonPath("$.diffList.length()").value(1),
            jsonPath("$.diffList[0].oldPath").value(nullValue()),
            jsonPath("$.diffList[0].newPath").value("README.md"),
            jsonPath("$.diffList[0].content").isString());
  }

  @Test
  public void testGetRepositoryDirectoryWithRefValid() throws Exception {
    mvc.perform(
            get(ApiPathConstant.REPOSITORY_GET_REPOSITORY_DIRECTORY_WITH_REF_API_PATH)
                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                .param("id", TestConstant.REPOSITORY_ID)
                .param("ref", "master")
                .param("path", "/"))
        .andExpectAll(
            status().isOk(),
            jsonPath("$").isArray(),
            jsonPath("$.length()").value(1),
            jsonPath("$[0].name").value("README.md"),
            jsonPath("$[0].isDirectory").value(false),
            jsonPath("$[0].commit.hash").isString(),
            jsonPath("$[0].commit.message").value("Initial commit"),
            jsonPath("$[0].commit.timestamp").isString(),
            jsonPath("$[0].commit.author.name").isString(),
            jsonPath("$[0].commit.author.email").isString(),
            jsonPath("$[0].commit.author.avatarUrl").value(""));
  }

  @Test
  public void testGetRepositoryFileWithRefValid() throws Exception {
    mvc.perform(
            get(ApiPathConstant.REPOSITORY_GET_REPOSITORY_FILE_WITH_REF_API_PATH)
                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                .param("id", TestConstant.REPOSITORY_ID)
                .param("ref", "master")
                .param("path", "/README.md"))
        .andExpectAll(status().isOk(), content().bytes("".getBytes()));
  }

  @Test
  public void testGetRepositoryDetailsInvalid() throws Exception {
    mvc.perform(
            get(ApiPathConstant.REPOSITORY_GET_REPOSITORY_API_PATH)
                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                .param("id", "123"))
        .andExpectAll(status().isNotFound());
  }

  @Test
  public void testGetRepositoryDirectoryWithRefInvalid() throws Exception {
    mvc.perform(
            get(ApiPathConstant.REPOSITORY_GET_REPOSITORY_DIRECTORY_WITH_REF_API_PATH)
                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                .param("id", TestConstant.REPOSITORY_ID)
                .param("ref", "master")
                .param("path", "/invalid"))
        .andExpect(status().isNotFound());
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
    var content =
        mvc.perform(
                get(ApiPathConstant.REPOSITORY_PAGE_COLLABORATOR_API_PATH)
                    .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                    .param("repositoryId", TestConstant.REPOSITORY_ID)
                    .param("page", "1")
                    .param("size", "10")
                    .param("orderBy", CollaboratorOrderByEnum.GMT_CREATED.name())
                    .param("isAsc", "false"))
            .andExpectAll(
                status().isOk(),
                jsonPath("$.total").value(greaterThan(0)),
                jsonPath("$.records").isArray(),
                jsonPath("$.records.length()").value(1),
                jsonPath("$.records[0].collaboratorId").value(TestConstant.OTHER_ID),
                jsonPath("$.records[0].username").value(TestConstant.OTHER_USERNAME),
                jsonPath("$.records[0].email").value(TestConstant.OTHER_EMAIL))
            .andReturn()
            .getResponse()
            .getContentAsString();
    var pageVO = objectMapper.readValue(content, new TypeReference<PageVO<CollaboratorVO>>() {});
    TestConstant.COLLABORATION_ID = pageVO.records().get(0).id();
  }

  @Test
  public void testPageOtherCollaboratorInvalid() throws Exception {
    mvc.perform(
            get(ApiPathConstant.REPOSITORY_PAGE_COLLABORATOR_API_PATH)
                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                .param("repositoryId", TestConstant.OTHER_PRIVATE_REPOSITORY_ID)
                .param("page", "1")
                .param("size", "10")
                .param("orderBy", CollaboratorOrderByEnum.GMT_CREATED.name())
                .param("isAsc", "false"))
        .andExpect(status().isNotFound());
  }

  @Test
  @Order(Ordered.HIGHEST_PRECEDENCE + 4)
  public void testRemoveCollaborationInvalid() throws Exception {
    // remove other's collaboration
    mvc.perform(
            delete(ApiPathConstant.REPOSITORY_REMOVE_COLLABORATION_API_PATH)
                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.OTHER_ACCESS_TOKEN)
                .param("id", TestConstant.COLLABORATION_ID))
        .andExpect(status().isForbidden());
  }

  @Test
  @Order(Ordered.HIGHEST_PRECEDENCE + 5)
  public void testRemoveCollaborationValid() throws Exception {
    mvc.perform(
            delete(ApiPathConstant.REPOSITORY_REMOVE_COLLABORATION_API_PATH)
                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                .param("id", TestConstant.COLLABORATION_ID))
        .andExpect(status().isOk());
    TestConstant.COLLABORATION_ID = null;
  }

  @Test
  @Order(Ordered.HIGHEST_PRECEDENCE + 6)
  public void testDeleteRepositoryValid() throws Exception {
    mvc.perform(
            delete(ApiPathConstant.REPOSITORY_DELETE_REPOSITORY_API_PATH)
                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                .param("id", TestConstant.REPOSITORY_ID))
        .andExpect(status().isOk());
    TestConstant.REPOSITORY_ID = null;
    TestConstant.REPOSITORY_NAME = null;
    TestConstant.REPOSITORY_LATEST_COMMIT_HASH = null;
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
  @Order(Ordered.HIGHEST_PRECEDENCE + 7)
  public void testPageRepositoryValid() throws Exception {
    var result = repositoryPager.apply(TestConstant.ACCESS_TOKEN, TestConstant.ID);
    if (result != null) {
      throw result;
    }
  }

  @Test
  @Order(Ordered.HIGHEST_PRECEDENCE + 8)
  public void testPageOtherUserRepositoryValid() throws Exception {
    mvc.perform(
            get(ApiPathConstant.REPOSITORY_PAGE_REPOSITORY_API_PATH)
                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.OTHER_ACCESS_TOKEN)
                .param("user", TestConstant.ID)
                .param("userType", UserQueryTypeEnum.ID.name())
                .param("page", "1")
                .param("size", TestConstant.REPOSITORY_SIZE.toString())
                .param("orderBy", RepositoryOrderByEnum.GMT_CREATED.name())
                .param("isAsc", "false"))
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
