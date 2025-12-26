package tech.wetech.flexmodel.domain.model.api.execution;

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
public abstract class AbstractExecutionStrategy implements ExecutionStrategy {


  protected abstract Map<String, Object> doExecute(ApiDefinition apiDefinition, ApiDefinitionMeta.Execution execution,
                                                   Map<String, String> pathParameters, HttpScriptContext httpScriptContext);

  @Override
  public void execute(ApiDefinition apiDefinition, ApiDefinitionMeta.Execution execution, Map<String, String> pathParameters, HttpScriptContext httpScriptContext) {
    // 前置脚本
    preExecute(execution, httpScriptContext);
    Map<String, Object> resMap = doExecute(apiDefinition, execution, pathParameters, httpScriptContext);
    // 设置返回值
    httpScriptContext.setResponse(new HttpScriptContext.Response(200, "OK", new HashMap<>(), resMap));
    // 后置脚本
    postExecute(execution, httpScriptContext);
  }

  protected void preExecute(ApiDefinitionMeta.Execution execution, HttpScriptContext httpScriptContext) {

    // 执行前置脚本
    String preScript = execution.getPreScript();
    if (preScript != null) {
      try {
        Map<String, Object> contextMap = httpScriptContext.toMap();
        JavaScriptUtil.execute(preScript, Map.of(HttpScriptContext.SCRIPT_CONTEXT_KEY, contextMap));
        httpScriptContext.syncFromMap(contextMap);
      } catch (Exception e) {
        log.error("Execute pre script error: {}", e.getMessage());
        throw new IllegalArgumentException("Execute pre script error: " + e.getMessage());
      } finally {
        JavaScriptUtil.cleanup();
      }
    }
  }

  protected void postExecute(ApiDefinitionMeta.Execution execution, HttpScriptContext httpScriptContext) {
    // 执行后置脚本
    String postScript = execution.getPostScript();
    if (postScript != null) {
      try {
        Map<String, Object> contextMap = httpScriptContext.toMap();
        JavaScriptUtil.execute(postScript, Map.of(HttpScriptContext.SCRIPT_CONTEXT_KEY, contextMap));
        httpScriptContext.syncFromMap(contextMap);
      } catch (Exception e) {
        log.error("Execute post script error: {}", e.getMessage());
        throw new IllegalArgumentException("Execute post script error: " + e.getMessage());
      } finally {
        JavaScriptUtil.cleanup();
      }
    }
  }

}
