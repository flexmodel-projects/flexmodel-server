package dev.flexmodel.rest;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import dev.flexmodel.SQLiteTestResource;
import dev.flexmodel.interfaces.rest.jwt.JwtUtil;

import java.time.Duration;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * AuthResource 测试类
 *
 * @author cjbi
 */
@QuarkusTest
@QuarkusTestResource(SQLiteTestResource.class)
class AuthResourceTest {

  /**
   * 测试登录成功
   */
  @Test
  void testLoginSuccess() {
    given()
      .when()
      .contentType(ContentType.JSON)
      .body("""
          {
            "username": "admin",
            "password": "admin123"
          }
        """)
      .post(Resources.ROOT_PATH + "/auth/login")
      .then()
      .statusCode(200)
      .body("token", notNullValue())
      .body("expiresIn", is(300000))
      .body("user.id", notNullValue())
      .body("user.name", is("Admin"))
      .body("permissions", hasSize(1))
      .body("permissions[0]", is("*"))
      .cookie("refreshToken", notNullValue());
  }

  /**
   * 测试登录失败 - 用户名错误
   */
  @Test
  void testLoginFailureInvalidUsername() {
    given()
      .when()
      .contentType(ContentType.JSON)
      .body("""
          {
            "username": "invalid_user",
            "password": "admin"
          }
        """)
      .post(Resources.ROOT_PATH + "/auth/login")
      .then()
      .statusCode(400); // 或者根据实际业务逻辑返回相应的错误码
  }

  /**
   * 测试登录失败 - 密码错误
   */
  @Test
  void testLoginFailureInvalidPassword() {
    given()
      .when()
      .contentType(ContentType.JSON)
      .body("""
          {
            "username": "admin",
            "password": "wrong_password"
          }
        """)
      .post(Resources.ROOT_PATH + "/auth/login")
      .then()
      .statusCode(400); // 或者根据实际业务逻辑返回相应的错误码
  }

  /**
   * 测试登录失败 - 请求体为空
   */
  @Test
  void testLoginFailureEmptyBody() {
    given()
      .when()
      .contentType(ContentType.JSON)
      .post(Resources.ROOT_PATH + "/auth/login")
      .then()
      .statusCode(500);
  }

  /**
   * 测试刷新token成功
   */
  @Test
  void testRefreshTokenSuccess() {
    // 先登录获取refreshToken
    String refreshToken = given()
      .when()
      .contentType(ContentType.JSON)
      .body("""
          {
            "username": "admin",
            "password": "admin123"
          }
        """)
      .post(Resources.ROOT_PATH + "/auth/login")
      .then()
      .statusCode(200)
      .extract()
      .cookie("refreshToken");

    // 使用refreshToken刷新accessToken
    given()
      .when()
      .cookie("refreshToken", refreshToken)
      .post(Resources.ROOT_PATH + "/auth/refresh")
      .then()
      .statusCode(200)
      .body("token", notNullValue())
      .body("expiresIn", is(300000))
      .body("user.id", notNullValue())
      .body("user.name", is("Admin"));
  }

  /**
   * 测试刷新token失败 - 没有refreshToken
   */
  @Test
  void testRefreshTokenFailureNoToken() {
    given()
      .when()
      .post(Resources.ROOT_PATH + "/auth/refresh")
      .then()
      .statusCode(401);
  }

  /**
   * 测试刷新token失败 - 无效的refreshToken
   */
  @Test
  void testRefreshTokenFailureInvalidToken() {
    given()
      .when()
      .cookie("refreshToken", "invalid_token")
      .post(Resources.ROOT_PATH + "/auth/refresh")
      .then()
      .statusCode(401);
  }

  /**
   * 测试获取用户信息成功
   */
  @Test
  void testGetUserInfoSuccess() {
    String accessToken = TestTokenHelper.getTestToken();

    given()
      .when()
      .header("Authorization", "Bearer " + accessToken)
      .get(Resources.ROOT_PATH + "/auth/whoami")
      .then()
      .statusCode(200)
      .body("token", is(accessToken))
      .body("expiresIn", is(300000))
      .body("user.id", is("admin"))
      .body("user.name", is("Admin"));
  }

  /**
   * 测试获取用户信息失败 - 没有Authorization header
   */
  @Test
  void testGetUserInfoFailureNoAuthHeader() {
    given()
      .when()
      .get(Resources.ROOT_PATH + "/auth/whoami")
      .then()
      .statusCode(400);
  }

  /**
   * 测试获取用户信息失败 - 无效的token
   */
  @Test
  void testGetUserInfoFailureInvalidToken() {
    given()
      .when()
      .header("Authorization", "Bearer invalid_token")
      .get(Resources.ROOT_PATH + "/auth/whoami")
      .then()
      .statusCode(401);
  }

  /**
   * 测试获取用户信息失败 - 过期的token
   */
  @Test
  void testGetUserInfoFailureExpiredToken() {
    String expiredToken = JwtUtil.sign("admin", Duration.ofSeconds(-1));

    given()
      .when()
      .header("Authorization", "Bearer " + expiredToken)
      .get(Resources.ROOT_PATH + "/auth/whoami")
      .then()
      .statusCode(401);
  }

  /**
   * 测试获取用户信息失败 - 错误的Authorization格式
   */
  @Test
  void testGetUserInfoFailureWrongAuthFormat() {
    given()
      .when()
      .header("Authorization", "InvalidFormat token")
      .get(Resources.ROOT_PATH + "/auth/whoami")
      .then()
      .statusCode(401);
  }

  /**
   * 测试获取用户信息失败 - 用户不存在
   */
  @Test
  void testGetUserInfoFailureUserNotExists() {
    String tokenForNonExistentUser = TestTokenHelper.getTestToken("non_existent_user");

    given()
      .when()
      .header("Authorization", "Bearer " + tokenForNonExistentUser)
      .get(Resources.ROOT_PATH + "/auth/whoami")
      .then()
      .statusCode(401);
  }

  /**
   * 测试完整的认证流程
   */
  @Test
  void testCompleteAuthFlow() {
    // 1. 登录
    String refreshToken = given()
      .when()
      .contentType(ContentType.JSON)
      .body("""
          {
            "username": "admin",
            "password": "admin123"
          }
        """)
      .post(Resources.ROOT_PATH + "/auth/login")
      .then()
      .statusCode(200)
      .extract()
      .cookie("refreshToken");

    String accessToken = given()
      .when()
      .contentType(ContentType.JSON)
      .body("""
          {
            "username": "admin",
            "password": "admin123"
          }
        """)
      .post(Resources.ROOT_PATH + "/auth/login")
      .then()
      .statusCode(200)
      .extract()
      .path("token");

    // 2. 使用accessToken获取用户信息
    given()
      .when()
      .header("Authorization", "Bearer " + accessToken)
      .get(Resources.ROOT_PATH + "/auth/whoami")
      .then()
      .statusCode(200)
      .body("token", is(accessToken))
      .body("user.name", is("Admin"));

    // 3. 使用refreshToken刷新accessToken
    String newAccessToken = given()
      .when()
      .cookie("refreshToken", refreshToken)
      .post(Resources.ROOT_PATH + "/auth/refresh")
      .then()
      .statusCode(200)
      .extract()
      .path("token");

    // 4. 使用新的accessToken获取用户信息
    given()
      .when()
      .header("Authorization", "Bearer " + newAccessToken)
      .get(Resources.ROOT_PATH + "/auth/whoami")
      .then()
      .statusCode(200)
      .body("token", is(newAccessToken))
      .body("user.name", is("Admin"));
  }
}
