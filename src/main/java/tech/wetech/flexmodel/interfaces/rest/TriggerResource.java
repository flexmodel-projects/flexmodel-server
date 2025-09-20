package tech.wetech.flexmodel.interfaces.rest;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import tech.wetech.flexmodel.application.TriggerApplicationService;
import tech.wetech.flexmodel.application.dto.PageDTO;
import tech.wetech.flexmodel.application.dto.TriggerDTO;
import tech.wetech.flexmodel.codegen.entity.Trigger;

/**
 * @author cjbi
 */
@ApplicationScoped
@Tag(name = "【Flexmodel】触发器", description = "触发器管理")
@Path("/f/triggers")
public class TriggerResource {
  @Inject
  TriggerApplicationService triggerApplicationService;

  @Operation(summary = "获取单个触发器")
  @GET
  @Path("/{id}")
  public TriggerDTO findById(@PathParam("id") String id) {
    return triggerApplicationService.findById(id);
  }

  @Operation(summary = "获取触发器列表")
  @GET
  public PageDTO<TriggerDTO> find(@QueryParam("name") String name,
                                  @QueryParam("page") @DefaultValue("1") Integer page,
                                  @QueryParam("size") @DefaultValue("15") Integer size) {
    return triggerApplicationService.find(name, page, size);
  }

  @Operation(summary = "创建触发器")
  @POST
  public Trigger create(Trigger trigger) {
    return triggerApplicationService.create(trigger);
  }

  @Operation(summary = "更新触发器")
  @PUT
  @Path("/{id}")
  public Trigger save(@PathParam("id") String id, Trigger trigger) {
    trigger.setId(id);
    return triggerApplicationService.update(trigger);
  }

  @Operation(summary = "部分更新触发器")
  @PATCH
  @Path("/{id}")
  public Trigger patch(@PathParam("id") String id, Trigger req) {
    TriggerDTO dto = triggerApplicationService.findById(id);
    if (req.getState() != null) {
      dto.setState(req.getState());
    }
    return triggerApplicationService.update(dto);
  }

  @Operation(summary = "删除触发器")
  @DELETE
  @Path("/{id}")
  public void deleteById(@PathParam("id") String id) {
    triggerApplicationService.deleteById(id);
  }

  @Operation(summary = "立即执行触发器")
  @POST
  @Path("/{id}/execute")
  public void executeNow(@PathParam("id") String id) {
    // TODO: 实现立即执行逻辑
    triggerApplicationService.executeNow(id);
  }

}
