package tech.wetech.flexmodel.interfaces.rest;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import tech.wetech.flexmodel.application.ScheduleApplicationService;
import tech.wetech.flexmodel.application.dto.PageDTO;
import tech.wetech.flexmodel.application.dto.TriggerDTO;
import tech.wetech.flexmodel.codegen.entity.Trigger;

/**
 * @author cjbi
 */
@ApplicationScoped
@Tag(name = "【Flexmodel】触发器", description = "触发器管理")
@Path("/f/schedule/triggers")
public class ScheduleResource {
  @Inject
  ScheduleApplicationService scheduleApplicationService;

  @Operation(summary = "获取单个触发器")
  @GET
  @Path("/{id}")
  public TriggerDTO findById(@PathParam("id") String id) {
    return scheduleApplicationService.findById(id);
  }

  @Operation(summary = "获取触发器列表")
  @GET
  public PageDTO<TriggerDTO> findPage(@QueryParam("name") String name,
                                      @QueryParam("page") @DefaultValue("1") Integer page,
                                      @QueryParam("size") @DefaultValue("15") Integer size) {
    return scheduleApplicationService.findPage(name, page, size);
  }

  @Operation(summary = "创建触发器")
  @POST
  public Trigger create(Trigger trigger) {
    return scheduleApplicationService.create(trigger);
  }

  @Operation(summary = "更新触发器")
  @PUT
  @Path("/{id}")
  public Trigger save(@PathParam("id") String id, Trigger trigger) {
    trigger.setId(id);
    return scheduleApplicationService.update(trigger);
  }

  @Operation(summary = "部分更新触发器")
  @PATCH
  @Path("/{id}")
  public Trigger patch(@PathParam("id") String id, Trigger req) {
    TriggerDTO dto = scheduleApplicationService.findById(id);
    if (req.getState() != null) {
      dto.setState(req.getState());
    }
    return scheduleApplicationService.update(dto);
  }

  @Operation(summary = "删除触发器")
  @DELETE
  @Path("/{id}")
  public void deleteById(@PathParam("id") String id) {
    scheduleApplicationService.deleteById(id);
  }

  @Operation(summary = "立即执行触发器")
  @POST
  @Path("/{id}/execute")
  public void executeNow(@PathParam("id") String id) {
    // TODO: 实现立即执行逻辑
    scheduleApplicationService.executeNow(id);
  }

}
