package edu.cmipt.gcs.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.cmipt.gcs.constant.ApiPathConstant;
import edu.cmipt.gcs.constant.HeaderParameter;
import edu.cmipt.gcs.enumeration.ErrorCodeEnum;
import edu.cmipt.gcs.enumeration.TokenTypeEnum;
import edu.cmipt.gcs.exception.GenericException;
import edu.cmipt.gcs.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * JwtFilter
 *
 * <p>Filter to check the validity of the Access-Token
 *
 * @author Kaiser
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class JwtFilter extends OncePerRequestFilter {
  @Autowired ObjectMapper objectMapper;

  // Paths that do not need token
  private Map<String, Set<String>> ignorePath =
      Map.of(
          "GET",
              Set.of(
                  ApiPathConstant.DEVELOPMENT_GET_API_MAP_API_PATH,
                  ApiPathConstant.DEVELOPMENT_GET_ERROR_MESSAGE_API_PATH,
                  ApiPathConstant.DEVELOPMENT_GET_VO_AS_TS_API_PATH,
                  ApiPathConstant.USER_CHECK_USERNAME_VALIDITY_API_PATH,
                  ApiPathConstant.USER_CHECK_USER_PASSWORD_VALIDITY_API_PATH,
                  ApiPathConstant.USER_CHECK_EMAIL_VALIDITY_API_PATH,
                  ApiPathConstant.AUTHENTICATION_SEND_EMAIL_VERIFICATION_CODE_API_PATH),
          "POST",
              Set.of(
                  ApiPathConstant.AUTHENTICATION_SIGN_IN_API_PATH,
                  ApiPathConstant.USER_CREATE_USER_API_PATH,
                  ApiPathConstant.USER_UPDATE_USER_PASSWORD_WITH_EMAIL_VERIFICATION_CODE_API_PATH));

  // Paths that do not need authorization in filter
  private Map<String, Set<String>> passPath =
      Map.of(
          "GET",
              Set.of(
                  ApiPathConstant.AUTHENTICATION_REFRESH_API_PATH,
                  ApiPathConstant.SSH_KEY_CHECK_SSH_KEY_PUBLIC_KEY_VALIDITY_API_PATH,
                  ApiPathConstant.SSH_KEY_CHECK_SSH_KEY_NAME_VALIDITY_API_PATH,
                  ApiPathConstant.REPOSITORY_PAGE_REPOSITORY_API_PATH,
                  ApiPathConstant.USER_GET_USER_API_PATH,
                  ApiPathConstant.REPOSITORY_GET_REPOSITORY_API_PATH,
                  ApiPathConstant.REPOSITORY_GET_REPOSITORY_DIRECTORY_WITH_REF_API_PATH,
                  ApiPathConstant.REPOSITORY_GET_REPOSITORY_FILE_WITH_REF_API_PATH,
                  ApiPathConstant.REPOSITORY_GET_REPOSITORY_COMMIT_DETAILS_API_PATH,
                  ApiPathConstant.REPOSITORY_PAGE_COLLABORATOR_API_PATH,
                  ApiPathConstant.REPOSITORY_PAGE_COMMIT_WITH_REF_API_PATH,
                  ApiPathConstant.REPOSITORY_CHECK_REPOSITORY_NAME_VALIDITY_API_PATH,
                  ApiPathConstant.SSH_KEY_PAGE_SSH_KEY_API_PATH,
                  ApiPathConstant.ACTIVITY_GET_ACTIVITY_API_PATH,
                  ApiPathConstant.ACTIVITY_PAGE_COMMENT_API_PATH,
                  ApiPathConstant.ACTIVITY_PAGE_LABEL_API_PATH,
                  ApiPathConstant.ACTIVITY_PAGE_ASSIGNEE_API_PATH,
                  ApiPathConstant.REPOSITORY_PAGE_LABEL_API_PATH),
          "POST",
              Set.of(
                  ApiPathConstant.REPOSITORY_CREATE_REPOSITORY_API_PATH,
                  ApiPathConstant.REPOSITORY_UPDATE_REPOSITORY_API_PATH,
                  ApiPathConstant.REPOSITORY_ADD_COLLABORATOR_API_PATH,
                  ApiPathConstant.SSH_KEY_UPLOAD_SSH_KEY_API_PATH,
                  ApiPathConstant.SSH_KEY_UPDATE_SSH_KEY_API_PATH,
                  ApiPathConstant.USER_UPDATE_USER_API_PATH,
                  ApiPathConstant.USER_UPDATE_USER_PASSWORD_WITH_OLD_PASSWORD_API_PATH,
                  ApiPathConstant.ACTIVITY_CREATE_ACTIVITY_API_PATH,
                  ApiPathConstant.ACTIVITY_UPDATE_ACTIVITY_API_PATH,
                  ApiPathConstant.ACTIVITY_UPDATE_COMMENT_API_PATH,
                  ApiPathConstant.ACTIVITY_CREATE_COMMENT_API_PATH,
                  ApiPathConstant.ACTIVITY_PAGE_ACTIVITY_API_PATH,
                  ApiPathConstant.ACTIVITY_ADD_LABEL_API_PATH,
                  ApiPathConstant.ACTIVITY_ADD_ASSIGNEE_API_PATH,
                  ApiPathConstant.REPOSITORY_CREATE_LABEL_API_PATH,
                  ApiPathConstant.REPOSITORY_UPDATE_LABEL_API_PATH),
          "DELETE",
              Set.of(
                  ApiPathConstant.REPOSITORY_DELETE_REPOSITORY_API_PATH,
                  ApiPathConstant.REPOSITORY_REMOVE_COLLABORATION_API_PATH,
                  ApiPathConstant.SSH_KEY_DELETE_SSH_KEY_API_PATH,
                  ApiPathConstant.AUTHENTICATION_SIGN_OUT_API_PATH,
                  ApiPathConstant.USER_DELETE_USER_API_PATH,
                  ApiPathConstant.ACTIVITY_DELETE_ACTIVITY_API_PATH,
                  ApiPathConstant.ACTIVITY_DELETE_COMMENT_API_PATH,
                  ApiPathConstant.ACTIVITY_DELETE_LABEL_API_PATH,
                  ApiPathConstant.ACTIVITY_DELETE_ASSIGNEE_API_PATH,
                  ApiPathConstant.REPOSITORY_DELETE_LABEL_API_PATH));

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    var ignoreSet = ignorePath.get(request.getMethod());
    if (request.getRequestURI().startsWith(ApiPathConstant.ALL_API_PREFIX)
        && (ignoreSet == null || !ignoreSet.contains(request.getRequestURI()))) {
      // throw exception if authorization failed
      authorize(
          request,
          request.getHeader(HeaderParameter.ACCESS_TOKEN),
          request.getHeader(HeaderParameter.REFRESH_TOKEN));
    }
    filterChain.doFilter(request, response);
  }

  private void authorize(HttpServletRequest request, String accessToken, String refreshToken) {
    if (accessToken != null && JwtUtil.getTokenType(accessToken) != TokenTypeEnum.ACCESS_TOKEN
        || refreshToken != null
            && JwtUtil.getTokenType(refreshToken) != TokenTypeEnum.REFRESH_TOKEN) {
      throw new GenericException(ErrorCodeEnum.INVALID_TOKEN, accessToken);
    }
    var requestURI = request.getRequestURI();
    var requestMethod = request.getMethod();
    var passSet = passPath.get(requestMethod);
    if (passSet == null || !passSet.contains(requestURI)) {
      throw new GenericException(ErrorCodeEnum.ACCESS_DENIED);
    }
    if (requestURI.equals(ApiPathConstant.AUTHENTICATION_REFRESH_API_PATH)) {
      if (refreshToken == null) {
        throw new GenericException(ErrorCodeEnum.TOKEN_NOT_FOUND);
      }
      JwtUtil.refreshToken(refreshToken);
    } else {
      if (accessToken == null) {
        throw new GenericException(ErrorCodeEnum.TOKEN_NOT_FOUND);
      }
      JwtUtil.refreshToken(accessToken);
    }
    return;
  }
}
