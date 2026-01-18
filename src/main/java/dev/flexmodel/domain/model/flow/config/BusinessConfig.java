package dev.flexmodel.domain.model.flow.config;

import jakarta.inject.Singleton;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import dev.flexmodel.shared.utils.JsonUtils;

import java.util.Map;
import java.util.Optional;

/**
 * @Author: james zhangxiao
 * @Date: 11/30/22
 * @Description:
 */
@Singleton
public class BusinessConfig {

  @ConfigProperty(name = "callActivity.nested.level")
  Optional<String> callActivityNestedLevel;

  public static final int COMPUTING_FLOW_NESTED_LEVEL = -1; // computing flow nested level
  public static final int MIN_FLOW_NESTED_LEVEL = 0; // Flow don't use CallActivity node
  public static final int MAX_FLOW_NESTED_LEVEL = 10;

  /**
   * Query callActivityNestedLevel according to caller
   * <p>
   * e.g.1 if flowA quote flowB, flowA callActivityNestedLevel equal to 1.
   * e.g.2 if flowA quote flowB, flowB quote flowC, flowA callActivityNestedLevel equal to 2.
   *
   * @param caller caller
   * @return -1 if unLimited
   */
  public int getCallActivityNestedLevel(String caller) {
    if (callActivityNestedLevel.isEmpty() || callActivityNestedLevel.get().isBlank()) {
      return MAX_FLOW_NESTED_LEVEL;
    }
    Map<String, Object> callActivityNestedLevelJO = JsonUtils.getInstance().parseToObject(callActivityNestedLevel.get(), Map.class);
    if (callActivityNestedLevelJO != null && callActivityNestedLevelJO.containsKey(caller)) {
      Object levelObj = callActivityNestedLevelJO.get(caller);
      int callActivityNestedLevel = levelObj instanceof Number ? ((Number) levelObj).intValue() : MAX_FLOW_NESTED_LEVEL;
      if (MAX_FLOW_NESTED_LEVEL < callActivityNestedLevel) {
        return MAX_FLOW_NESTED_LEVEL;
      } else {
        return callActivityNestedLevel;
      }
    } else {
      return MAX_FLOW_NESTED_LEVEL;
    }
  }
}
