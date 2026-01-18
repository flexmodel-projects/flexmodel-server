package dev.flexmodel.domain.model.flow.shared.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import dev.flexmodel.domain.model.flow.exception.ProcessException;
import dev.flexmodel.domain.model.flow.shared.common.ErrorEnum;
import dev.flexmodel.shared.utils.StringUtils;

import javax.script.*;
import java.util.Map;

/**
 * JavaScript执行工具类，使用GraalVM JavaScript ScriptEngine
 *
 * @author cjbi
 */
public class JavaScriptUtil {

  protected static final Logger LOGGER = LoggerFactory.getLogger(JavaScriptUtil.class);

  private static final ScriptEngineManager SCRIPT_ENGINE_MANAGER = new ScriptEngineManager();
  private static final ThreadLocal<ScriptEngine> ENGINE_THREAD_LOCAL = ThreadLocal.withInitial(() -> {
    try {
      // 尝试获取GraalVM JavaScript引擎
      ScriptEngine engine = SCRIPT_ENGINE_MANAGER.getEngineByName("graal.js");
      if (engine == null) {
        LOGGER.warn("GraalVM JavaScript engine not found, trying JavaScript engine");
        engine = SCRIPT_ENGINE_MANAGER.getEngineByName("JavaScript");
      }
      if (engine == null) {
        throw new RuntimeException("No JavaScript engine available");
      }

      // 配置引擎选项
      Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
      if (bindings != null) {
        // ECMAScript版本：使用最新标准
        bindings.put("polyglot.js.ecmascript-version", "2023");
        // 允许主机访问
        bindings.put("polyglot.js.allowHostAccess", true);
        bindings.put("polyglot.js.allowHostClassLookup", false);
        // 兼容性设置
        bindings.put("polyglot.js.nashorn-compat", false);
      }

      LOGGER.info("GraalVM JavaScript ScriptEngine initialized successfully");
      return engine;
    } catch (Exception e) {
      LOGGER.error("Failed to initialize GraalVM JavaScript ScriptEngine", e);
      throw new RuntimeException("Failed to initialize JavaScript engine", e);
    }
  });

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
    long startTime = System.currentTimeMillis();
    try {
      ScriptEngine engine = ENGINE_THREAD_LOCAL.get();
      ScriptContext scriptContext = engine.getContext();
      Bindings bindings = scriptContext.getBindings(ScriptContext.ENGINE_SCOPE);

      // 将数据映射注入到JavaScript上下文中
      if (dataMap != null && !dataMap.isEmpty()) {
        bindings.putAll(dataMap);
      }

      // 执行JavaScript表达式
      Object result = engine.eval(expression);

      // 同步 bindings 中的修改回 dataMap（如果 dataMap 是可变的）
      if (dataMap instanceof java.util.HashMap) {
        syncBindingsToMap(bindings, dataMap);
      }

      LOGGER.info("execute.||expression={}||resultObject={}", expression, result);
      return result;

    } catch (ScriptException se) {
      LOGGER.warn("execute ScriptException.||expression={}||dataMap={}", expression, dataMap, se);
      throw new ProcessException(ErrorEnum.MISSING_DATA.getErrNo(), se.getMessage());
    } catch (Exception e) {
      LOGGER.error("execute Exception.||expression={}||dataMap={}", expression, dataMap, e);
      throw e;
    } finally {
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("execute javascript, time: {} ms", System.currentTimeMillis() - startTime);
      }
    }
  }

  /**
   * 将 bindings 中的修改同步回 dataMap
   * 递归同步嵌套的 Map 结构
   */
  @SuppressWarnings("unchecked")
  private static void syncBindingsToMap(Bindings bindings, Map<String, Object> dataMap) {
    for (String key : bindings.keySet()) {
      // 跳过引擎配置键
      if (key.startsWith("polyglot.")) {
        continue;
      }
      Object value = bindings.get(key);
      Object existingValue = dataMap.get(key);

      if (value instanceof Map) {
        if (existingValue instanceof Map) {
          // 递归同步嵌套的 Map
          syncMapToMap((Map<String, Object>) value, (Map<String, Object>) existingValue);
        } else {
          // 如果类型不匹配，直接替换
          dataMap.put(key, value);
        }
      } else {
        // 对于非 Map 类型，直接更新
        dataMap.put(key, value);
      }
    }
  }

  /**
   * 递归同步源 Map 到目标 Map
   */
  @SuppressWarnings("unchecked")
  private static void syncMapToMap(Map<String, Object> source, Map<String, Object> target) {
    try {
      for (String key : source.keySet()) {
        Object sourceValue = source.get(key);
        Object targetValue = target.get(key);

        if (sourceValue instanceof Map) {
          if (targetValue instanceof Map) {
            // 递归同步嵌套的 Map
            syncMapToMap((Map<String, Object>) sourceValue, (Map<String, Object>) targetValue);
          } else {
            // 如果类型不匹配，直接替换
            target.put(key, sourceValue);
          }
        } else {
          // 对于非 Map 类型，直接更新
          target.put(key, sourceValue);
        }
      }
    } catch (Exception e) {
      LOGGER.error("syncMapToMap error.||source={}||target={}", source, target, e);
    }

  }


  /**
   * 清理当前线程的ScriptEngine
   */
  public static void cleanup() {
    ScriptEngine engine = ENGINE_THREAD_LOCAL.get();
    if (engine != null) {
      ENGINE_THREAD_LOCAL.remove();
    }
  }
}

