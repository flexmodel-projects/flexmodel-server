package tech.wetech.flexmodel.api;

import graphql.ExecutionResult;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import tech.wetech.flexmodel.application.GraphQLApplicationService;

import java.util.Map;

import static tech.wetech.flexmodel.api.Resources.BASE_PATH;

/**
 * @author cjbi
 */
@Path(BASE_PATH +"/graphql")
public class GraphQLResource {

  @Inject
  GraphQLApplicationService graphQLApplicationService;

  @POST
  public ExecutionResult execute(GraphQLRequest request) {
    return graphQLApplicationService.execute(request.operationName(), request.query(), request.variables());
  }

  public record GraphQLRequest(String operationName, String query, Map<String, Object> variables) {
  }

}
