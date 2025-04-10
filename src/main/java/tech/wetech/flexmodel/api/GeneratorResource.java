package tech.wetech.flexmodel.api;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static tech.wetech.flexmodel.api.Resources.ROOT_PATH;

/**
 * @author cjbi
 */
@Tag(name = "【Flexmodel】生成器", description = "生成器管理")
@Path(ROOT_PATH + "/generator")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class GeneratorResource {

  @GET
  @Path("/code.zip")
  public Response generate() {
    StreamingOutput stream = output -> {
      try (ZipOutputStream zipOut = new ZipOutputStream(output)) {

        // 模拟第一个文件
        zipOut.putNextEntry(new ZipEntry("hello.txt"));
        zipOut.write("Hello from Quarkus!\n".getBytes());
        zipOut.closeEntry();

        // 模拟第二个文件
        zipOut.putNextEntry(new ZipEntry("data/info.txt"));
        zipOut.write("Dynamic ZIP with directory structure.\n".getBytes());
        zipOut.closeEntry();

        zipOut.finish();
      }
    };

    return Response.ok(stream)
      .header("Content-Disposition", "attachment; filename=\"code.zip\"")
      .build();
  }

}
