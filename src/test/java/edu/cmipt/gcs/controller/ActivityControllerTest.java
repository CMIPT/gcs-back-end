package edu.cmipt.gcs.controller;

import static org.hamcrest.Matchers.greaterThan;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.cmipt.gcs.constant.ApiPathConstant;
import edu.cmipt.gcs.constant.ApplicationConstant;
import edu.cmipt.gcs.constant.HeaderParameter;
import edu.cmipt.gcs.constant.TestConstant;
import edu.cmipt.gcs.enumeration.ActivityOrderByEnum;
import edu.cmipt.gcs.enumeration.UserQueryTypeEnum;
import edu.cmipt.gcs.pojo.activity.ActivityFullInfoVO;
import edu.cmipt.gcs.pojo.activity.ActivityPO;
import edu.cmipt.gcs.pojo.assign.AssigneeVO;
import edu.cmipt.gcs.pojo.comment.CommentFullInfoVO;
import edu.cmipt.gcs.pojo.comment.CommentPO;
import edu.cmipt.gcs.pojo.other.PageVO;
import edu.cmipt.gcs.service.ActivityAssignLabelService;
import edu.cmipt.gcs.service.ActivityDesignateAssigneeService;
import edu.cmipt.gcs.service.ActivityService;
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
  @Autowired private ActivityService activityService;
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
                objectMapper.readValue(content, new TypeReference<PageVO<ActivityFullInfoVO>>() {});
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
            "repositoryId": "%s",
            "isPullRequest": false
        }
        """
            .formatted(
                TestConstant.REPOSITORY_ID);
    var result1 = activityPager.apply(TestConstant.ACCESS_TOKEN, activityQueryDTO);
    if (result1 != null) {
      throw result1;
    }
    var otherActivityQueryDTO =
        """
        {
            "repositoryId": "%s",
            "isPullRequest": false
        }
        """
            .formatted(
                TestConstant.OTHER_REPOSITORY_ID);
    var result2 = activityPager.apply(TestConstant.OTHER_ACCESS_TOKEN, otherActivityQueryDTO);
    if (result2 != null) {
      throw result2;
    }

    var otherPrivateActivityQueryDTO =
        """
        {
            "repositoryId": "%s",
            "isPullRequest": false
        }
        """
            .formatted(
                TestConstant.OTHER_PRIVATE_REPOSITORY_ID);
    var result3 =
        activityPager.apply(TestConstant.OTHER_ACCESS_TOKEN, otherPrivateActivityQueryDTO);
    if (result3 != null) {
      throw result3;
    }
  }

  @Test
  public void testPageActivityFullInfoValid() throws Exception {
    var result =
        activityPager.apply(
            TestConstant.ACCESS_TOKEN,
            """
            {
                "repositoryId": "%s",
                "author": "%s",
                "isPullRequest": false,
                "orderBy": "%s",
                "isAsc": true
            }
            """
                .formatted(
                    TestConstant.REPOSITORY_ID,
                    TestConstant.USERNAME,
                    ActivityOrderByEnum.GMT_CREATED.name()));
    if (result != null) {
      throw result;
    }
  }

  @Test
  public void testPageOtherUserRepositoryActivityFullInfoValid() throws Exception {
    mvc.perform(
            post(ApiPathConstant.ACTIVITY_PAGE_ACTIVITY_API_PATH)
                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.OTHER_ACCESS_TOKEN)
                .param("page", "1")
                .param("size", TestConstant.ACTIVITY_SIZE.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "repositoryId": "%s",
                        "author": "%s",
                        "isPullRequest": false,
                        "orderBy": "%s",
                        "isAsc": true
                    }
                    """
                        .formatted(
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
  @Order(Ordered.HIGHEST_PRECEDENCE)
  public void testCreateActivityInOtherUserPublicRepositoryValid() throws Exception {
    mvc.perform(
            post(ApiPathConstant.ACTIVITY_CREATE_ACTIVITY_API_PATH)
                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "repositoryId": "%s",
                        "title": "Test Activity in Other User Public Repository",
                        "description": "This is a test activity in other user public repository",
                        "isPullRequest": false
                    }
                    """
                        .formatted(TestConstant.OTHER_REPOSITORY_ID)))
        .andExpect(status().isOk());
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
  public void testGetActivityFullInfoValid() throws Exception {
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
            jsonPath("$.creator.id").value(TestConstant.ID),
            jsonPath("$.creator.username").value(TestConstant.USERNAME),
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
            jsonPath("$.creator.id").value(TestConstant.ID),
            jsonPath("$.creator.username").value(TestConstant.USERNAME),
            jsonPath("$.labels").isArray(),
            jsonPath("$.assignees").isArray(),
            jsonPath("$.commentCnt").value(1),
            jsonPath("$.gmtCreated").isString());
  }

  @Test
  public void testGetActivityFullInfoInvalid() throws Exception {
    mvc.perform(
            get(ApiPathConstant.ACTIVITY_GET_ACTIVITY_API_PATH)
                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                .param("id", "123"))
        .andExpectAll(status().isNotFound());
  }

  @Test
  @Order(Ordered.HIGHEST_PRECEDENCE + 1)
  public void testUpdateActivityContentValid() throws Exception {
    String newDescription = "This is an updated test description";
    mvc.perform(
            post(ApiPathConstant.ACTIVITY_UPDATE_ACTIVITY_CONTENT_API_PATH)
                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "id": "%s",
                        "description": "%s"
                    }
                    """
                        .formatted(TestConstant.REPOSITORY_ACTIVITY_ID, newDescription)))
        .andExpectAll(status().isOk());
  }

  @Test
  public void testUpdateOtherRepositoryActivityFullInfoInvalid() throws Exception {
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
                        "description": "%s"
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
                        "description": "%s"
                    }
                    """
                        .formatted(
                            TestConstant.OTHER_PRIVATE_REPOSITORY_ACTIVITY_ID, newDescription)))
        .andExpect(status().isNotFound());
  }

  @Test
  @Order(Ordered.HIGHEST_PRECEDENCE + 2)
  public void testAddAssigneeToActivityValid() throws Exception {
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
  public void testAddAssigneeToActivityInvalid() throws Exception {
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
  public void testPageAssigneesOfActivityValid() throws Exception {
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
  public void testPageAssigneesOfOtherActivityInvalid() throws Exception {
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
  public void testRemoveAssigneeFromActivityInvalid() throws Exception {
    // remove other's public repository activity assignee
    mvc.perform(
            delete(ApiPathConstant.ACTIVITY_REMOVE_ASSIGNEE_API_PATH)
                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.OTHER_ACCESS_TOKEN)
                .param("id", TestConstant.ASSIGNEE_ID))
        .andExpectAll(status().isForbidden());
  }

  @Test
  @Order(Ordered.LOWEST_PRECEDENCE - 1)
  public void testRemoveAssigneeFromActivityValid() throws Exception {
    mvc.perform(
            delete(ApiPathConstant.ACTIVITY_REMOVE_ASSIGNEE_API_PATH)
                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                .param("id", TestConstant.ASSIGNEE_ID))
        .andExpect(status().isOk());
    TestConstant.ASSIGNEE_ID = null;
  }

  @Test
  @Order(Ordered.HIGHEST_PRECEDENCE + 4)
  public void testAddLabelToActivityValid() throws Exception {
    mvc.perform(
            post(ApiPathConstant.ACTIVITY_ADD_LABEL_API_PATH)
                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                .param("activityId", TestConstant.REPOSITORY_ACTIVITY_ID)
                .param("labelId", TestConstant.LABEL_ID))
        .andExpectAll(status().isOk());

    // Used to verify that the label is removed when the activity is deleted
    mvc.perform(
                    post(ApiPathConstant.ACTIVITY_ADD_LABEL_API_PATH)
                            .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                            .param("activityId", TestConstant.REPOSITORY_DELETE_ACTIVITY_ID)
                            .param("labelId", TestConstant.LABEL_ID))
            .andExpectAll(status().isOk());
  }

  @Test
  public void testAddLabelToActivityInvalid() throws Exception {
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
        .andExpectAll(status().isBadRequest());

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
  }

  @Test
  @Order(Ordered.HIGHEST_PRECEDENCE + 5)
  public void testPageLabelOfActivityValid() throws Exception {
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
    var pageVO =
        objectMapper.readValue(content, new TypeReference<PageVO<ActivityFullInfoVO>>() {});
    TestConstant.REPOSITORY_ACTIVITY_LABEL_ID = pageVO.records().get(0).id();
  }

  @Test
  public void testPageLabelOfActivityInvalid() throws Exception {
    mvc.perform(
            get(ApiPathConstant.ACTIVITY_PAGE_LABEL_API_PATH)
                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                .param("activityId", TestConstant.OTHER_PRIVATE_REPOSITORY_ACTIVITY_ID)
                .param("page", "1")
                .param("size", "10"))
        .andExpectAll(status().isNotFound());
  }

  @Test
  public void testRemoveLabelFromActivityInvalid() throws Exception {
    mvc.perform(
            delete(ApiPathConstant.ACTIVITY_REMOVE_LABEL_API_PATH)
                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.OTHER_ACCESS_TOKEN)
                .param("id", TestConstant.REPOSITORY_ACTIVITY_LABEL_ID))
        .andExpectAll(status().isForbidden());
  }

  @Test
  @Order(Ordered.LOWEST_PRECEDENCE - 1)
  public void testRemoveLabelFromActivityValid() throws Exception {
    mvc.perform(
            delete(ApiPathConstant.ACTIVITY_REMOVE_LABEL_API_PATH)
                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                .param("id", TestConstant.REPOSITORY_ACTIVITY_LABEL_ID))
        .andExpectAll(status().isOk());
    TestConstant.REPOSITORY_ACTIVITY_LABEL_ID = null;
  }

  @Test
  @Order(Ordered.HIGHEST_PRECEDENCE + 6)
  public void testCreateCommentToActivityValid() throws Exception {
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
  public void testCreateCommentToActivityInvalid() throws Exception {
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
  @Order(Ordered.HIGHEST_PRECEDENCE + 6)
  public void testCreateCommentToOtherPublicRepositoryActivityValid() throws Exception {
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
                            TestConstant.OTHER_REPOSITORY_ACTIVITY_ID,
                            TestConstant.COMMENT_CONTENT)))
        .andExpectAll(status().isOk());
    // get the comment id for testing delete own comment in other public repository activity
    var wrapper = new QueryWrapper<CommentPO>()
        .eq("activity_id", Long.valueOf(TestConstant.OTHER_REPOSITORY_ACTIVITY_ID))
        .eq("creator_id", Long.valueOf(TestConstant.ID))
        .last("limit 1");
    CommentPO commentPO = commentService.getOne(wrapper);
    TestConstant.OTHER_COMMENT_ID = commentPO.getId().toString();
  }

  @Test
  @Order(Ordered.HIGHEST_PRECEDENCE + 7)
  public void testPageCommentOfActivityValid() throws Exception {
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
                jsonPath("$.records[0].creator.id").value(TestConstant.ID))
            .andReturn()
            .getResponse()
            .getContentAsString();
    var pageVO = objectMapper.readValue(content, new TypeReference<PageVO<CommentFullInfoVO>>() {});
    TestConstant.COMMENT_ID = pageVO.records().get(0).id();
  }

  @Test
  public void testPageCommentToOtherPrivateActivityInvalid() throws Exception {
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
  public void testUpdateCommentContentOfActivityValid() throws Exception {
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
  public void testUpdateCommentContentOfOtherActivityInvalid() throws Exception {
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
  public void testUpdateCommentHiddenStatusOfActivityValid() throws Exception {
    mvc.perform(
            post(ApiPathConstant.ACTIVITY_UPDATE_COMMENT_HIDDEN_STATE_API_PATH)
                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                .param("id", TestConstant.COMMENT_ID)
                .param("isHidden", "true"))
        .andExpectAll(status().isOk());
    // verify method idempotency
    CommentPO comment1 = commentService.getById(Long.valueOf(TestConstant.COMMENT_ID));
    mvc.perform(
            post(ApiPathConstant.ACTIVITY_UPDATE_COMMENT_HIDDEN_STATE_API_PATH)
                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                .param("id", TestConstant.COMMENT_ID)
                .param("isHidden", "true"))
        .andExpectAll(status().isOk());
    CommentPO comment2 = commentService.getById(Long.valueOf(TestConstant.COMMENT_ID));
    Assertions.assertEquals(comment1.getGmtHidden(), comment2.getGmtHidden());
  }

  @Test
  public void testUpdateCommentHiddenStatusOfActivityInvalid() throws Exception {
    mvc.perform(
            post(ApiPathConstant.ACTIVITY_UPDATE_COMMENT_HIDDEN_STATE_API_PATH)
                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                .param("id", "123")
                .param("isHidden", "true"))
        .andExpectAll(status().isNotFound());
  }

  @Test
  @Order(Ordered.HIGHEST_PRECEDENCE + 10)
  public void testUpdateCommentResolvedStatusOfActivityValid() throws Exception {
    mvc.perform(
            post(ApiPathConstant.ACTIVITY_UPDATE_COMMENT_RESOLVED_STATE_API_PATH)
                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                .param("id", TestConstant.COMMENT_ID)
                .param("isResolved", "true"))
        .andExpectAll(status().isOk());
    // verify method idempotency
    CommentPO comment1 = commentService.getById(Long.valueOf(TestConstant.COMMENT_ID));
    mvc.perform(
            post(ApiPathConstant.ACTIVITY_UPDATE_COMMENT_RESOLVED_STATE_API_PATH)
                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                .param("id", TestConstant.COMMENT_ID)
                .param("isResolved", "true"))
        .andExpectAll(status().isOk());
    CommentPO comment2 = commentService.getById(Long.valueOf(TestConstant.COMMENT_ID));
    Assertions.assertEquals(comment1.getGmtResolved(), comment2.getGmtResolved());
  }

  @Test
  public void testUpdateCommentResolvedStatusOfActivityInvalid() throws Exception {
    mvc.perform(
            post(ApiPathConstant.ACTIVITY_UPDATE_COMMENT_RESOLVED_STATE_API_PATH)
                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                .param("id", "123")
                .param("isResolved", "true"))
        .andExpectAll(status().isNotFound());
  }

  @Test
  @Order(Ordered.HIGHEST_PRECEDENCE + 11)
  public void testCreateSubIssueToIssueValid() throws Exception {
    mvc.perform(
            post(ApiPathConstant.ACTIVITY_CREATE_SUB_ISSUE_API_PATH)
                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "repositoryId": "%s",
                        "title": "Test Sub Issue",
                        "description": "This is a test sub issue",
                        "isPullRequest": false,
                        "parentId": "%s"
                    }
                    """
                        .formatted(
                            TestConstant.REPOSITORY_ID,
                            TestConstant.REPOSITORY_DELETE_ACTIVITY_ID)))
            .andExpectAll(status().isNotImplemented());
//        .andExpectAll(status().isOk());
  }

  @Test
  public void testCreateSubIssueToIssueInvalid() throws Exception {
    // create sub issue in other's public repository activity
    mvc.perform(
            post(ApiPathConstant.ACTIVITY_CREATE_SUB_ISSUE_API_PATH)
                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "repositoryId": "%s",
                        "title": "Test Sub Issue",
                        "description": "This is a test sub issue",
                        "isPullRequest": false,
                        "parentId": "%s"
                    }
                    """
                        .formatted(
                            TestConstant.REPOSITORY_ID, TestConstant.OTHER_REPOSITORY_ACTIVITY_ID)))
            .andExpectAll(status().isNotImplemented());
//        .andExpectAll(status().isForbidden());
  }

  @Test
  @Order(Ordered.HIGHEST_PRECEDENCE + 12)
  public void testAddSubIssueToIssueValid() throws Exception {
    mvc.perform(
            post(ApiPathConstant.ACTIVITY_ADD_SUB_ISSUE_API_PATH)
                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                .param("parentId", TestConstant.REPOSITORY_ACTIVITY_ID)
                .param("subIssueId", TestConstant.REPOSITORY_DELETE_ACTIVITY_ID))
            .andExpectAll(status().isNotImplemented());
//        .andExpectAll(status().isOk());
  }

  @Test
  public void testAddSubIssueToIssueInvalid() throws Exception {
    // add sub issue to other's public repository activity
    mvc.perform(
            post(ApiPathConstant.ACTIVITY_ADD_SUB_ISSUE_API_PATH)
                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                .param("parentId", TestConstant.OTHER_REPOSITORY_ACTIVITY_ID)
                .param("subIssueId", TestConstant.REPOSITORY_DELETE_ACTIVITY_ID))
            .andExpectAll(status().isNotImplemented());
//        .andExpectAll(status().isForbidden());
    // add itself as sub issue
    mvc.perform(
            post(ApiPathConstant.ACTIVITY_ADD_SUB_ISSUE_API_PATH)
                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                .param("parentId", TestConstant.REPOSITORY_ACTIVITY_ID)
                .param("subIssueId", TestConstant.REPOSITORY_ACTIVITY_ID))
            .andExpectAll(status().isNotImplemented());
//        .andExpectAll(status().isBadRequest());
  }

  @Test
  @Order(Ordered.HIGHEST_PRECEDENCE + 13)
  public void testPageSubIssuesOfIssueValid() throws Exception {
    mvc.perform(
            get(ApiPathConstant.ACTIVITY_PAGE_SUB_ISSUE_API_PATH)
                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                .param("page", "1")
                .param("size", "10")
                .param("parentId", TestConstant.REPOSITORY_ACTIVITY_ID))
            .andExpectAll(status().isNotImplemented());
//        .andExpectAll(
//            status().isOk(),
//            jsonPath("$.total").value(greaterThan(0)),
//            jsonPath("$.records").isArray(),
//            jsonPath("$.records.length()").value(1),
//            jsonPath("$.records[0].id").value(TestConstant.REPOSITORY_DELETE_ACTIVITY_ID),
//            jsonPath("$.records[0].number").isString(),
//            jsonPath("$.records[0].title").isString(),
//            jsonPath("$.records[0].description").isString(),
//            jsonPath("$.records[0].creator.username").value(TestConstant.USERNAME),
//            jsonPath("$.records[0].subIssueTotalCount").value(1),
//            jsonPath("$.records[0].subIssueCompletedCount").value(0));
  }

  @Test
  public void testPageSubIssuesOfIssueInvalid() throws Exception {
    // page sub issues in other's private repository activity
    mvc.perform(
            get(ApiPathConstant.ACTIVITY_PAGE_SUB_ISSUE_API_PATH)
                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                .param("page", "1")
                .param("size", "10")
                .param("parentId", TestConstant.OTHER_PRIVATE_REPOSITORY_ACTIVITY_ID))
            .andExpectAll(status().isNotImplemented());
//        .andExpectAll(status().isNotFound());
  }

  @Test
  @Order(Ordered.HIGHEST_PRECEDENCE + 14)
  public void testUpdateLockStateOfActivityValid() throws Exception {
    mvc.perform(
            post(ApiPathConstant.ACTIVITY_UPDATE_ACTIVITY_LOCKED_STATE_API_PATH)
                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                .param("id", TestConstant.REPOSITORY_DELETE_ACTIVITY_ID)
                .param("isLocked", "true"))
        .andExpectAll(status().isOk());
    // verify method idempotency
    ActivityPO activity1 =
        activityService.getById(Long.valueOf(TestConstant.REPOSITORY_DELETE_ACTIVITY_ID));
    mvc.perform(
            post(ApiPathConstant.ACTIVITY_UPDATE_ACTIVITY_LOCKED_STATE_API_PATH)
                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                .param("id", TestConstant.REPOSITORY_DELETE_ACTIVITY_ID)
                .param("isLocked", "true"))
        .andExpectAll(status().isOk());
    ActivityPO activity2 =
        activityService.getById(Long.valueOf(TestConstant.REPOSITORY_DELETE_ACTIVITY_ID));
    Assertions.assertEquals(activity1.getGmtLocked(), activity2.getGmtLocked());
  }

  @Test
  public void testUpdateLockStateOfActivityInvalid() throws Exception {
    mvc.perform(
            post(ApiPathConstant.ACTIVITY_UPDATE_ACTIVITY_LOCKED_STATE_API_PATH)
                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                .param("id", "123")
                .param("isLocked", "true"))
        .andExpectAll(status().isNotFound());
  }

  @Test
  @Order(Ordered.HIGHEST_PRECEDENCE + 15)
  public void testUpdateClosedStateOfActivityValid() throws Exception {
    mvc.perform(
            post(ApiPathConstant.ACTIVITY_UPDATE_ACTIVITY_CLOSED_STATE_API_PATH)
                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                .param("id", TestConstant.REPOSITORY_DELETE_ACTIVITY_ID)
                .param("isClosed", "true"))
        .andExpectAll(status().isOk());
    // verify method idempotency
    ActivityPO activity1 =
        activityService.getById(Long.valueOf(TestConstant.REPOSITORY_DELETE_ACTIVITY_ID));
    mvc.perform(
            post(ApiPathConstant.ACTIVITY_UPDATE_ACTIVITY_CLOSED_STATE_API_PATH)
                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                .param("id", TestConstant.REPOSITORY_DELETE_ACTIVITY_ID)
                .param("isClosed", "true"))
        .andExpectAll(status().isOk());
    ActivityPO activity2 =
        activityService.getById(Long.valueOf(TestConstant.REPOSITORY_DELETE_ACTIVITY_ID));
    Assertions.assertEquals(activity1.getGmtClosed(), activity2.getGmtClosed());
  }

  @Test
  public void testUpdateClosedStateOfActivityInvalid() throws Exception {
    mvc.perform(
            post(ApiPathConstant.ACTIVITY_UPDATE_ACTIVITY_CLOSED_STATE_API_PATH)
                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                .param("id", "123")
                .param("isClosed", "true"))
        .andExpectAll(status().isNotFound());
  }

  @Test
  @Order(Ordered.HIGHEST_PRECEDENCE + 16)
  public void testCreateCommentToLockedActivityValid() throws Exception {
    // activity creator is allowed to create comment to locked activity
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
  @Order(Ordered.HIGHEST_PRECEDENCE + 17)
  public void testCreateCommentToLockedActivityInvalid() throws Exception {
    // if user isn't repository or collaborators
    // he is not allowed to create comment to closed activity
    mvc.perform(
            post(ApiPathConstant.ACTIVITY_CREATE_COMMENT_API_PATH)
                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.OTHER_ACCESS_TOKEN)
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
        .andExpectAll(status().isForbidden());
  }

  @Test
  @Order(Ordered.HIGHEST_PRECEDENCE + 18)
  public void testCheckActivityOperationValidityValid() throws Exception {
    mvc.perform(
            get(ApiPathConstant.ACTIVITY_CHECK_OPERATION_VALIDITY_API_PATH)
                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                .param("id", TestConstant.REPOSITORY_ACTIVITY_ID)
                .param("operationType", "MODIFY_ACTIVITY"))
        .andExpectAll(status().isOk());
  }

  @Test
  public void testCheckActivityOperationValidityInvalid() throws Exception {
    mvc.perform(
            get(ApiPathConstant.ACTIVITY_CHECK_OPERATION_VALIDITY_API_PATH)
                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.OTHER_ACCESS_TOKEN)
                .param("id", TestConstant.REPOSITORY_ACTIVITY_ID)
                .param("operationType", "MODIFY_ACTIVITY"))
        .andExpectAll(status().isForbidden());
  }

  @Test
  @Order(Ordered.HIGHEST_PRECEDENCE + 19)
  public void testCheckCommentOperationValidityValid() throws Exception {
    mvc.perform(
            get(ApiPathConstant.ACTIVITY_CHECK_COMMENT_OPERATION_VALIDITY_API_PATH)
                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                .param("id", TestConstant.COMMENT_ID)
                .param("operationType", "MODIFY_COMMENT"))
        .andExpectAll(status().isOk());
  }

  @Test
  public void testCheckCommentOperationValidityInvalid() throws Exception {
    mvc.perform(
            get(ApiPathConstant.ACTIVITY_CHECK_COMMENT_OPERATION_VALIDITY_API_PATH)
                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.OTHER_ACCESS_TOKEN)
                .param("id", TestConstant.COMMENT_ID)
                .param("operationType", "MODIFY_COMMENT"))
        .andExpectAll(status().isForbidden());
  }

  @Order(Ordered.LOWEST_PRECEDENCE - 2)
  public void testRemoveSubIssueFromIssueValid() throws Exception {
    mvc.perform(
            delete(ApiPathConstant.ACTIVITY_REMOVE_SUB_ISSUE_API_PATH)
                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                .param("subIssueId", TestConstant.REPOSITORY_DELETE_ACTIVITY_ID))
            .andExpectAll(status().isNotImplemented());
//        .andExpectAll(status().isOk());
  }

  @Test
  public void testRemoveSubIssueFromIssueInvalid() throws Exception {
    // remove sub issue from other's public repository activity
    mvc.perform(
            delete(ApiPathConstant.ACTIVITY_REMOVE_SUB_ISSUE_API_PATH)
                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                .param("subIssueId", TestConstant.OTHER_REPOSITORY_ACTIVITY_ID))
            .andExpectAll(status().isNotImplemented());
//        .andExpectAll(status().isForbidden());
  }

  @Test
  @Order(Ordered.LOWEST_PRECEDENCE - 1)
  public void testDeleteCommentFromActivityValid() throws Exception {
    mvc.perform(
            delete(ApiPathConstant.ACTIVITY_DELETE_COMMENT_API_PATH)
                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                .param("id", TestConstant.COMMENT_ID))
        .andExpectAll(status().isOk());
    TestConstant.COMMENT_ID = null;
  }

  @Test
  @Order(Ordered.LOWEST_PRECEDENCE - 1)
  public void testDeleteOtherCommentFromOtherPublicRepositoryActivityValid() throws Exception {
    mvc.perform(
            delete(ApiPathConstant.ACTIVITY_DELETE_COMMENT_API_PATH)
                .header(HeaderParameter.ACCESS_TOKEN, TestConstant.ACCESS_TOKEN)
                .param("id", TestConstant.OTHER_COMMENT_ID))
        .andExpectAll(status().isOk());
    TestConstant.OTHER_COMMENT_ID = null;
  }

  @Test
  public void testDeleteCommentFromActivityInvalid() throws Exception {
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

    //    //  verify that comments、labels and assignees are removed when the activity is deleted
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
