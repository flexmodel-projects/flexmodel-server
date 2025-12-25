package tech.wetech.flexmodel.domain.model.api.execution;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import tech.wetech.flexmodel.codegen.entity.ApiDefinition;
import tech.wetech.flexmodel.domain.model.api.ApiDefinitionMeta;
import tech.wetech.flexmodel.domain.model.flow.shared.util.HttpScriptContext;
import tech.wetech.flexmodel.domain.model.flow.shared.util.JavaScriptUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * @author cjbi
 */
@Slf4j
@ApplicationScoped
public class ScriptExecutionStrategy extends AbstractExecutionStrategy {
  @Override
  protected Map<String, Object> doExecute(ApiDefinition apiDefinition, ApiDefinitionMeta.Execution execution, Map<String, String> pathParameters, HttpScriptContext httpScriptContext) {
    httpScriptContext.setResponse(new HttpScriptContext.Response(200, "OK", new HashMap<>(), new HashMap<>()));
    Map<String, Object> contextMap = httpScriptContext.toMap();
    try {
      JavaScriptUtil.execute(execution.getExecutionScript(), contextMap);
      httpScriptContext.syncFromMap(contextMap);
      return httpScriptContext.getResponse().body();
    } catch (Exception e) {
      log.error("Execute script error: {}", e.getMessage(), e);
      throw new IllegalArgumentException("Execute script error: " + e.getMessage());
    } finally {
      JavaScriptUtil.cleanup();
    }
  }

  @Override
  public String getExecutionType() {
    return "script";
  }
}
