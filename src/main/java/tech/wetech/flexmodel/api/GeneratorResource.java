package tech.wetech.flexmodel.api;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import tech.wetech.flexmodel.Entity;
import tech.wetech.flexmodel.SchemaObject;
import tech.wetech.flexmodel.codegen.*;
import tech.wetech.flexmodel.domain.model.modeling.ModelService;

import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static tech.wetech.flexmodel.api.Resources.ROOT_PATH;

/**
 * @author cjbi
 */
@Tag(name = "【Flexmodel】生成器", description = "生成器管理")
@Path(ROOT_PATH + "/codegen")
public class GeneratorResource {

  @Inject
  ModelService modelService;

  @GET
  @Path("/{datasource}_{model}.zip")
  public Response generate(@PathParam("datasource") String datasourceName, @PathParam("model") String modelName) {
    StreamingOutput stream = output -> {
      try (ZipOutputStream zipOut = new ZipOutputStream(output)) {

        String dir = String.format("%s_%s/tech/wetech/flexmodel/", datasourceName, modelName);

        zipOut.putNextEntry(new ZipEntry(String.format("%s_%s/", datasourceName, modelName)));
        zipOut.closeEntry();

        zipOut.putNextEntry(new ZipEntry(String.format("%s_%s/tech/", datasourceName, modelName)));
        zipOut.closeEntry();

        zipOut.putNextEntry(new ZipEntry(String.format("%s_%s/tech/wetech/", datasourceName, modelName)));
        zipOut.closeEntry();

        zipOut.putNextEntry(new ZipEntry(String.format("%s_%s/tech/wetech/flexmodel/", datasourceName, modelName)));
        zipOut.closeEntry();

        zipOut.putNextEntry(new ZipEntry(String.format("%s_%s/tech/wetech/flexmodel/entity/", datasourceName, modelName)));
        zipOut.closeEntry();

        zipOut.putNextEntry(new ZipEntry(String.format("%s_%s/tech/wetech/flexmodel/dao/", datasourceName, modelName)));
        zipOut.closeEntry();

        zipOut.putNextEntry(new ZipEntry(String.format("%s_%s/tech/wetech/flexmodel/dsl/", datasourceName, modelName)));
        zipOut.closeEntry();

        zipOut.putNextEntry(new ZipEntry(String.format("%s_%s/tech/wetech/flexmodel/enumeration/", datasourceName, modelName)));
        zipOut.closeEntry();

        List<SchemaObject> models = modelService.findModels(datasourceName);

        for (SchemaObject model : models) {
          if (model instanceof Entity entity) {
            ModelClass modelClass = GenerationTool.buildModelClass("com.example", datasourceName, entity);

            GenerationContext context = new GenerationContext();
            context.setModelClass(modelClass);
            context.putVariable("rootPackage", "com.example");


            zipOut.putNextEntry(new ZipEntry(dir + "entity/" + modelClass.getShortClassName() + ".java"));
            zipOut.write(new PojoGenerator().generate(context).getBytes());
            zipOut.closeEntry();

            zipOut.putNextEntry(new ZipEntry(dir + "dsl/" + modelClass.getShortClassName() + "DSL.java"));
            zipOut.write(new DSLGenerator().generate(context).getBytes());
            zipOut.closeEntry();

            zipOut.putNextEntry(new ZipEntry(dir + "dao/" + modelClass.getShortClassName() + "DAO.java"));
            zipOut.write(new DaoGenerator().generate(context).getBytes());
            zipOut.closeEntry();
          } else if (model instanceof tech.wetech.flexmodel.Enum anEnum) {
            EnumClass enumClass = GenerationTool.buildEnumClass("com.example", datasourceName, anEnum);
            GenerationContext context = new GenerationContext();
            context.setEnumClass(enumClass);
            context.putVariable("rootPackage", "com.example");

            zipOut.putNextEntry(new ZipEntry(dir + "enumeration/" + enumClass.getShortClassName() + ".java"));
            zipOut.write(new EnumGenerator().generate(context).getBytes());
            zipOut.closeEntry();

          }
        }


        zipOut.finish();
      }
    };

    return Response.ok(stream)
      .header("Content-Disposition", String.format("attachment; filename=\"%s_%s.zip\"", datasourceName, modelName))
      .build();
  }

}
