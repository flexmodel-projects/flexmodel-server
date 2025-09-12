package tech.wetech.flexmodel.interfaces.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.ParameterIn;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.media.SchemaProperty;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import tech.wetech.flexmodel.application.FlowApplicationService;
import tech.wetech.flexmodel.domain.model.flow.dto.param.*;
import tech.wetech.flexmodel.domain.model.flow.dto.result.*;

/**
 * @author cjbi
 */
@Tag(name = "【Flexmodel】服务编排", description = "服务编排管理")
@Path("/f/flows")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class FlowResource {

  @Inject
  FlowApplicationService flowApplicationService;

  @Operation(summary = "创建流程")
  @POST
  @RequestBody(
    name = "请求体",
    content = {@Content(
      mediaType = "application/json",
      schema = @Schema(
        implementation = CreateFlowParamSchema.class
      )
    )}
  )
  @APIResponse(
    name = "200",
    responseCode = "200",
    description = "OK",
    content = {@Content(
      mediaType = "application/json",
      schema = @Schema(
        implementation = CreateFlowResultSchema.class
      )
    )})
  public CreateFlowResult createFlow(CreateFlowParam createFlowParam) {
    return flowApplicationService.createFlow(createFlowParam);
  }

  @Operation(summary = "部署流程")
  @POST
  @Path("/{flowModuleId}/deploy")
  @RequestBody(
    name = "请求体",
    content = {@Content(
      mediaType = "application/json",
      schema = @Schema(
        implementation = DeployFlowParamSchema.class
      )
    )}
  )
  @APIResponse(
    name = "200",
    responseCode = "200",
    description = "OK",
    content = {@Content(
      mediaType = "application/json",
      schema = @Schema(
        implementation = DeployFlowResultSchema.class
      )
    )})
  public DeployFlowResult deployFlow(
    @Parameter(name = "flowModuleId", description = "流程模块ID", in = ParameterIn.PATH)
    @PathParam("flowModuleId") String flowModuleId,
    DeployFlowParam deployFlowParam) {
    deployFlowParam.setFlowModuleId(flowModuleId);
    return flowApplicationService.deployFlow(deployFlowParam);
  }

  @Operation(summary = "获取流程模块信息")
  @APIResponse(
    name = "200",
    responseCode = "200",
    description = "OK",
    content = {@Content(
      mediaType = "application/json",
      schema = @Schema(
        implementation = FlowModuleResultSchema.class
      )
    )})
  @GET
  @Path("/{flowModuleId}")
  public FlowModuleResult getFlowModule(
    @Parameter(name = "flowModuleId", description = "流程模块ID", in = ParameterIn.PATH)
    @PathParam("flowModuleId") String flowModuleId,
    @Parameter(name = "flowDeployId", description = "流程部署ID", in = ParameterIn.QUERY)
    @QueryParam("flowDeployId") String flowDeployId) {
    GetFlowModuleParam param = new GetFlowModuleParam();
    param.setFlowModuleId(flowModuleId);
    param.setFlowDeployId(flowDeployId);
    return flowApplicationService.getFlowModule(param);
  }

  @Operation(summary = "启动流程实例")
  @POST
  @Path("/instances/start")
  @RequestBody(
    name = "请求体",
    content = {@Content(
      mediaType = "application/json",
      schema = @Schema(
        implementation = StartProcessParamSchema.class
      )
    )}
  )
  @APIResponse(
    name = "200",
    responseCode = "200",
    description = "OK",
    content = {@Content(
      mediaType = "application/json",
      schema = @Schema(
        implementation = StartProcessResultSchema.class
      )
    )})
  public StartProcessResult startProcess(StartProcessParam startProcessParam) {
    return flowApplicationService.startProcess(startProcessParam);
  }

  @Operation(summary = "提交任务")
  @POST
  @Path("/instances/{flowInstanceId}/commit")
  @RequestBody(
    name = "请求体",
    content = {@Content(
      mediaType = "application/json",
      schema = @Schema(
        implementation = CommitTaskParamSchema.class
      )
    )}
  )
  @APIResponse(
    name = "200",
    responseCode = "200",
    description = "OK",
    content = {@Content(
      mediaType = "application/json",
      schema = @Schema(
        implementation = CommitTaskResultSchema.class
      )
    )})
  public CommitTaskResult commitTask(
    @Parameter(name = "flowInstanceId", description = "流程实例ID", in = ParameterIn.PATH)
    @PathParam("flowInstanceId") String flowInstanceId,
    CommitTaskParam commitTaskParam) {
    commitTaskParam.setFlowInstanceId(flowInstanceId);
    return flowApplicationService.commitTask(commitTaskParam);
  }

  @Operation(summary = "回滚任务")
  @POST
  @Path("/instances/{flowInstanceId}/rollback")
  @RequestBody(
    name = "请求体",
    content = {@Content(
      mediaType = "application/json",
      schema = @Schema(
        implementation = RollbackTaskParamSchema.class
      )
    )}
  )
  @APIResponse(
    name = "200",
    responseCode = "200",
    description = "OK",
    content = {@Content(
      mediaType = "application/json",
      schema = @Schema(
        implementation = RollbackTaskResultSchema.class
      )
    )})
  public RollbackTaskResult rollbackTask(
    @Parameter(name = "flowInstanceId", description = "流程实例ID", in = ParameterIn.PATH)
    @PathParam("flowInstanceId") String flowInstanceId,
    RollbackTaskParam rollbackTaskParam) {
    rollbackTaskParam.setFlowInstanceId(flowInstanceId);
    return flowApplicationService.rollbackTask(rollbackTaskParam);
  }

  @Operation(summary = "终止流程实例")
  @POST
  @Path("/instances/{flowInstanceId}/terminate")
  @APIResponse(
    name = "200",
    responseCode = "200",
    description = "OK",
    content = {@Content(
      mediaType = "application/json",
      schema = @Schema(
        implementation = TerminateResultSchema.class
      )
    )})
  public TerminateResult terminateProcess(
    @Parameter(name = "flowInstanceId", description = "流程实例ID", in = ParameterIn.PATH)
    @PathParam("flowInstanceId") String flowInstanceId,
    @Parameter(name = "effectiveForSubFlowInstance", description = "是否对子流程实例生效", in = ParameterIn.QUERY)
    @QueryParam("effectiveForSubFlowInstance") @DefaultValue("true") boolean effectiveForSubFlowInstance) {
    return flowApplicationService.terminateProcess(flowInstanceId, effectiveForSubFlowInstance);
  }

  @Operation(summary = "获取流程实例信息")
  @APIResponse(
    name = "200",
    responseCode = "200",
    description = "OK",
    content = {@Content(
      mediaType = "application/json",
      schema = @Schema(
        implementation = FlowInstanceResultSchema.class
      )
    )})
  @GET
  @Path("/instances/{flowInstanceId}")
  public FlowInstanceResult getFlowInstance(
    @Parameter(name = "flowInstanceId", description = "流程实例ID", in = ParameterIn.PATH)
    @PathParam("flowInstanceId") String flowInstanceId) {
    return flowApplicationService.getFlowInstance(flowInstanceId);
  }

  @Schema(
    properties = {
      @SchemaProperty(name = "flowKey", example = "order_process", description = "流程键"),
      @SchemaProperty(name = "flowName", example = "订单处理流程", description = "流程名称"),
      @SchemaProperty(name = "remark", example = "处理订单的完整业务流程", description = "备注"),
      @SchemaProperty(name = "tenant", example = "default", description = "租户"),
      @SchemaProperty(name = "caller", example = "admin", description = "调用者"),
      @SchemaProperty(name = "operator", example = "admin", description = "操作者")
    }
  )
  public static class CreateFlowParamSchema extends CreateFlowParam {
    public CreateFlowParamSchema() {
      super("default", "admin");
    }
  }

  @Schema(
    properties = {
      @SchemaProperty(name = "errCode", example = "0", description = "错误码"),
      @SchemaProperty(name = "errMsg", example = "success", description = "错误信息"),
      @SchemaProperty(name = "flowModuleId", example = "flow_module_001", description = "流程模块ID")
    }
  )
  public static class CreateFlowResultSchema extends CreateFlowResult {
  }

  @Schema(
    properties = {
      @SchemaProperty(name = "flowModuleId", example = "flow_module_001", description = "流程模块ID"),
      @SchemaProperty(name = "tenant", example = "default", description = "租户"),
      @SchemaProperty(name = "caller", example = "admin", description = "调用者"),
      @SchemaProperty(name = "operator", example = "admin", description = "操作者")
    }
  )
  public static class DeployFlowParamSchema extends DeployFlowParam {
    public DeployFlowParamSchema() {
      super("default", "admin");
    }
  }

  @Schema(
    properties = {
      @SchemaProperty(name = "errCode", example = "0", description = "错误码"),
      @SchemaProperty(name = "errMsg", example = "success", description = "错误信息"),
      @SchemaProperty(name = "flowDeployId", example = "flow_deploy_001", description = "流程部署ID"),
      @SchemaProperty(name = "flowModuleId", example = "flow_module_001", description = "流程模块ID")
    }
  )
  public static class DeployFlowResultSchema extends DeployFlowResult {
  }

  @Schema(
    properties = {
      @SchemaProperty(name = "errCode", example = "0", description = "错误码"),
      @SchemaProperty(name = "errMsg", example = "success", description = "错误信息"),
      @SchemaProperty(name = "flowModuleId", example = "flow_module_001", description = "流程模块ID"),
      @SchemaProperty(name = "flowDeployId", example = "flow_deploy_001", description = "流程部署ID")
    }
  )
  public static class FlowModuleResultSchema extends FlowModuleResult {
  }

  @Schema(
    properties = {
      @SchemaProperty(name = "flowModuleId", example = "flow_module_001", description = "流程模块ID"),
      @SchemaProperty(name = "flowDeployId", example = "flow_deploy_001", description = "流程部署ID"),
      @SchemaProperty(name = "variables", description = "流程变量", type = SchemaType.ARRAY)
    }
  )
  public static class StartProcessParamSchema extends StartProcessParam {
  }

  @Schema(
    properties = {
      @SchemaProperty(name = "errCode", example = "0", description = "错误码"),
      @SchemaProperty(name = "errMsg", example = "success", description = "错误信息"),
      @SchemaProperty(name = "flowInstanceId", example = "flow_inst_001", description = "流程实例ID"),
      @SchemaProperty(name = "status", example = "1", description = "状态"),
      @SchemaProperty(name = "flowDeployId", example = "flow_deploy_001", description = "流程部署ID"),
      @SchemaProperty(name = "flowModuleId", example = "flow_module_001", description = "流程模块ID")
    }
  )
  public static class StartProcessResultSchema extends StartProcessResult {
  }

  @Schema(
    properties = {
      @SchemaProperty(name = "flowInstanceId", example = "flow_inst_001", description = "流程实例ID"),
      @SchemaProperty(name = "nodeInstanceId", example = "node_inst_001", description = "节点实例ID"),
      @SchemaProperty(name = "variables", description = "流程变量", type = SchemaType.ARRAY),
      @SchemaProperty(name = "tenant", example = "default", description = "租户"),
      @SchemaProperty(name = "caller", example = "admin", description = "调用者"),
      @SchemaProperty(name = "operator", example = "admin", description = "操作者")
    }
  )
  public static class CommitTaskParamSchema extends CommitTaskParam {
  }

  @Schema(
    properties = {
      @SchemaProperty(name = "errCode", example = "0", description = "错误码"),
      @SchemaProperty(name = "errMsg", example = "success", description = "错误信息"),
      @SchemaProperty(name = "flowInstanceId", example = "flow_inst_001", description = "流程实例ID"),
      @SchemaProperty(name = "status", example = "1", description = "状态")
    }
  )
  public static class CommitTaskResultSchema extends CommitTaskResult {
  }

  @Schema(
    properties = {
      @SchemaProperty(name = "flowInstanceId", example = "flow_inst_001", description = "流程实例ID"),
      @SchemaProperty(name = "nodeInstanceId", example = "node_inst_001", description = "节点实例ID"),
      @SchemaProperty(name = "tenant", example = "default", description = "租户"),
      @SchemaProperty(name = "caller", example = "admin", description = "调用者"),
      @SchemaProperty(name = "operator", example = "admin", description = "操作者")
    }
  )
  public static class RollbackTaskParamSchema extends RollbackTaskParam {
  }

  @Schema(
    properties = {
      @SchemaProperty(name = "errCode", example = "0", description = "错误码"),
      @SchemaProperty(name = "errMsg", example = "success", description = "错误信息"),
      @SchemaProperty(name = "flowInstanceId", example = "flow_inst_001", description = "流程实例ID"),
      @SchemaProperty(name = "status", example = "1", description = "状态")
    }
  )
  public static class RollbackTaskResultSchema extends RollbackTaskResult {
  }

  @Schema(
    properties = {
      @SchemaProperty(name = "errCode", example = "0", description = "错误码"),
      @SchemaProperty(name = "errMsg", example = "success", description = "错误信息"),
      @SchemaProperty(name = "flowInstanceId", example = "flow_inst_001", description = "流程实例ID"),
      @SchemaProperty(name = "status", example = "1", description = "状态")
    }
  )
  public static class TerminateResultSchema extends TerminateResult {
    public TerminateResultSchema() {
      super(tech.wetech.flexmodel.domain.model.flow.shared.common.ErrorEnum.SUCCESS);
    }
  }

  @Schema(
    properties = {
      @SchemaProperty(name = "errCode", example = "0", description = "错误码"),
      @SchemaProperty(name = "errMsg", example = "success", description = "错误信息"),
      @SchemaProperty(name = "flowInstanceBO", description = "流程实例业务对象", type = SchemaType.OBJECT)
    }
  )
  public static class FlowInstanceResultSchema extends FlowInstanceResult {
  }
}
