package tech.wetech.flexmodel.application;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.wetech.flexmodel.graphql.GraphQLProvider;

import java.util.HashMap;
import java.util.Map;

import static graphql.ExecutionInput.newExecutionInput;

/**
 * @author cjbi
 */
@ApplicationScoped
public class GraphQLApplicationService {

  @Inject
  GraphQLProvider graphQLProvider;

  public ExecutionResult execute(String operationName, String query, Map<String, Object> variables) {
    GraphQL graphQL = graphQLProvider.getGraphQL();
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
