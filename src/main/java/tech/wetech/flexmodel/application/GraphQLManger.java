package tech.wetech.flexmodel.application;

import com.cronutils.utils.StringUtils;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import jakarta.enterprise.context.ApplicationScoped;
import tech.wetech.flexmodel.shared.SessionContextHolder;

import java.util.HashMap;
import java.util.Map;

import static graphql.ExecutionInput.newExecutionInput;

/**
 * @author cjbi
 */
@ApplicationScoped
public class GraphQLManger {

  private GraphQL defaultGraphql;
  private final Map<String, GraphQL> tenantGraphqlMap = new HashMap<>();

  public GraphQL getGraphQL(String tenantId) {
    if (StringUtils.isEmpty(tenantId)) {
      return defaultGraphql;
    }
    return tenantGraphqlMap.get(tenantId);
  }

  public void addDefaultGraphQL(GraphQL graphQL) {
    defaultGraphql = graphQL;
  }

  public void addGraphQL(String tenantId, GraphQL graphQL) {
    tenantGraphqlMap.put(tenantId, graphQL);
  }

  public ExecutionResult execute(String operationName, String query, Map<String, Object> variables) {
    String tenantId = SessionContextHolder.getTenantId();
    GraphQL graphQL = getGraphQL(tenantId);
    if (variables == null) {
      variables = new HashMap<>();
    }
    ExecutionInput executionInput = newExecutionInput()
      .operationName(operationName)
      .query(query)
      .variables(variables)
      .build();
    return graphQL.execute(executionInput);
  }


}
