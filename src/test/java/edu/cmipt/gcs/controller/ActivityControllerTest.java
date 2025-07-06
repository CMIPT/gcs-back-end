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
import edu.cmipt.gcs.enumeration.ActivityOrderByEnum;
import edu.cmipt.gcs.enumeration.UserQueryTypeEnum;
import edu.cmipt.gcs.pojo.activity.ActivityDetailVO;
import edu.cmipt.gcs.pojo.assign.AssigneeVO;
import edu.cmipt.gcs.pojo.comment.CommentPO;
import edu.cmipt.gcs.pojo.other.PageVO;
import edu.cmipt.gcs.service.ActivityAssignLabelService;
import edu.cmipt.gcs.service.ActivityDesignateAssigneeService;
import edu.cmipt.gcs.service.CommentService;
import java.util.function.BiFunction;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.Ordered;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

/**
 * Tests for RepositoryController
 *
 * @author LuckyGalaxy666
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles({ApplicationConstant.TEST_PROFILE})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ActivityControllerTest {
  @Autowired private ObjectMapper objectMapper;
  @Autowired private CommentService commentService;
  @Autowired private ActivityAssignLabelService activityAssignLabelService;
  @Autowired private ActivityDesignateAssigneeService activityDesignateAssigneeService;
  @Autowired private MockMvc mvc;
  private BiFunction<String, String, Exception> activityCreator;
  private BiFunction<String, String, Exception> activityPager;

  @BeforeAll
  public void init() {
    activityCreator =
        (accessToken, activityDTO) -> {
          try {
            mvc.perform(
                    post(ApiPathConstant.ACTIVITY_CREATE_ACTIVITY_API_PATH)
                        .header(HeaderParameter.ACCESS_TOKEN, accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(activityDTO))
                .andExpect(status().isOk());
          } catch (Exception e) {
            return e;
          }
          return null;
        };
    activityPager =
        (accessToken, activityQueryDTO) -> {
          try {
            var content =
                mvc.perform(
                        post(ApiPathConstant.ACTIVITY_PAGE_ACTIVITY_API_PATH)
                            .header(HeaderParameter.ACCESS_TOKEN, accessToken)
                            .param("page", "1")
                            .param("size", TestConstant.ACTIVITY_SIZE.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(activityQueryDTO))
                    .andExpectAll(
                        status().isOk(),
                        jsonPath("$.total").value(greaterThan(0)),
                        jsonPath("$.records").isArray(),
                        jsonPath("$.records.length()").value(TestConstant.ACTIVITY_SIZE))
                    .andReturn()
                    .getResponse()
                    .getContentAsString();
            var pageVO =
                objectMapper.readValue(content, new TypeReference<PageVO<ActivityDetailVO>>() {});
            var repositoryId = pageVO.records().get(0).repositoryId();
            if (repositoryId.equals(TestConstant.REPOSITORY_ID)) {
              TestConstant.REPOSITORY_ACTIVITY_NUMBER = pageVO.records().get(0).number();
              TestConstant.REPOSITORY_ACTIVITY_ID = pageVO.records().get(0).id();
              TestConstant.REPOSITORY_DELETE_ACTIVITY_ID =
                  pageVO.records().get(TestConstant.ACTIVITY_SIZE - 1).id();
            } else if (repositoryId.equals(TestConstant.OTHER_REPOSITORY_ID)) {
              TestConstant.OTHER_REPOSITORY_ACTIVITY_NUMBER = pageVO.records().get(0).number();
              TestConstant.OTHER_REPOSITORY_ACTIVITY_ID = pageVO.records().get(0).id();
            } else {
              TestConstant.OTHER_PRIVATE_REPOSITORY_ACTIVITY_NUMBER =
                  pageVO.records().get(0).number();
              TestConstant.OTHER_PRIVATE_REPOSITORY_ACTIVITY_ID = pageVO.records().get(0).id();
            }

          } catch (Exception e) {
            return e;
          }
          return null;
        };
  }

  @Test
  @Order(Ordered.HIGHEST_PRECEDENCE)
  public void testCreateActivityValid() throws Exception {
    for (int i = 0; i < TestConstant.ACTIVITY_SIZE; i++) {
      String activityDTO =
          """
          {
              "repositoryId": "%s",
              "title": "Test Activity %s",
              "description": "This is a test activity %s",
              "isPullRequest": false
          }
          """
              .formatted(TestConstant.REPOSITORY_ID, i, i);
      var result1 = activityCreator.apply(TestConstant.ACCESS_TOKEN, activityDTO);
      if (result1 != null) {
        throw result1;
      }
      String otherActivityDTO =
          """
          {
              "repositoryId": "%s",
              "title": "Test Activity %s",
              "description": "This is a test activity %s",
              "isPullRequest": false
          }
          """
              .formatted(TestConstant.OTHER_REPOSITORY_ID, i, i);
      var result2 = activityCreator.apply(TestConstant.OTHER_ACCESS_TOKEN, otherActivityDTO);
      if (result2 != null) {
        throw result2;
      }
      String privateActivityDTO =
          """
          {
              "repositoryId": "%s",
              "title": "Test Activity %s",
              "description": "This is a test activity %s",
              "isPullRequest": false
          }
          """
              .formatted(TestConstant.OTHER_PRIVATE_REPOSITORY_ID, i, i);
      var result3 = activityCreator.apply(TestConstant.OTHER_ACCESS_TOKEN, privateActivityDTO);
      if (result3 != null) {
        throw result3;
      }
    }
    String activityQueryDTO =
        """
        {
            "user": "%s",
            "userType":"%s",
            "repositoryId": "%s",
            "isPullRequest": false
        }
        """
            .formatted(
                TestConstant.USERNAME,
                UserQueryTypeEnum.USERNAME.name(),
                TestConstant.REPOSITORY_ID);
    var result1 = activityPager.apply(TestConstant.ACCESS_TOKEN, activityQueryDTO);
    if (result1 != null) {
      throw result1;
    }
    var otherActivityQueryDTO =
        """
        {
            "user": "%s",
            "userType":"%s",
            "repositoryId": "%s",
            "isPullRequest": false
        }
        """
            .formatted(
                TestConstant.OTHER_USERNAME,
                UserQueryTypeEnum.USERNAME.name(),
                TestConstant.OTHER_REPOSITORY_ID);
    var result2 = activityPager.apply(TestConstant.OTHER_ACCESS_TOKEN, otherActivityQueryDTO);
    if (result2 != null) {
      throw result2;
    }

    var otherPrivateActivityQueryDTO =
        """
        {
            "user": "%s",
            "userType":"%s",
            "repositoryId": "%s",
            "isPullRequest": false
        }
        """
            .formatted(
                TestConstant.OTHER_USERNAME,
                UserQueryTypeEnum.USERNAME.name(),
                TestConstant.OTHER_PRIVATE_REPOSITORY_ID);
    var result3 =
        activityPager.apply(TestConstant.OTHER_ACCESS_TOKEN, otherPrivateActivityQueryDTO);
    if (result3 != null) {
      throw result3;
    }
  }

  @Test
  public void testPageActivityValid() throws Exception {
    var result =
        activityPager.apply(
            TestConstant.ACCESS_TOKEN,
            """
            {
                "user": "%s",
                "userType":"%s",
                "repositoryId": "%s",
                "author": "%s",
                "isPullRequest": false,
                "orderBy": "%s",
                "isAsc": true,
                "isLocked": false,
                "isClosed": false
            }
            """
                .formatted(
                    TestConstant.USERNAME,
                    UserQueryTypeEnum.USERNAME.name(),
                    TestConstant.REPOSITORY_ID,
                    TestConstant.USERNAME,
                    ActivityOrderByEnum.GMT_CREATED.name()));
    if (result != null) {
      throw result;
    }
  }

  @Test
  public void testPageOtherUserRepositoryActivityValid() throws Exception {
    mvc.perform(
            post(ApiPathConstant.ACTIVITY_PAGE_ACTIVITY_API_PATH)
                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.OTHER_ACCESS_TOKEN)
                .param("page", "1")
                .param("size", TestConstant.ACTIVITY_SIZE.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "user": "%s",
                        "userType":"%s",
                        "repositoryId": "%s",
                        "author": "%s",
                        "isPullRequest": false,
                        "orderBy": "%s",
                        "isAsc": true,
                        "isLocked": false,
                        "isClosed": false
                    }
                    """
                        .formatted(
                            TestConstant.USERNAME,
                            UserQueryTypeEnum.USERNAME.name(),
                            TestConstant.REPOSITORY_ID,
                            TestConstant.USERNAME,
                            ActivityOrderByEnum.GMT_CREATED.name())))
        .andExpectAll(
            status().isOk(),
            jsonPath("$.total").value(greaterThan(0)),
            jsonPath("$.records").isArray(),
            jsonPath("$.records.length()").value(TestConstant.ACTIVITY_SIZE));
  }

  @Test
  public void testCreateActivityInvalid() throws Exception {
    mvc.perform(
            post(ApiPathConstant.ACTIVITY_CREATE_ACTIVITY_API_PATH)
                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "repositoryId": "",
                        "title": "",
                        "description": "",
                        "isPullRequest": false
                    }
                    """))
        .andExpect(status().isBadRequest()); // ErrorCodeEnum.MESSAGE_CONVERSION_ERROR
  }

  @Test
  public void testGetActivityDetailsValid() throws Exception {
    mvc.perform(
            get(ApiPathConstant.ACTIVITY_GET_ACTIVITY_API_PATH)
                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                .param("activityNumber", TestConstant.REPOSITORY_ACTIVITY_NUMBER)
                .param("repositoryId", TestConstant.REPOSITORY_ID))
        .andExpectAll(
            status().isOk(),
            jsonPath("$.id").value(TestConstant.REPOSITORY_ACTIVITY_ID),
            jsonPath("$.number").value(TestConstant.REPOSITORY_ACTIVITY_NUMBER),
            jsonPath("$.repositoryId").value(TestConstant.REPOSITORY_ID),
            jsonPath("$.title").isString(),
            jsonPath("$.description").isString(),
            jsonPath("$.username").value(TestConstant.USERNAME),
            jsonPath("$.labels").isArray(),
            jsonPath("$.assignees").isArray(),
            jsonPath("$.commentCnt").value(1),
            jsonPath("$.gmtCreated").isString());

    mvc.perform(
            get(ApiPathConstant.ACTIVITY_GET_ACTIVITY_API_PATH)
                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                .param("id", TestConstant.REPOSITORY_ACTIVITY_ID))
        .andExpectAll(
            status().isOk(),
            jsonPath("$.id").value(TestConstant.REPOSITORY_ACTIVITY_ID),
            jsonPath("$.number").value(TestConstant.REPOSITORY_ACTIVITY_NUMBER),
            jsonPath("$.repositoryId").value(TestConstant.REPOSITORY_ID),
            jsonPath("$.title").isString(),
            jsonPath("$.description").isString(),
            jsonPath("$.username").value(TestConstant.USERNAME),
            jsonPath("$.labels").isArray(),
            jsonPath("$.assignees").isArray(),
            jsonPath("$.commentCnt").value(1),
            jsonPath("$.gmtCreated").isString());
  }

  @Test
  public void testGetActivityDetailsInvalid() throws Exception {
    mvc.perform(
            get(ApiPathConstant.ACTIVITY_GET_ACTIVITY_API_PATH)
                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                .param("id", "123"))
        .andExpectAll(status().isNotFound());
  }

  @Test
  @Order(Ordered.HIGHEST_PRECEDENCE + 1)
  public void testUpdateActivityValid() throws Exception {
    String newDescription = "This is an updated test description";
    mvc.perform(
            post(ApiPathConstant.ACTIVITY_UPDATE_ACTIVITY_CONTENT_API_PATH)
                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "id": "%s",
                        "description": "%s",
                        "isPullRequest": false
                    }
                    """
                        .formatted(TestConstant.REPOSITORY_ACTIVITY_ID, newDescription)))
        .andExpectAll(status().isOk());
  }

  @Test
  public void testUpdateOtherRepositoryActivityInvalid() throws Exception {
    // update other's public repository activity
    String newDescription = "This is an updated test description";
    mvc.perform(
            post(ApiPathConstant.ACTIVITY_UPDATE_ACTIVITY_CONTENT_API_PATH)
                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "id": "%s",
                        "description": "%s",
                        "isPullRequest": false
                    }
                    """
                        .formatted(TestConstant.OTHER_REPOSITORY_ACTIVITY_ID, newDescription)))
        .andExpect(status().isForbidden());
    // update other's private repository activity
    mvc.perform(
            post(ApiPathConstant.ACTIVITY_UPDATE_ACTIVITY_CONTENT_API_PATH)
                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "id": "%s",
                        "description": "%s",
                        "isPullRequest": false
                    }
                    """
                        .formatted(
                            TestConstant.OTHER_PRIVATE_REPOSITORY_ACTIVITY_ID, newDescription)))
        .andExpect(status().isNotFound());
  }

  @Test
  @Order(Ordered.HIGHEST_PRECEDENCE + 2)
  public void testAddAssigneeValid() throws Exception {
    mvc.perform(
            MockMvcRequestBuilders.post(ApiPathConstant.ACTIVITY_ADD_ASSIGNEE_API_PATH)
                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                .param("activityId", TestConstant.REPOSITORY_ACTIVITY_ID)
                .param("assigneeId", TestConstant.OTHER_ID))
        .andExpectAll(status().isOk());

    // Used to verify that the assignee is removed when the activity is deleted
    mvc.perform(
            MockMvcRequestBuilders.post(ApiPathConstant.ACTIVITY_ADD_ASSIGNEE_API_PATH)
                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                .param("activityId", TestConstant.REPOSITORY_DELETE_ACTIVITY_ID)
                .param("assigneeId", TestConstant.OTHER_ID))
        .andExpectAll(status().isOk());
  }

  @Test
  public void testAddAssigneeInvalid() throws Exception {
    // add self to other's public repository activity
    mvc.perform(
            MockMvcRequestBuilders.post(ApiPathConstant.ACTIVITY_ADD_ASSIGNEE_API_PATH)
                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                .param("activityId", TestConstant.OTHER_REPOSITORY_ACTIVITY_ID)
                .param("assigneeId", TestConstant.ID))
        .andExpectAll(status().isForbidden());
    // add self to other's private repository activity
    mvc.perform(
            MockMvcRequestBuilders.post(ApiPathConstant.ACTIVITY_ADD_ASSIGNEE_API_PATH)
                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                .param("activityId", TestConstant.OTHER_PRIVATE_REPOSITORY_ACTIVITY_ID)
                .param("assigneeId", TestConstant.ID))
        .andExpectAll(status().isNotFound());
  }

  @Test
  @Order(Ordered.HIGHEST_PRECEDENCE + 3)
  public void testPageActivityAssigneesValid() throws Exception {
    var content =
        mvc.perform(
                MockMvcRequestBuilders.get(ApiPathConstant.ACTIVITY_PAGE_ASSIGNEE_API_PATH)
                    .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                    .param("activityId", TestConstant.REPOSITORY_ACTIVITY_ID)
                    .param("page", "1")
                    .param("size", "10"))
            .andExpectAll(
                status().isOk(),
                jsonPath("$.total").value(greaterThan(0)),
                jsonPath("$.records").isArray(),
                jsonPath("$.records.length()").value(1),
                jsonPath("$.records[0].assigneeId").value(TestConstant.OTHER_ID),
                jsonPath("$.records[0].username").value(TestConstant.OTHER_USERNAME),
                jsonPath("$.records[0].email").value(TestConstant.OTHER_EMAIL))
            .andReturn()
            .getResponse()
            .getContentAsString();
    var pageVO = objectMapper.readValue(content, new TypeReference<PageVO<AssigneeVO>>() {});
    TestConstant.ASSIGNEE_ID = pageVO.records().get(0).id();
  }

  @Test
  public void testPageOtherActivityAssigneesInvalid() throws Exception {
    // page other's private repository activity assignees
    mvc.perform(
            MockMvcRequestBuilders.get(ApiPathConstant.ACTIVITY_PAGE_ASSIGNEE_API_PATH)
                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                .param("activityId", TestConstant.OTHER_PRIVATE_REPOSITORY_ACTIVITY_ID)
                .param("page", "1")
                .param("size", "10"))
        .andExpectAll(status().isNotFound());
  }

  @Test
  public void testRemoveActivityAssigneeInvalid() throws Exception {
    // remove other's public repository activity assignee
    mvc.perform(
            delete(ApiPathConstant.ACTIVITY_DELETE_ASSIGNEE_API_PATH)
                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.OTHER_ACCESS_TOKEN)
                .param("id", TestConstant.ASSIGNEE_ID))
        .andExpectAll(status().isForbidden());
  }

  @Test
  @Order(Ordered.LOWEST_PRECEDENCE - 1)
  public void testRemoveActivityAssigneeValid() throws Exception {
    mvc.perform(
            delete(ApiPathConstant.ACTIVITY_DELETE_ASSIGNEE_API_PATH)
                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                .param("id", TestConstant.ASSIGNEE_ID))
        .andExpect(status().isOk());
    TestConstant.ASSIGNEE_ID = null;
  }

  @Test
  @Order(Ordered.HIGHEST_PRECEDENCE + 4)
  public void testAddLabelValid() throws Exception {
    mvc.perform(
            post(ApiPathConstant.ACTIVITY_ADD_LABEL_API_PATH)
                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                .param("activityId", TestConstant.REPOSITORY_ACTIVITY_ID)
                .param("labelId", TestConstant.LABEL_ID))
        .andExpectAll(status().isOk());
  }

  @Test
  public void testAddLabelInvalid() throws Exception {
    // label does not exist in repository
    mvc.perform(
            post(ApiPathConstant.ACTIVITY_ADD_LABEL_API_PATH)
                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                .param("activityId", TestConstant.REPOSITORY_ACTIVITY_ID)
                .param("labelId", "1234567890"))
        .andExpectAll(status().isNotFound());

    // label has been added to activity
    mvc.perform(
            post(ApiPathConstant.ACTIVITY_ADD_LABEL_API_PATH)
                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                .param("activityId", TestConstant.REPOSITORY_ACTIVITY_ID)
                .param("labelId", TestConstant.LABEL_ID))
        .andExpectAll(status().isBadRequest()); // ErrorCodeEnum.ACTIVITY_LABEL_ALREADY_EXISTS

    // add label to other's public repository activity
    mvc.perform(
            post(ApiPathConstant.ACTIVITY_ADD_LABEL_API_PATH)
                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                .param("activityId", TestConstant.OTHER_REPOSITORY_ACTIVITY_ID)
                .param("labelId", TestConstant.LABEL_ID))
        .andExpectAll(status().isForbidden());

    // add label to other's private repository activity
    mvc.perform(
            post(ApiPathConstant.ACTIVITY_ADD_LABEL_API_PATH)
                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                .param("activityId", TestConstant.OTHER_PRIVATE_REPOSITORY_ACTIVITY_ID)
                .param("labelId", TestConstant.LABEL_ID))
        .andExpectAll(status().isNotFound());

    // Used to verify that the label is removed when the activity is deleted
    mvc.perform(
            post(ApiPathConstant.ACTIVITY_ADD_LABEL_API_PATH)
                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                .param("activityId", TestConstant.REPOSITORY_DELETE_ACTIVITY_ID)
                .param("labelId", TestConstant.LABEL_ID))
        .andExpectAll(status().isOk());
  }

  @Test
  @Order(Ordered.HIGHEST_PRECEDENCE + 5)
  public void testPageActivityLabelValid() throws Exception {
    var content =
        mvc.perform(
                get(ApiPathConstant.ACTIVITY_PAGE_LABEL_API_PATH)
                    .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                    .param("activityId", TestConstant.REPOSITORY_ACTIVITY_ID)
                    .param("page", "1")
                    .param("size", "10"))
            .andExpectAll(
                status().isOk(),
                jsonPath("$.total").value(greaterThan(0)),
                jsonPath("$.records").isArray(),
                jsonPath("$.records.length()").value(1),
                jsonPath("$.records[0].id").isString(),
                jsonPath("$.records[0].userId").value(TestConstant.ID),
                jsonPath("$.records[0].labelId").value(TestConstant.LABEL_ID),
                jsonPath("$.records[0].labelName").value(TestConstant.LABEL_NAME),
                jsonPath("$.records[0].labelHexColor").value(TestConstant.LABEL_HEX_COLOR))
            .andReturn()
            .getResponse()
            .getContentAsString();
    var pageVO = objectMapper.readValue(content, new TypeReference<PageVO<ActivityDetailVO>>() {});
    TestConstant.REPOSITORY_ACTIVITY_LABEL_ID = pageVO.records().get(0).id();
  }

  @Test
  public void testPageActivityLabelInvalid() throws Exception {
    mvc.perform(
            get(ApiPathConstant.ACTIVITY_PAGE_LABEL_API_PATH)
                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                .param("activityId", TestConstant.OTHER_PRIVATE_REPOSITORY_ACTIVITY_ID)
                .param("page", "1")
                .param("size", "10"))
        .andExpectAll(status().isNotFound());
  }

  @Test
  public void testRemoveActivityLabelInvalid() throws Exception {
    mvc.perform(
            delete(ApiPathConstant.ACTIVITY_DELETE_LABEL_API_PATH)
                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.OTHER_ACCESS_TOKEN)
                .param("id", TestConstant.REPOSITORY_ACTIVITY_LABEL_ID))
        .andExpectAll(status().isForbidden());
  }

  @Test
  @Order(Ordered.LOWEST_PRECEDENCE - 1)
  public void testRemoveActivityLabelValid() throws Exception {
    mvc.perform(
            delete(ApiPathConstant.ACTIVITY_DELETE_LABEL_API_PATH)
                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                .param("id", TestConstant.REPOSITORY_ACTIVITY_LABEL_ID))
        .andExpectAll(status().isOk());
    TestConstant.REPOSITORY_ACTIVITY_LABEL_ID = null;
  }

  @Test
  @Order(Ordered.HIGHEST_PRECEDENCE + 6)
  public void testCreateActivityCommentValid() throws Exception {
    mvc.perform(
            post(ApiPathConstant.ACTIVITY_CREATE_COMMENT_API_PATH)
                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "activityId": "%s",
                        "content": "%s"
                    }
                    """
                        .formatted(
                            TestConstant.REPOSITORY_ACTIVITY_ID, TestConstant.COMMENT_CONTENT)))
        .andExpectAll(status().isOk());

    // Used to verify that the comment is removed when the activity is deleted
    mvc.perform(
            post(ApiPathConstant.ACTIVITY_CREATE_COMMENT_API_PATH)
                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "activityId": "%s",
                        "content": "%s"
                    }
                    """
                        .formatted(
                            TestConstant.REPOSITORY_DELETE_ACTIVITY_ID,
                            TestConstant.COMMENT_CONTENT)))
        .andExpectAll(status().isOk());
  }

  @Test
  public void testCreateActivityCommentInvalid() throws Exception {
    mvc.perform(
            post(ApiPathConstant.ACTIVITY_CREATE_COMMENT_API_PATH)
                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "activityId": "%s",
                        "content": "%s",
                        "codePath":"/path/to/code",
                        "codeLine": 10
                    }
                    """
                        .formatted(
                            TestConstant.OTHER_PRIVATE_REPOSITORY_ACTIVITY_ID,
                            TestConstant.COMMENT_CONTENT)))
        .andExpectAll(status().isNotFound());
  }

  @Test
  @Order(Ordered.HIGHEST_PRECEDENCE + 7)
  public void testPageActivityCommentValid() throws Exception {
    var content =
        mvc.perform(
                get(ApiPathConstant.ACTIVITY_PAGE_COMMENT_API_PATH)
                    .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                    .param("activityId", TestConstant.REPOSITORY_ACTIVITY_ID)
                    .param("page", "1")
                    .param("size", "10"))
            .andExpectAll(
                status().isOk(),
                jsonPath("$.total").value(greaterThan(0)),
                jsonPath("$.records").isArray(),
                jsonPath("$.records.length()").value(1),
                jsonPath("$.records[0].id").isString(),
                jsonPath("$.records[0].content").value(TestConstant.COMMENT_CONTENT),
                jsonPath("$.records[0].userId").value(TestConstant.ID))
            .andReturn()
            .getResponse()
            .getContentAsString();
    var pageVO = objectMapper.readValue(content, new TypeReference<PageVO<ActivityDetailVO>>() {});
    TestConstant.COMMENT_ID = pageVO.records().get(0).id();
  }

  @Test
  public void testPageOtherPrivateActivityCommentInvalid() throws Exception {
    mvc.perform(
            get(ApiPathConstant.ACTIVITY_PAGE_COMMENT_API_PATH)
                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                .param("activityId", TestConstant.OTHER_PRIVATE_REPOSITORY_ACTIVITY_ID)
                .param("page", "1")
                .param("size", "10"))
        .andExpectAll(status().isNotFound());
  }

  @Test
  @Order(Ordered.HIGHEST_PRECEDENCE + 8)
  public void testUpdateActivityCommentContentValid() throws Exception {
    String newContent = TestConstant.COMMENT_CONTENT + " updated";
    mvc.perform(
            post(ApiPathConstant.ACTIVITY_UPDATE_COMMENT_CONTENT_API_PATH)
                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "id": "%s",
                        "content": "%s",
                        "codePath":"/path/to/code",
                        "codeLine": 10
                    }
                    """
                        .formatted(TestConstant.COMMENT_ID, newContent)))
        .andExpectAll(status().isOk());
    TestConstant.COMMENT_CONTENT = newContent;
  }

  @Test
  public void testUpdateOtherActivityCommentContentInvalid() throws Exception {
    String newContent = TestConstant.COMMENT_CONTENT + " updated";
    mvc.perform(
            post(ApiPathConstant.ACTIVITY_UPDATE_COMMENT_CONTENT_API_PATH)
                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "id": "%s",
                        "content": "%s",
                        "codePath":"/path/to/code",
                        "codeLine": 10
                    }
                    """
                        .formatted(TestConstant.OTHER_PRIVATE_REPOSITORY_ACTIVITY_ID, newContent)))
        .andExpectAll(status().isNotFound());
  }

  @Test
  @Order(Ordered.HIGHEST_PRECEDENCE + 9)
  public void testUpdateActivityCommentHiddenStatusValid() throws Exception {
    mvc.perform(
            post(ApiPathConstant.ACTIVITY_UPDATE_COMMENT_HIDDEN_STATE_API_PATH)
                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                .param("id", TestConstant.COMMENT_ID)
                .param("isHidden", "true"))
        .andExpectAll(status().isOk());
    // Verify method idempotency
    CommentPO comment1 = commentService.getById(TestConstant.COMMENT_ID);
    mvc.perform(
                    post(ApiPathConstant.ACTIVITY_UPDATE_COMMENT_HIDDEN_STATE_API_PATH)
                            .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                            .param("id", TestConstant.COMMENT_ID)
                            .param("isHidden", "true"))
            .andExpectAll(status().isOk());
    CommentPO comment2 = commentService.getById(TestConstant.COMMENT_ID);
    Assertions.assertEquals(comment1.getGmtHidden(), comment2.getGmtHidden());

  }

  @Test
  public void testUpdateActivityCommentHiddenStatusInvalid() throws Exception {
    mvc.perform(
            post(ApiPathConstant.ACTIVITY_UPDATE_COMMENT_HIDDEN_STATE_API_PATH)
                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                .param("id", "123")
            .param("isHidden", "true"))
        .andExpectAll(status().isNotFound());
  }

  @Test
  @Order(Ordered.HIGHEST_PRECEDENCE + 10)
  public void testUpdateActivityCommentResolvedStatusValid() throws Exception {
    mvc.perform(
            post(ApiPathConstant.ACTIVITY_UPDATE_COMMENT_RESOLVED_STATE_API_PATH)
                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                .param("id", TestConstant.COMMENT_ID)
                .param("isResolved", "true"))
        .andExpectAll(status().isOk());
    // Verify method idempotency
    CommentPO comment1 = commentService.getById(TestConstant.COMMENT_ID);
    mvc.perform(
              post(ApiPathConstant.ACTIVITY_UPDATE_COMMENT_RESOLVED_STATE_API_PATH)
                  .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                  .param("id", TestConstant.COMMENT_ID)
                  .param("isResolved", "true"))
            .andExpectAll(status().isOk());
    CommentPO comment2 = commentService.getById(TestConstant.COMMENT_ID);
    Assertions.assertEquals(comment1.getGmtResolved(), comment2.getGmtResolved());
  }

  @Test
    public void testUpdateActivityCommentResolvedStatusInvalid() throws Exception {
        mvc.perform(
                post(ApiPathConstant.ACTIVITY_UPDATE_COMMENT_RESOLVED_STATE_API_PATH)
                    .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                    .param("id", "123")
                    .param("isResolved", "true"))
            .andExpectAll(status().isNotFound());
    }

  @Test
  @Order(Ordered.LOWEST_PRECEDENCE - 1)
  public void testDeleteActivityCommentValid() throws Exception {
    mvc.perform(
            delete(ApiPathConstant.ACTIVITY_DELETE_COMMENT_API_PATH)
                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                .param("id", TestConstant.COMMENT_ID))
        .andExpectAll(status().isOk());
    TestConstant.COMMENT_ID = null;
  }

  @Test
  public void testDeleteActivityCommentInvalid() throws Exception {
    mvc.perform(
            delete(ApiPathConstant.ACTIVITY_DELETE_COMMENT_API_PATH)
                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                .param("id", "123"))
        .andExpectAll(status().isNotFound());
  }

  @Test
  @Order(Ordered.LOWEST_PRECEDENCE - 1)
  public void testDeleteActivityValid() throws Exception {
    mvc.perform(
            delete(ApiPathConstant.ACTIVITY_DELETE_ACTIVITY_API_PATH)
                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                .param("id", TestConstant.REPOSITORY_DELETE_ACTIVITY_ID))
        .andExpectAll(status().isNotImplemented());

    //    //  Verify that comments、labels and assignees are removed when the activity is deleted
    //    long CommentCnt =
    //        commentService.count(
    //            new QueryWrapper<CommentPO>()
    //                .eq("activity_id", Long.valueOf(TestConstant.REPOSITORY_DELETE_ACTIVITY_ID)));
    //    long labelCnt =
    //        activityAssignLabelService.count(
    //            new QueryWrapper<ActivityAssignLabelPO>()
    //                .eq("activity_id", Long.valueOf(TestConstant.REPOSITORY_DELETE_ACTIVITY_ID)));
    //    long assigneeCnt =
    //        activityDesignateAssigneeService.count(
    //            new QueryWrapper<ActivityDesignateAssigneePO>()
    //                .eq("activity_id", Long.valueOf(TestConstant.REPOSITORY_DELETE_ACTIVITY_ID)));
    //    Assertions.assertEquals(0, CommentCnt);
    //    Assertions.assertEquals(0, labelCnt);
    //    Assertions.assertEquals(0, assigneeCnt);
    //    TestConstant.REPOSITORY_DELETE_ACTIVITY_ID = null;
    //    TestConstant.ACTIVITY_SIZE--;
  }

  @Test
  public void testDeleteOtherActivityInvalid() throws Exception {
    mvc.perform(
            delete(ApiPathConstant.ACTIVITY_DELETE_ACTIVITY_API_PATH)
                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                .param("id", TestConstant.OTHER_REPOSITORY_ACTIVITY_ID))
        .andExpect(status().isNotImplemented());
  }

  @Test
  @Order(Ordered.LOWEST_PRECEDENCE)
  public void testAssignActivityNumberValid() throws Exception {
    // create a new activity to update the activity latest number
    String activityDTO =
        """
        {
            "repositoryId": "%s",
            "title": "Test Activity",
            "description": "This is a test activity ,which is used to verify the activity number assignment",
            "isPullRequest": false
        }
        """
            .formatted(TestConstant.REPOSITORY_ID);
    var result = activityCreator.apply(TestConstant.ACCESS_TOKEN, activityDTO);
    if (result != null) {
      throw result;
    }
    //    TestConstant.ACTIVITY_SIZE++;  // Used in conjunction with deletion activity test
    // verify the number of new activity doesn't duplicate in the repository, includes the deleted
    // activity
    // the number should be equal to the activity size + 1
    mvc.perform(
            get(ApiPathConstant.ACTIVITY_GET_ACTIVITY_API_PATH)
                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                .param("activityNumber", String.valueOf(TestConstant.ACTIVITY_SIZE + 1))
                .param("repositoryId", TestConstant.REPOSITORY_ID))
        .andExpectAll(status().isOk());
  }
}
