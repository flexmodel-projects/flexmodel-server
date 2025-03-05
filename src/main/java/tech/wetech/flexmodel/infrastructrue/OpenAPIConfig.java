package tech.wetech.flexmodel.infrastructrue;

import jakarta.annotation.Priority;
import jakarta.ws.rs.core.Application;
import org.eclipse.microprofile.openapi.annotations.Components;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.media.SchemaProperty;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

/**
 * @author cjbi
 */
@OpenAPIDefinition(
  info = @Info(
    title = "Flexmodel API",
    version = "0.0.1"
  ),
  components = @Components(
    responses = {
      @APIResponse(
        name = "InternalError",
        responseCode = "500",
        description = "Internal Error",
        content = {@Content(
          mediaType = "application/json",
          schema = @Schema(
            properties = {
              @SchemaProperty(name = "code", example = "-1"),
              @SchemaProperty(name = "message", example = "失败"),
              @SchemaProperty(name = "success", example = "false")
            }
          ),
          examples = {
            @ExampleObject(name = "fail", value = """
              { "code": -1, "message": "失败", "success": false }
              """)
          }
        )}),
      @APIResponse(
        name = "BadRequest",
        responseCode = "400",
        description = "Bad Request",
        content = {@Content(
          mediaType = "application/json",
          schema = @Schema(
            properties = {
              @SchemaProperty(name = "code", example = "400"),
              @SchemaProperty(name = "message", example = "参数验证异常"),
              @SchemaProperty(name = "success", example = "false"),
              @SchemaProperty(name = "errors", type = SchemaType.ARRAY)
            }
          ),
          examples = {
            @ExampleObject(name = "参数验证异常", value = """
              { "code": 1001, "message": "参数验证异常", "success": false, "errors": [{"field": "name", "message": "不能为空"}] }
              """)
          }
        )})
    }
  )
)
@Priority(1)
public class OpenAPIConfig extends Application {
}
