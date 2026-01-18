package dev.flexmodel.interfaces.rest.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.time.Duration;
import java.util.Date;

/**
 * @author cjbi
 */
public class JwtUtil {

  //JWT-account
  public static final String ACCOUNT = "upn";

  //失效时间七天
  public static final long REFRESH_TOKEN_EXPIRE_TIME = 7 * 24 * 60 * 60 * 1000L;

  public static final String SECRET_KEY = "storewebkey";

  /**
   * 校验token是否正确
   *
   * @param token
   * @return
   */
  public static boolean verify(String token) {
    String secret = getClaim(token, ACCOUNT) + SECRET_KEY;
    Algorithm algorithm = Algorithm.HMAC256(secret);
    JWTVerifier verifier = JWT.require(algorithm)
      .build();
    verifier.verify(token);
    return true;
  }

  /**
   * 获得Token中的信息无需secret解密也能获得
   *
   * @param token
   * @param claim
   * @return
   */
  public static String getClaim(String token, String claim) {
    try {
      DecodedJWT jwt = JWT.decode(token);
      return jwt.getClaim(claim).asString();
    } catch (JWTDecodeException e) {
      return null;
    }
  }

  /**
   * 生成签名
   *
   * @param account
   * @return
   */
  public static String sign(String account, Duration expiresIn) {
    // 帐号加JWT私钥加密
    String secret = account + SECRET_KEY;
    // 此处过期时间，单位：毫秒
    Date date = new Date(System.currentTimeMillis() + expiresIn.getSeconds() * 1000L);
    Algorithm algorithm = Algorithm.HMAC256(secret);

    return JWT.create()
      .withClaim(ACCOUNT, account)
      .withExpiresAt(date)
      .sign(algorithm);
  }

  public static String getAccount(String token) {
    return getClaim(token, ACCOUNT);
  }

  public static void main(String[] args) {
    String token = JwtUtil.sign("admin", Duration.ofDays(7));
    System.out.println(token);
    System.out.println(JwtUtil.verify(token));
    System.out.println(JwtUtil.getClaim(token, ACCOUNT));
  }

}
