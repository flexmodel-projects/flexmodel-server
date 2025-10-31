package tech.wetech.flexmodel.interfaces.rest;

import graphql.ExecutionResult;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import tech.wetech.flexmodel.application.GraphQLManger;

import java.util.Map;

/**
 * @author cjbi
 */
@Tag(name = "【Flexmodel】GraphQL", description = "GraphQL接口信息")
@Path("/f/graphql")
public class GraphQLResource {

  @Inject
  GraphQLManger graphQLApplicationService;

  @Operation(summary = "执行GraphQL查询")
  @POST
  public ExecutionResult execute(GraphQLRequest request) {
    return graphQLApplicationService.execute(request.operationName(), request.query(), request.variables());
  }

  public record GraphQLRequest(@Schema(description = "操作名称") String operationName,
                               @Schema(description = "查询") String query,
                               @Schema(description = "变量") Map<String, Object> variables) {
  }

}
