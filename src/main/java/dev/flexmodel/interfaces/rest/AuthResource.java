package dev.flexmodel.interfaces.rest;

import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import dev.flexmodel.application.AuthApplicationService;
import dev.flexmodel.codegen.entity.User;
import dev.flexmodel.interfaces.rest.jwt.JwtUtil;
import dev.flexmodel.interfaces.rest.response.UserinfoResponse;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * @author cjbi
 */
@Tag(name = "认证", description = "认证授权管理")
@Slf4j
@Path("/v1/auth")
@Produces(MediaType.APPLICATION_JSON)
public class AuthResource {

  @Inject
  AuthApplicationService authApplicationService;

  @POST
  @Path("/login")
  @PermitAll
  public Response login(LoginRequest req) {
    User user = authApplicationService.login(req.username, req.password);
    // 签发 accessToken
    String accessToken = JwtUtil.sign(user.getId(), Duration.ofDays(7));
    // 签发 refreshToken
    String refreshToken = JwtUtil.sign(user.getId(), Duration.ofDays(30));

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
  @Path("/whoami")
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
    UserinfoResponse userinfo = new UserinfoResponse();
    userinfo.setToken(accessToken);
    userinfo.setExpiresIn(300000L);
    userinfo.setUser(new UserinfoResponse.UserResponse(user.getId(), user.getName(), user.getEmail()));
    userinfo.setPermissions(authApplicationService.findPermissions(user.getId()));
    return userinfo;
  }

  public record LoginRequest(String username, String password) {
  }

}
