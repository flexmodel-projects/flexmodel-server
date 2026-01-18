package dev.flexmodel.rest;

import dev.flexmodel.interfaces.rest.jwt.JwtUtil;

import java.time.Duration;

/**
 * 测试Token辅助类
 * 用于在测试中统一管理JWT token
 *
 * @author cjbi
 */
public class TestTokenHelper {

  /**
   * 默认测试用户ID
   */
  private static final String DEFAULT_TEST_USER_ID = "admin";

  /**
   * 默认token过期时间（5分钟）
   */
  private static final Duration DEFAULT_TOKEN_DURATION = Duration.ofMinutes(5);

  /**
   * 获取测试用的token
   *
   * @return JWT token字符串
   */
  public static String getTestToken() {
    return JwtUtil.sign(DEFAULT_TEST_USER_ID, DEFAULT_TOKEN_DURATION);
  }

  /**
   * 获取指定用户的测试token
   *
   * @param userId 用户ID
   * @return JWT token字符串
   */
  public static String getTestToken(String userId) {
    return JwtUtil.sign(userId, DEFAULT_TOKEN_DURATION);
  }

  /**
   * 获取指定过期时间的测试token
   *
   * @param duration token过期时间
   * @return JWT token字符串
   */
  public static String getTestToken(Duration duration) {
    return JwtUtil.sign(DEFAULT_TEST_USER_ID, duration);
  }

  /**
   * 获取指定用户和过期时间的测试token
   *
   * @param userId 用户ID
   * @param duration token过期时间
   * @return JWT token字符串
   */
  public static String getTestToken(String userId, Duration duration) {
    return JwtUtil.sign(userId, duration);
  }

  /**
   * 获取Authorization header的值
   *
   * @return "Bearer {token}"格式的字符串
   */
  public static String getAuthorizationHeader() {
    return "Bearer " + getTestToken();
  }

  /**
   * 获取指定用户的Authorization header的值
   *
   * @param userId 用户ID
   * @return "Bearer {token}"格式的字符串
   */
  public static String getAuthorizationHeader(String userId) {
    return "Bearer " + getTestToken(userId);
  }
}
