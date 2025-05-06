package tech.wetech.flexmodel.interfaces.rest;

import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import tech.wetech.flexmodel.domain.model.codegen.CodeGenerationService;
import tech.wetech.flexmodel.domain.model.codegen.ZipService;
import tech.wetech.flexmodel.util.JsonUtils;

import java.util.List;
import java.util.Map;

import static tech.wetech.flexmodel.interfaces.rest.Resources.ROOT_PATH;

/**
 * @author cjbi
 */
@Slf4j
@Tag(name = "【Flexmodel】生成器", description = "生成器管理")
@Path(ROOT_PATH + "/codegen")
public class GeneratorResource {

  @Inject
  CodeGenerationService codeGenerationService;
  @Inject
  ZipService zipService;

  @GET
  @Path("/{datasource}_{model}.zip")
  @PermitAll
  public Response generate(@PathParam("datasource") String datasource,
                           @PathParam("model") String model,
                           @QueryParam("template") String template,
                           @QueryParam("variables") String variablesString
  ) throws Exception {
    try {
      java.nio.file.Path codeDir = codeGenerationService.generateCode(datasource, template, JsonUtils.getInstance().parseToObject(variablesString, Map.class));
      StreamingOutput stream = out -> zipService.zipDirectory(datasource, codeDir, out);
      String fileName = datasource + ".zip";
      return Response.ok(stream)
        .header("Content-Disposition", "attachment; filename=\"" + fileName + "\"")
        .build();
    } catch (Exception e) {
      // 统一捕获业务异常，返回 500
      return Response.serverError()
        .entity("生成失败: " + e.getMessage())
        .build();
    }
  }

  @GET
  @Path("/templates/names")
  public List<String> getTemplateNames() {
    return codeGenerationService.getTemplates();
  }

}
