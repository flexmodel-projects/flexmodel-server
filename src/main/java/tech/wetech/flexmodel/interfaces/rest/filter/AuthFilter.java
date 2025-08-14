package tech.wetech.flexmodel.interfaces.rest.filter;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

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
//    if (requestContext.getUriInfo().getPath().startsWith(Resources.ROOT_PATH)) {
//      PermitAll permitAll = resourceInfo.getResourceMethod().getAnnotation(PermitAll.class);
//      if (permitAll == null) {
//        String accessToken = Objects.toString(requestContext.getHeaderString("Authorization"), "").replaceFirst("Bearer ", "");
//        if (accessToken.isEmpty()) {
//          throw new AuthException("Token is missing");
//        }
//        if (!JwtUtil.verify(accessToken)) {
//          throw new AuthException("Invalid token");
//        }
//      }
//    }
  }

}
