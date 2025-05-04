package tech.wetech.flexmodel.interfaces.rest;

import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.ConfigProvider;
import tech.wetech.flexmodel.application.AuthApplicationService;
import tech.wetech.flexmodel.application.SettingsApplicationService;
import tech.wetech.flexmodel.codegen.entity.User;
import tech.wetech.flexmodel.interfaces.rest.jwt.JwtUtil;
import tech.wetech.flexmodel.interfaces.rest.response.UserinfoResponse;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * @author cjbi
 */
@Slf4j
@Path(Resources.ROOT_PATH + "/auth")
@Produces(MediaType.APPLICATION_JSON)
public class AuthResource {

  @Inject
  AuthApplicationService authApplicationService;

  @Inject
  SettingsApplicationService settingsApplicationService;

  @POST
  @Path("/login")
  @PermitAll
  public Response login(LoginRequest req) {
    User user = authApplicationService.login(req.username, req.password);
    // 签发 accessToken
    String accessToken = JwtUtil.sign(user.getId(), Duration.ofMinutes(5));
    // 签发 refreshToken
    String refreshToken = JwtUtil.sign(user.getId(), Duration.ofDays(7));

    NewCookie cookie = new NewCookie
      .Builder("refreshToken")
      .value(refreshToken)
      .httpOnly(true)
      .path("/")          // 根据前端路径决定
      .maxAge(7 * 24 * 3600)
      .build();
    return Response.ok(buildUserInfo(accessToken, user))
      .cookie(cookie).build();
  }

  @POST
  @Path("/refresh")
  @PermitAll
  public Response refresh(@CookieParam("refreshToken") String refreshToken) {
    if (refreshToken == null) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }
    try {
      String userId = JwtUtil.getClaim(refreshToken, JwtUtil.ACCOUNT);

      User user = authApplicationService.getUser(userId);
      // 签发新 accessToken
      String newAccess = JwtUtil.sign(userId, Duration.ofMinutes(5));

      //refresh Token 即将到期续约功能
      // 旋转 refreshToken：签发新 refreshToken 并更新存储
//    String newRefresh = Jwt.issuer(ui.getBaseUri().toString())
//      .upn("admin")
//      .expiresIn(Duration.ofDays(7))
//      .sign();
//
//    NewCookie newCookie = new NewCookie.Builder("refreshToken")
//      .value(newRefresh)
//      .httpOnly(true)
//      .path("/")
//      .maxAge(7 * 24 * 3600)
//      .build();

      return Response.ok(buildUserInfo(newAccess, user))
//      .cookie(newCookie)
        .build();
    } catch (Exception e) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }

  }

  @GET
  @Path("/me")
  public Response getUserInfo(@HeaderParam("Authorization") String authorization) {
    try {
      String accessToken = authorization.replace("Bearer ", "");
      String userId = JwtUtil.getClaim(accessToken, JwtUtil.ACCOUNT);
      User user = authApplicationService.getUser(userId);
      return Response.ok(buildUserInfo(accessToken, user)).build();
    } catch (Exception e) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }
  }


  private UserinfoResponse buildUserInfo(String accessToken, User user) {
    Map<String, Object> appConfig = new HashMap<>();
    Iterable<String> propertyNames = ConfigProvider.getConfig().getPropertyNames();
    for (String propertyName : propertyNames) {
      try {
        appConfig.put(propertyName, ConfigProvider.getConfig().getValue(propertyName, String.class));
      } catch (Exception e) {
        log.warn("get config error, key={}, message={}", propertyName, e.getMessage());
      }
    }
    UserinfoResponse userinfo = new UserinfoResponse();
    userinfo.setAccessToken(accessToken);
    userinfo.setUser(new UserinfoResponse.UserResponse(user.getId(), user.getUsername(), user.getAvatar()));
    userinfo.setSettings(settingsApplicationService.getSettings());
    return userinfo;
  }

  public record LoginRequest(String username, String password) {
  }

}
