package tech.wetech.flexmodel.domain.model.api.execution;

import tech.wetech.flexmodel.codegen.entity.ApiDefinition;
import tech.wetech.flexmodel.domain.model.api.ApiDefinitionMeta;
import tech.wetech.flexmodel.domain.model.flow.shared.util.HttpScriptContext;

import java.util.Map;

public interface ExecutionStrategy {
  /**
   * Execute the specific execution type
   *
   * @param execution The execution configuration
   * @param httpScriptContext The HTTP script context containing all necessary data
   */
  void execute(ApiDefinition apiDefinition, ApiDefinitionMeta.Execution execution,
               Map<String, String> pathParameters, HttpScriptContext httpScriptContext);

  /**
   * Get the execution type
   * @return The execution type
   */
  String getExecutionType();

}
