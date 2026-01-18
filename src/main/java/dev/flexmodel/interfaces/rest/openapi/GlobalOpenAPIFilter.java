package dev.flexmodel.interfaces.rest.openapi;

/**
 * @author cjbi
 */

import io.quarkus.smallrye.openapi.OpenApiFilter;
import io.smallrye.openapi.internal.models.parameters.Parameter;
import jakarta.annotation.Priority;
import org.eclipse.microprofile.openapi.OASFactory;
import org.eclipse.microprofile.openapi.OASFilter;
import org.eclipse.microprofile.openapi.models.Operation;

import static org.eclipse.microprofile.openapi.models.parameters.Parameter.In;

/**
 * 此Filter用于全局设置OpenAPI文档，如添加响应、安全要求和参数验证等
 */
@OpenApiFilter(OpenApiFilter.RunStage.BUILD)
@Priority(1)
public class GlobalOpenAPIFilter implements OASFilter {
  @Override
  public Operation filterOperation(Operation operation) {

    // 引用预定义的500响应
    operation
      .getResponses()
      .addAPIResponse("500", OASFactory.createAPIResponse().ref("#/components/responses/InternalError"))
      .addAPIResponse("400", OASFactory.createAPIResponse().ref("#/components/responses/BadRequest"));
    // 添加安全要求
    operation.addSecurityRequirement(OASFactory.createSecurityRequirement().addScheme("BearerAuth"));

    return operation;
  }
}
