package tech.wetech.flexmodel.interfaces.rest.filter;

import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;
import tech.wetech.flexmodel.application.IdentityProviderApplicationService;
import tech.wetech.flexmodel.application.SettingsApplicationService;
import tech.wetech.flexmodel.codegen.entity.IdentityProvider;
import tech.wetech.flexmodel.domain.model.auth.AuthException;
import tech.wetech.flexmodel.domain.model.idp.provider.ValidateParam;
import tech.wetech.flexmodel.domain.model.idp.provider.ValidateResult;
import tech.wetech.flexmodel.domain.model.settings.Settings;
import tech.wetech.flexmodel.interfaces.rest.jwt.JwtUtil;
import tech.wetech.flexmodel.shared.SessionContextHolder;
import tech.wetech.flexmodel.shared.utils.JsonUtils;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author cjbi
 */
@Slf4j
@Provider
public class AuthFilter implements ContainerRequestFilter {

  @Context
  ResourceInfo resourceInfo;
  @Inject
  SettingsApplicationService settingsApplicationService;
  @Inject
  IdentityProviderApplicationService identityProviderApplicationService;

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
        // 默认使用内置的token，否则就使用idp提供的身份源验证
        if (!JwtUtil.verify(accessToken) && !varifyWithIdp(requestContext)) {
          throw new AuthException("Invalid token");
        }
        // 填充会话上下文
        fillSessionContext(requestContext);
      }
    }
  }

  /**
   * 使用idp验证
   *
   * @param requestContext 请求上下文
   * @return 是否验证成功
   */
  private boolean varifyWithIdp(ContainerRequestContext requestContext) {
    Settings.Security security = settingsApplicationService.getSettings().getSecurity();
    String systemIdentityProvider = security.getSystemIdentityProvider();
    IdentityProvider identityProvider = identityProviderApplicationService.find(systemIdentityProvider);
    if (identityProvider == null) {
      log.warn("system idp not found!");
      return false;
    }
    tech.wetech.flexmodel.domain.model.idp.provider.Provider provider = JsonUtils.getInstance().convertValue(identityProvider.getProvider(), tech.wetech.flexmodel.domain.model.idp.provider.Provider.class);
    ValidateParam param = new ValidateParam();
    Map<String, String> headers = new HashMap<>();
    requestContext.getHeaders().forEach((k, v) -> headers.put(k, v.getFirst()));
    Collection<String> propertyNames = requestContext.getPropertyNames();
    Map<String, Object> query = new HashMap<>();
    propertyNames.forEach(propertyName -> query.put(propertyName, requestContext.getProperty(propertyName)));
    param.setQuery(query);
    param.setHeaders(headers);
    ValidateResult result = provider.validate(param);
    return result.isSuccess();
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
