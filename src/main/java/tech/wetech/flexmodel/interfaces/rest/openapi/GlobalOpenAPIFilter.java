package tech.wetech.flexmodel.interfaces.rest.openapi;

/**
 * @author cjbi
 */

import io.quarkus.smallrye.openapi.OpenApiFilter;
import jakarta.annotation.Priority;
import org.eclipse.microprofile.openapi.OASFactory;
import org.eclipse.microprofile.openapi.OASFilter;
import org.eclipse.microprofile.openapi.models.Operation;

@OpenApiFilter(OpenApiFilter.RunStage.BUILD)
@Priority(1)
public class GlobalOpenAPIFilter implements OASFilter {
    @Override
    public Operation filterOperation(Operation operation) {
        // 引用预定义的500响应
        operation.getResponses()
            .addAPIResponse("500", OASFactory.createAPIResponse().ref("#/components/responses/InternalError"))
            .addAPIResponse("400", OASFactory.createAPIResponse().ref("#/components/responses/BadRequest"));
        return operation;
    }
}
