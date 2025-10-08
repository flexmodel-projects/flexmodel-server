package tech.wetech.flexmodel.domain.model.flow.shared.util;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.wetech.flexmodel.domain.model.flow.exception.ProcessException;
import tech.wetech.flexmodel.domain.model.flow.shared.common.ErrorEnum;
import tech.wetech.flexmodel.shared.utils.JsonUtils;
import tech.wetech.flexmodel.shared.utils.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JavaScript执行工具类，使用GraalVM JavaScript引擎
 *
 * @author cjbi
 */
public class JavaScriptUtil {

  protected static final Logger LOGGER = LoggerFactory.getLogger(JavaScriptUtil.class);

  private static final ThreadLocal<Context> CONTEXT_THREAD_LOCAL = ThreadLocal.withInitial(() ->
    Context.newBuilder("js")
      .allowAllAccess(false)
      .allowHostClassLookup(className -> false)
      .option("engine.WarnInterpreterOnly", "false")
      .build()
  );

  private JavaScriptUtil() {
  }

  /**
   * 执行JavaScript表达式
   *
   * @param expression JavaScript表达式
   * @param dataMap 变量映射
   * @return 执行结果
   * @throws Exception 执行异常
   */
  public static Object execute(String expression, Map<String, Object> dataMap) throws Exception {
    if (StringUtils.isBlank(expression)) {
      LOGGER.warn("execute: expression is empty");
      return null;
    }

    try {
      Context context = CONTEXT_THREAD_LOCAL.get();
      Value bindings = context.getBindings("js");

      // 将数据映射注入到JavaScript上下文中
      if (dataMap != null && !dataMap.isEmpty()) {
        for (Map.Entry<String, Object> entry : dataMap.entrySet()) {
          Value jsValue = convertObjectToJSValue(context, entry.getValue());
          bindings.putMember(entry.getKey(), jsValue);
        }
      }

      // 执行JavaScript表达式
      Value result = context.eval("js", expression);
      Object resultObject = convertValue(result);

      LOGGER.info("execute.||expression={}||resultObject={}", expression, resultObject);
      return resultObject;

    } catch (PolyglotException pe) {
      LOGGER.warn("execute PolyglotException.||expression={}||dataMap={}", expression, dataMap, pe);
      throw new ProcessException(ErrorEnum.MISSING_DATA.getErrNo(), pe.getMessage());
    } catch (Exception e) {
      LOGGER.error("execute Exception.||expression={}||dataMap={}", expression, dataMap, e);
      throw e;
    }
  }

  /**
   * 执行JavaScript脚本（支持多行代码）
   *
   * @param script JavaScript脚本
   * @param dataMap 变量映射
   * @return 执行结果
   * @throws Exception 执行异常
   */
  public static Object executeScript(String script, Map<String, Object> dataMap) throws Exception {
    if (StringUtils.isBlank(script)) {
      LOGGER.warn("executeScript: script is empty");
      return null;
    }

    try {
      Context context = CONTEXT_THREAD_LOCAL.get();
      Value bindings = context.getBindings("js");

      // 将数据映射注入到JavaScript上下文中
      if (dataMap != null && !dataMap.isEmpty()) {
        for (Map.Entry<String, Object> entry : dataMap.entrySet()) {
          Value jsValue = convertObjectToJSValue(context, entry.getValue());
          bindings.putMember(entry.getKey(), jsValue);
        }
      }

      // 执行JavaScript脚本
      Value result = context.eval("js", script);
      Object resultObject = convertValue(result);

      LOGGER.info("executeScript.||script={}||resultObject={}", script, resultObject);
      return resultObject;

    } catch (PolyglotException pe) {
      LOGGER.warn("executeScript PolyglotException.||script={}||dataMap={}", script, dataMap, pe);
      throw new ProcessException(ErrorEnum.MISSING_DATA.getErrNo(), pe.getMessage());
    } catch (Exception e) {
      LOGGER.error("executeScript Exception.||script={}||dataMap={}", script, dataMap, e);
      throw e;
    }
  }

  /**
   * 将Java对象转换为JavaScript对象
   */
  private static Value convertObjectToJSValue(Context context, Object obj) {
    if (obj == null) {
      return context.eval("js", "null");
    }

    if (obj instanceof String) {
      String escaped = obj.toString().replace("'", "\\'").replace("\"", "\\\"");
      return context.eval("js", "'" + escaped + "'");
    }

    if (obj instanceof Number || obj instanceof Boolean) {
      return context.eval("js", obj.toString());
    }

    if (obj instanceof Map || obj instanceof List) {
      // 使用JsonUtils将对象转换为JSON字符串，然后解析为JavaScript对象
      String json = JsonUtils.getInstance().stringify(obj);
      if (json != null) {
        return context.eval("js", "JSON.parse('" + json.replace("'", "\\'") + "')");
      }
    }

    // 对于其他复杂对象，也使用JSON转换
    String json = JsonUtils.getInstance().stringify(obj);
    if (json != null) {
      return context.eval("js", "JSON.parse('" + json.replace("'", "\\'") + "')");
    }

    // 兜底方案：转换为字符串
    return context.eval("js", "'" + obj.toString().replace("'", "\\'") + "'");
  }

  /**
   * 转换GraalVM Value为Java对象
   */
  private static Object convertValue(Value value) {
    if (value == null || value.isNull()) {
      return null;
    }

    if (value.isBoolean()) {
      return value.asBoolean();
    }

    if (value.isNumber()) {
      if (value.fitsInInt()) {
        return value.asInt();
      } else if (value.fitsInLong()) {
        return value.asLong();
      } else if (value.fitsInDouble()) {
        return value.asDouble();
      }
    }

    if (value.isString()) {
      return value.asString();
    }

    if (value.hasArrayElements()) {
      long size = value.getArraySize();
      Object[] array = new Object[(int) size];
      for (int i = 0; i < size; i++) {
        array[i] = convertValue(value.getArrayElement(i));
      }
      return array;
    }

    // 处理JavaScript对象，转换为Java Map
    if (value.hasMembers()) {
      Map<String, Object> map = new HashMap<>();
      for (String key : value.getMemberKeys()) {
        Value memberValue = value.getMember(key);
        map.put(key, convertValue(memberValue));
      }
      return map;
    }

    // 对于其他类型，返回字符串表示
    return value.toString();
  }

  /**
   * 清理当前线程的Context
   */
  public static void cleanup() {
    Context context = CONTEXT_THREAD_LOCAL.get();
    if (context != null) {
      context.close();
      CONTEXT_THREAD_LOCAL.remove();
    }
  }
}

