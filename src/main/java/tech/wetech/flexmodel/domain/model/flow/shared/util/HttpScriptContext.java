package tech.wetech.flexmodel.domain.model.flow.shared.util;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.slf4j.LoggerFactory;
import tech.wetech.flexmodel.JsonUtils;
import tech.wetech.flexmodel.interfaces.rest.jwt.JwtUtil;
import tech.wetech.flexmodel.shared.Constants;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 脚本执行上下文：封装请求、响应、环境变量等信息
 * context = {
 *     request: {
 *         method,
 *         url,
 *         headers,
 *         body,
 *         query
 *     },
 *     response: {
 *         status,
 *         headers,
 *         body
 *     },
 *     log(msg){},
 *     utils:{
 *         md5(str){}
 *     }
 * }
 *
 */
@Getter
@Setter
@ToString
public class HttpScriptContext {

  public static final String SCRIPT_CONTEXT_KEY = "context";
  /** 原始请求参数（用户输入） */
  private Request request;

  /** 最终响应数据（后置脚本可修改） */
  private Response response;

  /** 工具类 */
  private ScriptUtils utils = new ScriptUtils();

  private Logger log = new Logger();

  public record Request(String method,
                        String url,
                        Map<String, String> headers,
                        Map<String, Object> body,
                        Map<String, String> query
  ) {
  }

  public record Response(int status,
                         String message,
                         Map<String, String> headers,
                         Map<String, Object> body
  ) {
  }

  public static class ScriptUtils {
    public String uuid() {
      return UUID.randomUUID().toString();
    }

    public String jwtSign(String account) {
      return JwtUtil.sign(account, Duration.ofDays(7));
    }

    public boolean jwtVerify(String token) {
      return JwtUtil.verify(token);
    }

  }

  public static class Logger {
    public void info(String msg, Object... args) {
      LoggerFactory.getLogger(Constants.APP_LOG_CATEGORY_NAME).info(msg, args);
    }

    public void error(String msg, Object... args) {
      LoggerFactory.getLogger(Constants.APP_LOG_CATEGORY_NAME).error(msg, args);
    }

    public void debug(String msg, Object... args) {
      LoggerFactory.getLogger(Constants.APP_LOG_CATEGORY_NAME).debug(msg, args);
    }

    public void warn(String msg, Object... args) {
      LoggerFactory.getLogger(Constants.APP_LOG_CATEGORY_NAME).warn(msg, args);
    }

  }

  public HttpScriptContext() {
  }

  @SuppressWarnings("all")
  public Map<String, Object> toMap() {
    Map<String, Object> context = new HashMap<>();

    // 将 Request record 转换为 Map
    if (request != null) {
      Map<String, Object> requestMap = new HashMap<>();
      requestMap.put("method", request.method());
      requestMap.put("url", request.url());
      requestMap.put("headers", request.headers());
      requestMap.put("body", request.body());
      requestMap.put("query", request.query());
      context.put("request", requestMap);
    }

    // 将 Response record 转换为 Map，使用可变的 HashMap 以便 JavaScript 修改后能同步
    if (response != null) {
      Map<String, Object> responseMap = new HashMap<>();
      responseMap.put("status", response.status());
      responseMap.put("message", response.message());
      responseMap.put("headers", response.headers());
      // 直接使用原始 body Map 的引用，这样修改会反映回去
      responseMap.put("body", response.body());
      context.put("response", responseMap);
    }

    context.put("utils", utils);
    context.put("log", log);
    return context;
  }

  /**
   * 从 Map 中同步回 Response 对象
   * 用于在 JavaScript 执行后，将修改后的值同步回原始的 Response
   *
   * @param contextMap JavaScript 执行后的 context Map
   */
  @SuppressWarnings("all")
  public void syncFromMap(Map<String, Object> contextMap) {
    JsonUtils.updateValue(this, contextMap);
  }
}
