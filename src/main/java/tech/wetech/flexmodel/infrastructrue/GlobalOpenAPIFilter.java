package tech.wetech.flexmodel.infrastructrue;

/**
 * @author cjbi
 */

import io.quarkus.smallrye.openapi.OpenApiFilter;
import io.smallrye.openapi.api.models.responses.APIResponseImpl;
import org.eclipse.microprofile.openapi.OASFilter;
import org.eclipse.microprofile.openapi.models.Operation;

@OpenApiFilter(OpenApiFilter.RunStage.BUILD)
public class GlobalOpenAPIFilter implements OASFilter {
    @Override
    public Operation filterOperation(Operation operation) {
        // 引用预定义的500响应
        operation.getResponses()
            .addAPIResponse("500", new APIResponseImpl().ref("#/components/responses/InternalError"))
            .addAPIResponse("400", new APIResponseImpl().ref("#/components/responses/BadRequest"));
        return operation;
    }
}
