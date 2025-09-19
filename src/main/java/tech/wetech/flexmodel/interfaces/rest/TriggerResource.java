package tech.wetech.flexmodel.interfaces.rest;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import tech.wetech.flexmodel.application.TriggerApplicationService;
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

  @Operation(summary = "获取触发器")
  @GET
  @Path("/{id}")
  public Trigger findById(@PathParam("id") String id) {
    return triggerApplicationService.findById(id);
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

  @Operation(summary = "删除触发器")
  @DELETE
  @Path("/{id}")
  public void deleteById(@PathParam("id") String id) {
    triggerApplicationService.deleteById(id);
  }

}
