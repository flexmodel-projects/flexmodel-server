package dev.flexmodel.domain.model.api;

import com.cronutils.utils.StringUtils;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.execution.values.InputInterceptor;
import graphql.schema.GraphQLScalarType;
import jakarta.enterprise.context.ApplicationScoped;

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

  public GraphQL getGraphQL(String projectId) {
    if (StringUtils.isEmpty(projectId)) {
      return defaultGraphql;
    }
    return tenantGraphqlMap.get(projectId);
  }

  public void addDefaultGraphQL(GraphQL graphQL) {
    defaultGraphql = graphQL;
  }

  public void addGraphQL(String projectId, GraphQL graphQL) {
    tenantGraphqlMap.put(projectId, graphQL);
  }

  public ExecutionResult execute(String projectId, String operationName, String query, Map<String, Object> variables) {
    GraphQL graphQL = getGraphQL(projectId);
    if (variables == null) {
      variables = new HashMap<>();
    }
    ExecutionInput executionInput = newExecutionInput()
      .operationName(operationName)
      .query(query)
      .variables(variables)
      .graphQLContext(Map.of(InputInterceptor.class, (InputInterceptor) (value, graphQLType, graphqlContext, locale) -> {
        boolean isNumeric = graphQLType instanceof GraphQLScalarType graphQLScalarType
                            && (graphQLScalarType.getName().equals("Int") ||
                                graphQLScalarType.getName().equals("Float") ||
                                graphQLScalarType.getName().equals("Long")
                            );
        if (isNumeric && value instanceof String valueString) {
          return Double.valueOf(valueString);
        }
        return value;
      }))
      .build();
    return graphQL.execute(executionInput);
  }


}
