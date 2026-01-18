package dev.flexmodel.interfaces.rest;

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
import dev.flexmodel.codegen.CodeGenerationService;
import dev.flexmodel.codegen.TemplateInfo;
import dev.flexmodel.domain.model.codegen.ZipService;
import dev.flexmodel.model.SchemaObject;
import dev.flexmodel.session.SessionFactory;
import dev.flexmodel.shared.utils.JsonUtils;

import java.util.List;
import java.util.Map;

/**
 * @author cjbi
 */
@Slf4j
@Tag(name = "生成器", description = "生成器管理")
@Path("/v1/codegen")
public class GeneratorResource {

  @Inject
  CodeGenerationService codeGenerationService;
  @Inject
  ZipService zipService;
  @Inject
  SessionFactory sessionFactory;

  @GET
  @Path("/{template}.zip")
  @PermitAll
  public Response generate(@PathParam("template") String template,
                           @QueryParam("projectId") String projectId,
                           @QueryParam("datasource") String datasource,
                           @QueryParam("variables") String variablesString
  ) throws Exception {
    try {
      List<SchemaObject> models = sessionFactory.getModels(datasource);
      java.nio.file.Path codeDir = codeGenerationService.generateCode(datasource, models, template, JsonUtils.getInstance().parseToObject(variablesString, Map.class));
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
  @Path("/templates")
  public List<TemplateInfo> getTemplates() {
    return codeGenerationService.getTemplates();
  }

}
