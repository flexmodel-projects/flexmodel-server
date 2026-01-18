package dev.flexmodel.domain.model.api.execution;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import dev.flexmodel.codegen.entity.ApiDefinition;
import dev.flexmodel.codegen.entity.Datasource;
import dev.flexmodel.domain.model.api.ApiDefinitionMeta;
import dev.flexmodel.domain.model.connect.DatasourceService;
import dev.flexmodel.domain.model.flow.shared.util.HttpScriptContext;
import dev.flexmodel.domain.model.flow.shared.util.JavaScriptUtil;
import dev.flexmodel.session.SessionFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author cjbi
 */
@Slf4j
@ApplicationScoped
public class ScriptExecutionStrategy extends AbstractExecutionStrategy {

  @Inject
  DatasourceService datasourceService;

  @Inject
  SessionFactory sessionFactory;

  @Override
  protected Map<String, Object> doExecute(ApiDefinition apiDefinition, ApiDefinitionMeta.Execution execution, Map<String, String> pathParameters, HttpScriptContext httpScriptContext) {
    httpScriptContext.setResponse(new HttpScriptContext.Response(200, "OK", new HashMap<>(), new HashMap<>()));
    Map<String, Object> contextMap = httpScriptContext.toMap();
    try {
      Map<String, Object> data = new HashMap<>(contextMap);
      Map<String, ScriptExecutionDB> dbs = new HashMap<>();
      for (Datasource datasource : datasourceService.findAll(apiDefinition.getProjectId())) {
        dbs.put(datasource.getName(), new ScriptExecutionDB(datasource.getName(), sessionFactory));
      }
      data.put("dbs", dbs);
      JavaScriptUtil.execute(execution.getExecutionScript(), Map.of(HttpScriptContext.SCRIPT_CONTEXT_KEY, data));
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
