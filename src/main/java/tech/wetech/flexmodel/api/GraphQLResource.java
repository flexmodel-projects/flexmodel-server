package tech.wetech.flexmodel.api;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import tech.wetech.flexmodel.graphql.GraphQLProvider;

import java.util.HashMap;
import java.util.Map;

import static graphql.ExecutionInput.newExecutionInput;

/**
 * @author cjbi
 */
@Path("/api/graphql")
public class GraphQLResource {

  @Inject
  GraphQLProvider graphQLProvider;

  @POST
  public ExecutionResult execute(GraphQLRequest request) {
    GraphQL graphQL = graphQLProvider.getGraphQL();
    Map<String, Object> variables = request.variables();
    if (variables == null) {
      variables = new HashMap<>();
    }
    ExecutionInput executionInput = newExecutionInput()
      .operationName(request.operationName())
      .query(request.query())
      .variables(variables)
      .build();
    return graphQL.execute(executionInput);
  }

  public record GraphQLRequest(String operationName, String query, Map<String, Object> variables) {
  }

}
