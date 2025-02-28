package tech.wetech.flexmodel.infrastructrue;

import jakarta.ws.rs.core.Application;
import org.eclipse.microprofile.openapi.annotations.Components;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
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
    title = "Flexmodel2 API",
    version = "0.0.1"
  ),
  components = @Components(
    responses = {
      @APIResponse(
        name = "500",
        description = "失败",
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
        )})
    }
  )
)
public class OpenAPIConfig extends Application {
}
