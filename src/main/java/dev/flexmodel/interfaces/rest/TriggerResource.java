package dev.flexmodel.interfaces.rest;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import dev.flexmodel.application.TriggerApplicationService;
import dev.flexmodel.application.dto.PageDTO;
import dev.flexmodel.application.dto.TriggerDTO;
import dev.flexmodel.application.dto.TriggerPageRequest;
import dev.flexmodel.codegen.entity.Trigger;

/**
 * @author cjbi
 */
@ApplicationScoped
@Tag(name = "触发器", description = "触发器管理")
@Path("/v1/projects/{projectId}/triggers")
public class TriggerResource {
  @Inject
  TriggerApplicationService scheduleApplicationService;

  @Operation(summary = "获取单个触发器")
  @GET
  @Path("/{id}")
  public TriggerDTO findById(@PathParam("projectId") String projectId,
                             @PathParam("id") String id) {
    return scheduleApplicationService.findById(projectId, id);
  }

  @Operation(summary = "获取触发器列表")
  @GET
  public PageDTO<TriggerDTO> findPage(@PathParam("projectId") String projectId,
                                      @QueryParam("name") String name,
                                      @QueryParam("jobType") String jobType,
                                      @QueryParam("jobId") String jobId,
                                      @QueryParam("jobGroup") String jobGroup,
                                      @QueryParam("page") @DefaultValue("1") Integer page,
                                      @QueryParam("size") @DefaultValue("15") Integer size) {
    TriggerPageRequest request = new TriggerPageRequest();
    request.setProjectId(projectId);
    request.setName(name);
    request.setJobType(jobType);
    request.setJobId(jobId);
    request.setJobGroup(jobGroup);
    request.setPage(page);
    request.setSize(size);
    return scheduleApplicationService.findPage(projectId, request);
  }

  @Operation(summary = "创建触发器")
  @POST
  public Trigger create(@PathParam("projectId") String projectId, Trigger trigger) {
    trigger.setProjectId(projectId);
    return scheduleApplicationService.create(projectId, trigger);
  }

  @Operation(summary = "更新触发器")
  @PUT
  @Path("/{id}")
  public Trigger update(@PathParam("projectId") String projectId, @PathParam("id") String id, Trigger req) {
    req.setId(id);
    req.setProjectId(projectId);
    return scheduleApplicationService.update(projectId, req);
  }

  @Operation(summary = "部分更新触发器")
  @PATCH
  @Path("/{id}")
  public Trigger patch(@PathParam("projectId") String projectId, @PathParam("id") String id, Trigger req) {
    TriggerDTO dto = scheduleApplicationService.findById(projectId, id);
    if (req.getState() != null) {
      dto.setState(req.getState());
    }
    dto.setProjectId(projectId);
    return scheduleApplicationService.update(projectId, dto);
  }

  @Operation(summary = "删除触发器")
  @DELETE
  @Path("/{id}")
  public void deleteById(@PathParam("projectId") String projectId, @PathParam("id") String id) {
    scheduleApplicationService.deleteById(projectId, id);
  }

  @Operation(summary = "立即执行触发器")
  @POST
  @Path("/{id}/execute")
  public Trigger executeNow(@PathParam("projectId") String projectId, @PathParam("id") String id) {
    return scheduleApplicationService.executeNow(projectId, id);
  }

}
