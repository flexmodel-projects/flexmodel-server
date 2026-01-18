package dev.flexmodel.interfaces.rest;

import jakarta.inject.Inject;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.ParameterIn;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.media.SchemaProperty;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import dev.flexmodel.application.dto.FileItem;
import dev.flexmodel.codegen.entity.Storage;
import dev.flexmodel.domain.model.storage.StorageService;

import java.io.InputStream;
import java.util.List;

/**
 * @author cjbi
 */
@Tag(name = "文件存储", description = "文件存储管理")
@Path("/v1/projects/{projectId}/storages")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class StorageResource {

  @Inject
  StorageService storageService;

  @APIResponse(
    name = "200",
    responseCode = "200",
    description = "OK",
    content = {
      @Content(
        mediaType = "application/json",
        schema = @Schema(
          type = SchemaType.ARRAY,
          implementation = StorageSchema.class
        )
      )
    }
  )
  @Operation(summary = "获取所有存储配置")
  @GET
  public List<Storage> findAll(@PathParam("projectId") String projectId) {
    return storageService.findAll(projectId);
  }

  @APIResponse(
    name = "200",
    responseCode = "200",
    description = "OK",
    content = {
      @Content(
        mediaType = "application/json",
        schema = @Schema(
          implementation = StorageSchema.class
        )
      )
    }
  )
  @Operation(summary = "获取单个存储配置")
  @GET
  @Path("/{storageName}")
  public Storage findOne(
    @Parameter(name = "projectId", in = ParameterIn.PATH, description = "项目ID", required = true)
    @PathParam("projectId") String projectId,
    @Parameter(name = "storageName", in = ParameterIn.PATH, description = "存储名称", required = true)
    @PathParam("storageName") String storageName) {
    return storageService.findOne(projectId, storageName).orElse(null);
  }

  @APIResponse(
    name = "200",
    responseCode = "200",
    description = "OK",
    content = {
      @Content(
        mediaType = "application/json",
        schema = @Schema(
          implementation = StorageSchema.class
        )
      )
    }
  )
  @Operation(summary = "创建存储配置")
  @POST
  public Storage create(
    @Parameter(name = "projectId", in = ParameterIn.PATH, description = "项目ID", required = true)
    @PathParam("projectId") String projectId,
    Storage storage) {
    storage.setProjectId(projectId);
    return storageService.createStorage(projectId, storage);
  }

  @APIResponse(
    name = "200",
    responseCode = "200",
    description = "OK",
    content = {
      @Content(
        mediaType = "application/json",
        schema = @Schema(
          implementation = StorageSchema.class
        )
      )
    }
  )
  @Operation(summary = "更新存储配置")
  @PUT
  @Path("/{storageName}")
  public Storage update(
    @Parameter(name = "projectId", in = ParameterIn.PATH, description = "项目ID", required = true)
    @PathParam("projectId") String projectId,
    @Parameter(name = "storageName", in = ParameterIn.PATH, description = "存储名称", required = true)
    @PathParam("storageName") String storageName,
    Storage storage) {
    storage.setProjectId(projectId);
    return storageService.updateStorage(projectId, storage);
  }

  @APIResponse(
    name = "200",
    responseCode = "200",
    description = "OK"
  )
  @Operation(summary = "删除存储配置")
  @DELETE
  @Path("/{storageName}")
  public void delete(
    @Parameter(name = "projectId", in = ParameterIn.PATH, description = "项目ID", required = true)
    @PathParam("projectId") String projectId,
    @Parameter(name = "storageName", in = ParameterIn.PATH, description = "存储名称", required = true)
    @PathParam("storageName") String storageName) {
    storageService.deleteStorage(projectId, storageName);
  }

  @APIResponse(
    name = "200",
    responseCode = "200",
    description = "OK",
    content = {
      @Content(
        mediaType = "application/json",
        schema = @Schema(
          type = SchemaType.ARRAY,
          implementation = FileItem.class
        )
      )
    }
  )
  @Operation(summary = "列出文件")
  @GET
  @Path("/{storageName}/files")
  public List<FileItem> listFiles(
    @Parameter(name = "projectId", in = ParameterIn.PATH, description = "项目ID", required = true)
    @PathParam("projectId") String projectId,
    @Parameter(name = "storageName", in = ParameterIn.PATH, description = "存储名称", required = true)
    @PathParam("storageName") String storageName,
    @Parameter(name = "path", in = ParameterIn.QUERY, description = "文件路径")
    @QueryParam("path") String path) {
    return storageService.listFiles(projectId, storageName, path != null ? path : "");
  }

  @APIResponse(
    name = "200",
    responseCode = "200",
    description = "OK",
    content = {
      @Content(
        mediaType = "application/json",
        schema = @Schema(
          implementation = FileItem.class
        )
      )
    }
  )
  @Operation(summary = "获取文件信息")
  @GET
  @Path("/{storageName}/files/info")
  public FileItem getFile(
    @Parameter(name = "projectId", in = ParameterIn.PATH, description = "项目ID", required = true)
    @PathParam("projectId") String projectId,
    @Parameter(name = "storageName", in = ParameterIn.PATH, description = "存储名称", required = true)
    @PathParam("storageName") String storageName,
    @Parameter(name = "path", in = ParameterIn.QUERY, description = "文件路径", required = true)
    @QueryParam("path") @NotBlank String path) {
    return storageService.getFile(projectId, storageName, path);
  }

  @APIResponse(
    name = "200",
    responseCode = "200",
    description = "OK"
  )
  @Operation(summary = "上传文件")
  @POST
  @Path("/{storageName}/files/upload")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  public Response uploadFile(
    @Parameter(name = "projectId", in = ParameterIn.PATH, description = "项目ID", required = true)
    @PathParam("projectId") String projectId,
    @Parameter(name = "storageName", in = ParameterIn.PATH, description = "存储名称", required = true)
    @PathParam("storageName") String storageName,
    @Parameter(name = "path", in = ParameterIn.QUERY, description = "文件路径", required = true)
    @QueryParam("path") @NotBlank String path,
    @Parameter(name = "file", description = "文件", required = true)
    @org.jboss.resteasy.reactive.RestForm("file") @org.jboss.resteasy.reactive.PartType(MediaType.APPLICATION_OCTET_STREAM) InputStream fileStream,
    @Parameter(name = "fileSize", description = "文件大小")
    @org.jboss.resteasy.reactive.RestForm("fileSize") Long fileSize) {
    storageService.uploadFile(projectId, storageName, path, fileStream, fileSize != null ? fileSize : 0);
    return Response.ok().build();
  }

  @APIResponse(
    name = "200",
    responseCode = "200",
    description = "OK"
  )
  @Operation(summary = "删除文件")
  @DELETE
  @Path("/{storageName}/files/delete")
  public void deleteFile(
    @Parameter(name = "projectId", in = ParameterIn.PATH, description = "项目ID", required = true)
    @PathParam("projectId") String projectId,
    @Parameter(name = "storageName", in = ParameterIn.PATH, description = "存储名称", required = true)
    @PathParam("storageName") String storageName,
    @Parameter(name = "path", in = ParameterIn.QUERY, description = "文件路径", required = true)
    @QueryParam("path") @NotBlank String path) {
    storageService.deleteFile(projectId, storageName, path);
  }

  @APIResponse(
    name = "200",
    responseCode = "200",
    description = "OK"
  )
  @Operation(summary = "创建文件夹")
  @POST
  @Path("/{storageName}/folders/create")
  public void createFolder(
    @Parameter(name = "projectId", in = ParameterIn.PATH, description = "项目ID", required = true)
    @PathParam("projectId") String projectId,
    @Parameter(name = "storageName", in = ParameterIn.PATH, description = "存储名称", required = true)
    @PathParam("storageName") String storageName,
    @Parameter(name = "path", in = ParameterIn.QUERY, description = "文件夹路径", required = true)
    @QueryParam("path") @NotBlank String path) {
    storageService.createFolder(projectId, storageName, path);
  }

  @APIResponse(
    name = "200",
    responseCode = "200",
    description = "OK",
    content = {
      @Content(
        mediaType = "application/json",
        schema = @Schema(
          properties = {
            @SchemaProperty(name = "exists", description = "是否存在")
          }
        )
      )
    }
  )
  @Operation(summary = "检查文件是否存在")
  @GET
  @Path("/{storageName}/files/exists")
  public Response exists(
    @Parameter(name = "projectId", in = ParameterIn.PATH, description = "项目ID", required = true)
    @PathParam("projectId") String projectId,
    @Parameter(name = "storageName", in = ParameterIn.PATH, description = "存储名称", required = true)
    @PathParam("storageName") String storageName,
    @Parameter(name = "path", in = ParameterIn.QUERY, description = "文件路径", required = true)
    @QueryParam("path") @NotBlank String path) {
    boolean exists = storageService.exists(projectId, storageName, path);
    return Response.ok().entity(new ExistsResponse(exists)).build();
  }

  @APIResponse(
    name = "200",
    responseCode = "200",
    description = "OK",
    content = {
      @Content(
        mediaType = "application/json",
        schema = @Schema(
          properties = {
            @SchemaProperty(name = "size", description = "文件大小")
          }
        )
      )
    }
  )
  @Operation(summary = "获取文件大小")
  @GET
  @Path("/{storageName}/files/size")
  public Response getFileSize(
    @Parameter(name = "projectId", in = ParameterIn.PATH, description = "项目ID", required = true)
    @PathParam("projectId") String projectId,
    @Parameter(name = "storageName", in = ParameterIn.PATH, description = "存储名称", required = true)
    @PathParam("storageName") String storageName,
    @Parameter(name = "path", in = ParameterIn.QUERY, description = "文件路径", required = true)
    @QueryParam("path") @NotBlank String path) {
    long size = storageService.getFileSize(projectId, storageName, path);
    return Response.ok().entity(new FileSizeResponse(size)).build();
  }

  public static class StorageSchema {
    public String name;
    public String type;
    public Object config;
    public Boolean enabled;
  }

  public static class ExistsResponse {
    public boolean exists;

    public ExistsResponse(boolean exists) {
      this.exists = exists;
    }
  }

  public static class FileSizeResponse {
    public long size;

    public FileSizeResponse(long size) {
      this.size = size;
    }
  }
}
