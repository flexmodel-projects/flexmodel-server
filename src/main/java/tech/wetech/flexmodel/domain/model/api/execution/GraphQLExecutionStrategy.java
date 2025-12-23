package tech.wetech.flexmodel.domain.model.api.execution;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.wetech.flexmodel.domain.model.api.GraphQLManger;
import tech.wetech.flexmodel.codegen.entity.ApiDefinition;
import tech.wetech.flexmodel.domain.model.api.ApiDefinitionMeta;
import tech.wetech.flexmodel.domain.model.flow.shared.util.HttpScriptContext;

import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class GraphQLExecutionStrategy extends AbstractExecutionStrategy {

    @Inject
    GraphQLManger graphQLManger;

    @Override
    protected Map<String, Object> doExecute(ApiDefinition apiDefinition, ApiDefinitionMeta.Execution execution,
                                       Map<String, String> pathParameters, HttpScriptContext httpScriptContext) {
        String tenantId = apiDefinition.getTenantId();
        String method = httpScriptContext.getRequest().method();
        String operationName = execution.getOperationName();
        String query = execution.getQuery();
        Map<String, Object> defaultVariables = execution.getVariables();

        Map<String, Object> executionData = new HashMap<>();
        if (defaultVariables != null) {
            executionData.putAll(defaultVariables);
        }
        if ("GET".equals(method)) {
            if (httpScriptContext.getRequest().query() != null && !httpScriptContext.getRequest().query().isEmpty()) {
                executionData.putAll(httpScriptContext.getRequest().query());
            }
        } else {
            // Request body
            if (httpScriptContext.getRequest().body() != null && !httpScriptContext.getRequest().body().isEmpty()) {
                executionData.putAll(httpScriptContext.getRequest().body());
            }
        }
        // Path parameters
        if (pathParameters != null) {
            executionData.putAll(pathParameters);
        }

        graphql.ExecutionResult result = graphQLManger.execute(tenantId, operationName, query, executionData);

        Map<String, Object> resMap = new HashMap<>();
        resMap.put("data", result.getData());
        return resMap;
    }

  @Override
  public String getExecutionType() {
    return "graphql";
  }
}
