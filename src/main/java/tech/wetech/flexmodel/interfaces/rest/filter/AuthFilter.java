package tech.wetech.flexmodel.interfaces.rest.filter;

import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;
import tech.wetech.flexmodel.domain.model.auth.AuthException;
import tech.wetech.flexmodel.interfaces.rest.jwt.JwtUtil;
import tech.wetech.flexmodel.shared.SessionContextHolder;

import java.io.IOException;
import java.util.Objects;

/**
 * @author cjbi
 */
@Slf4j
@Provider
public class AuthFilter implements ContainerRequestFilter {

  @Context
  ResourceInfo resourceInfo;

  @Override
  public void filter(ContainerRequestContext requestContext) throws IOException {
    String path = requestContext.getUriInfo().getPath();
    boolean isFlexmodelPath = path.startsWith("/f/");
    if (isFlexmodelPath) {
      PermitAll permitAll = resourceInfo.getResourceMethod().getAnnotation(PermitAll.class);
      if (permitAll == null) {
        String accessToken = Objects.toString(requestContext.getHeaderString("Authorization"), "").replaceFirst("Bearer ", "");
        if (accessToken.isEmpty()) {
          throw new AuthException("Token is missing");
        }
        if (!JwtUtil.verify(accessToken)) {
          throw new AuthException("Invalid token");
        }
        // 填充会话上下文
        fillSessionContext(requestContext);
      }
    }
  }

  private void fillSessionContext(ContainerRequestContext requestContext) {
    String tenantId = requestContext.getHeaderString("X-Tenant-Id");
    String accessToken = Objects.toString(requestContext.getHeaderString("Authorization"), "")
      .replaceFirst("Bearer ", "");
    String userId = JwtUtil.getAccount(accessToken);
    SessionContextHolder.setTenantId(tenantId);
    SessionContextHolder.setUserId(userId);
    requestContext.setProperty("tenantId", tenantId);
    requestContext.setProperty("userId", userId);
  }

}
