package dev.flexmodel.interfaces.rest;

import dev.flexmodel.application.dto.*;
import dev.flexmodel.domain.model.flow.dto.param.*;
import dev.flexmodel.domain.model.flow.dto.result.*;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import lombok.Getter;
import lombok.Setter;
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
import dev.flexmodel.application.FlowApplicationService;
import dev.flexmodel.application.dto.*;
import dev.flexmodel.codegen.entity.FlowInstance;
import dev.flexmodel.domain.model.flow.dto.bo.ElementInstance;
import dev.flexmodel.domain.model.flow.dto.bo.NodeInstance;
import dev.flexmodel.domain.model.flow.dto.param.*;
import dev.flexmodel.domain.model.flow.dto.result.*;
import dev.flexmodel.domain.model.flow.shared.common.ErrorEnum;

import java.util.List;
import java.util.Map;

/**
 * @author cjbi
 */
@Tag(name = "服务编排", description = "服务编排管理")
@Path("/v1/projects/{projectId}/flows")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class FlowResource {

  @Inject
  FlowApplicationService flowApplicationService;

  @Operation(summary = "获取流程实例历史用户任务列表", description = "获取指定流程实例的历史用户任务列表，按处理时间倒序排列。包含活跃和已完成的任务，不包含已禁用的任务。")
  @APIResponse(
    name = "200",
    responseCode = "200",
    description = "成功返回历史用户任务列表",
    content = {@Content(
      mediaType = "application/json",
      schema = @Schema(
        type = SchemaType.ARRAY,
        implementation = NodeInstanceSchema.class
      )
    )})
  @GET
  @Path("/instances/{flowInstanceId}/user-tasks")
  public List<NodeInstance> getHistoryUserTaskList(
    @PathParam("projectId") String projectId,
    @Parameter(name = "flowInstanceId", description = "流程实例ID", in = ParameterIn.PATH, required = true, example = "flow_inst_001")
    @PathParam("flowInstanceId") String flowInstanceId,
    @Parameter(name = "effectiveForSubFlowInstance", description = "是否对子流程实例生效", in = ParameterIn.QUERY, example = "true")
    @QueryParam("effectiveForSubFlowInstance") @DefaultValue("true") boolean effectiveForSubFlowInstance) {
    return flowApplicationService.getHistoryUserTaskList(projectId, flowInstanceId, effectiveForSubFlowInstance);
  }

  @Operation(summary = "获取流程实例历史元素列表", description = "获取指定流程实例的历史元素列表，主要用于显示流程快照视图。包含流程中已执行的所有节点信息。")
  @APIResponse(
    name = "200",
    responseCode = "200",
    description = "成功返回历史元素列表",
    content = {@Content(
      mediaType = "application/json",
      schema = @Schema(
        type = SchemaType.ARRAY,
        implementation = ElementInstanceSchema.class
      )
    )})
  @GET
  @Path("/instances/{flowInstanceId}/elements")
  public List<ElementInstance> getHistoryElementList(
    @PathParam("projectId") String projectId,
    @Parameter(name = "flowInstanceId", description = "流程实例ID", in = ParameterIn.PATH, required = true, example = "flow_inst_001")
    @PathParam("flowInstanceId") String flowInstanceId) {
    return flowApplicationService.getHistoryElementList(projectId, flowInstanceId);
  }

  @Operation(summary = "获取流程实例元素实例数据")
  @Path("/instances/{flowInstanceId}/data/{instanceDataId}")
  public Map<String, Object> getElementInstanceData(@PathParam("projectId") String projectId,
                                                    @PathParam("flowInstanceId") String flowInstanceId,
                                                    @PathParam("instanceDataId") String instanceDataId) {
    return flowApplicationService.getInstanceData(projectId, flowInstanceId, instanceDataId);
  }

  @Operation(summary = "获取流程列表")
  @APIResponse(
    name = "200",
    responseCode = "200",
    description = "OK",
    content = {@Content(
      mediaType = "application/json",
      schema = @Schema(
        implementation = FlowModuleListResponseSchema.class
      )
    )})
  @GET
  public PageDTO<FlowModuleResponse> findFlowList(
    @PathParam("projectId") String projectId,
    @Parameter(name = "flowKey", description = "流程模块ID", in = ParameterIn.QUERY)
    @QueryParam("flowModuleId") String flowModuleId,
    @QueryParam("flowKey") String flowKey,
    @Parameter(name = "flowName", description = "流程名称", in = ParameterIn.QUERY)
    @QueryParam("flowName") String flowName,
    @Parameter(name = "page", description = "页码", in = ParameterIn.QUERY)
    @QueryParam("page") @DefaultValue("1") Integer page,
    @Parameter(name = "size", description = "每页大小", in = ParameterIn.QUERY)
    @QueryParam("size") @DefaultValue("20") Integer size) {
    FlowModuleListRequest request = new FlowModuleListRequest();
    request.setProjectId(projectId);
    request.setFlowModuleId(flowModuleId);
    request.setFlowKey(flowKey);
    request.setFlowName(flowName);
    request.setPage(page);
    request.setSize(size);
    return flowApplicationService.findFlowModuleList(request);
  }

  @Operation(summary = "获取流程实例列表")
  @APIResponse(
    name = "200",
    responseCode = "200",
    description = "OK",
    content = {@Content(
      mediaType = "application/json",
      schema = @Schema(
        implementation = FlowInstanceListResponseSchema.class
      )
    )})
  @GET
  @Path("/instances")
  public PageDTO<FlowInstanceResponse> findFlowInstanceList(
    @PathParam("projectId") String projectId,
    @Parameter(name = "flowInstanceId", description = "流程实例ID", in = ParameterIn.QUERY)
    @QueryParam("flowInstanceId") String flowInstanceId,
    @Parameter(name = "flowModuleId", description = "流程模块ID", in = ParameterIn.QUERY)
    @QueryParam("flowModuleId") String flowModuleId,
    @Parameter(name = "flowDeployId", description = "流程部署ID", in = ParameterIn.QUERY)
    @QueryParam("flowDeployId") String flowDeployId,
    @Parameter(name = "status", description = "流程实例状态", in = ParameterIn.QUERY)
    @QueryParam("status") Integer status,
    @Parameter(name = "caller", description = "调用者", in = ParameterIn.QUERY)
    @QueryParam("caller") String caller,
    @Parameter(name = "page", description = "页码", in = ParameterIn.QUERY)
    @QueryParam("page") @DefaultValue("1") Integer page,
    @Parameter(name = "size", description = "每页大小", in = ParameterIn.QUERY)
    @QueryParam("size") @DefaultValue("20") Integer size) {
    FlowInstanceListRequest request = new FlowInstanceListRequest();
    request.setProjectId(projectId);
    request.setFlowInstanceId(flowInstanceId);
    request.setFlowModuleId(flowModuleId);
    request.setFlowDeployId(flowDeployId);
    request.setStatus(status);
    request.setCaller(caller);
    request.setPage(page);
    request.setSize(size);
    return flowApplicationService.findFlowInstanceList(request);
  }

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
  public CreateFlowResult createFlow(@PathParam("projectId") String projectId, CreateFlowParam createFlowParam) {

    createFlowParam.setProjectId(projectId);
    createFlowParam.setCaller("admin");
    createFlowParam.setOperator("admin");
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
    @PathParam("projectId") String projectId,
    @Parameter(name = "flowModuleId", description = "流程模块ID", in = ParameterIn.PATH)
    @PathParam("flowModuleId") String flowModuleId,
    DeployFlowParam deployFlowParam) {
    deployFlowParam.setProjectId(projectId);
    deployFlowParam.setFlowModuleId(flowModuleId);
    deployFlowParam.setCaller("admin");
    deployFlowParam.setOperator("admin");
    return flowApplicationService.deployFlow(deployFlowParam);
  }

  @Operation(summary = "更新流程")
  @PUT
  @Path("/{flowModuleId}")
  @RequestBody(
    name = "请求体",
    content = {@Content(
      mediaType = "application/json",
      schema = @Schema(
        implementation = UpdateFlowParamSchema.class
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
        implementation = UpdateFlowResultSchema.class
      )
    )})
  public UpdateFlowResult updateFlow(
    @PathParam("projectId") String projectId,
    @Parameter(name = "flowModuleId", description = "流程模块ID", in = ParameterIn.PATH)
    @PathParam("flowModuleId") String flowModuleId,
    UpdateFlowParam updateFlowParam) {
    updateFlowParam.setFlowModuleId(flowModuleId);
    updateFlowParam.setProjectId(projectId);
    updateFlowParam.setCaller(updateFlowParam.getCaller());
    updateFlowParam.setOperator(updateFlowParam.getOperator());
    return flowApplicationService.updateFlow(updateFlowParam);
  }

  @DELETE
  @Path("/{flowModuleId}")
  public void deleteFlow(@PathParam("projectId") String projectId, @Parameter(name = "flowModuleId", description = "流程模块ID", in = ParameterIn.PATH)
  @PathParam("flowModuleId") String flowModuleId) {
    flowApplicationService.deleteFlow(projectId, flowModuleId);
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
    @PathParam("projectId") String projectId,
    @Parameter(name = "flowModuleId", description = "流程模块ID", in = ParameterIn.PATH)
    @PathParam("flowModuleId") String flowModuleId,
    @Parameter(name = "flowDeployId", description = "流程部署ID", in = ParameterIn.QUERY)
    @QueryParam("flowDeployId") String flowDeployId) {
    GetFlowModuleParam param = new GetFlowModuleParam();
    param.setProjectId(projectId);
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
  public StartProcessResult startProcess(@PathParam("projectId") String projectId, StartProcessParam startProcessParam) {
    startProcessParam.setProjectId(projectId);
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
    @PathParam("projectId") String projectId,
    @Parameter(name = "flowInstanceId", description = "流程实例ID", in = ParameterIn.PATH)
    @PathParam("flowInstanceId") String flowInstanceId,
    CommitTaskParam commitTaskParam) {
    commitTaskParam.setProjectId(projectId);
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
    @PathParam("projectId") String projectId,
    @Parameter(name = "flowInstanceId", description = "流程实例ID", in = ParameterIn.PATH)
    @PathParam("flowInstanceId") String flowInstanceId,
    RollbackTaskParam rollbackTaskParam) {
    rollbackTaskParam.setProjectId(projectId);
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
    @PathParam("projectId") String projectId,
    @Parameter(name = "flowInstanceId", description = "流程实例ID", in = ParameterIn.PATH)
    @PathParam("flowInstanceId") String flowInstanceId,
    @Parameter(name = "effectiveForSubFlowInstance", description = "是否对子流程实例生效", in = ParameterIn.QUERY)
    @QueryParam("effectiveForSubFlowInstance") @DefaultValue("true") boolean effectiveForSubFlowInstance) {
    return flowApplicationService.terminateProcess(projectId, flowInstanceId, effectiveForSubFlowInstance);
  }

  @Operation(summary = "获取流程实例信息")
  @APIResponse(
    name = "200",
    responseCode = "200",
    description = "OK",
    content = {@Content(
      mediaType = "application/json",
      schema = @Schema(
        implementation = FlowInstanceSchema.class
      )
    )})
  @GET
  @Path("/instances/{flowInstanceId}")
  public FlowInstance getFlowInstance(
    @PathParam("projectId") String projectId,
    @Parameter(name = "flowInstanceId", description = "流程实例ID", in = ParameterIn.PATH)
    @PathParam("flowInstanceId") String flowInstanceId) {
    return flowApplicationService.findFlowInstance(projectId, flowInstanceId);
  }

  @Schema(
    properties = {
      @SchemaProperty(name = "flowKey", examples = {"order_process"}, description = "流程键"),
      @SchemaProperty(name = "flowName", examples = {"订单处理流程"}, description = "流程名称"),
      @SchemaProperty(name = "remark", examples = {"处理订单的完整业务流程"}, description = "备注"),
      @SchemaProperty(name = "projectId", examples = {"default"}, description = "项目ID"),
      @SchemaProperty(name = "caller", examples = {"admin"}, description = "调用者"),
      @SchemaProperty(name = "operator", examples = {"admin"}, description = "操作者")
    }
  )
  public static class CreateFlowParamSchema extends CreateFlowParam {
    public CreateFlowParamSchema() {
      super("default", "admin");
    }
  }

  @Schema(
    properties = {
      @SchemaProperty(name = "errCode", examples = {"0"}, description = "错误码"),
      @SchemaProperty(name = "errMsg", examples = {"success"}, description = "错误信息"),
      @SchemaProperty(name = "flowModuleId", examples = {"flow_module_001"}, description = "流程模块ID")
    }
  )
  public static class CreateFlowResultSchema extends CreateFlowResult {
  }

  @Schema(
    properties = {
      @SchemaProperty(name = "flowModuleId", examples = {"flow_module_001"}, description = "流程模块ID"),
      @SchemaProperty(name = "projectId", examples = {"default"}, description = "项目ID"),
      @SchemaProperty(name = "caller", examples = {"admin"}, description = "调用者"),
      @SchemaProperty(name = "operator", examples = {"admin"}, description = "操作者")
    }
  )
  public static class DeployFlowParamSchema extends DeployFlowParam {
    public DeployFlowParamSchema() {
      super("default", "admin");
    }
  }

  @Schema(
    properties = {
      @SchemaProperty(name = "flowModuleId", examples = {"flow_module_001"}, description = "流程模块ID"),
      @SchemaProperty(name = "projectId", examples = {"default"}, description = "项目ID"),
      @SchemaProperty(name = "caller", examples = {"admin"}, description = "调用者"),
      @SchemaProperty(name = "operator", examples = {"admin"}, description = "操作者")
    }
  )
  public static class UpdateFlowParamSchema extends DeployFlowParam {
    public UpdateFlowParamSchema() {
      super("default", "admin");
    }
  }

  @Schema(
    properties = {
      @SchemaProperty(name = "errCode", examples = {"0"}, description = "错误码"),
      @SchemaProperty(name = "errMsg", examples = {"success"}, description = "错误信息"),
      @SchemaProperty(name = "flowDeployId", examples = {"flow_deploy_001"}, description = "流程部署ID"),
      @SchemaProperty(name = "flowModuleId", examples = {"flow_module_001"}, description = "流程模块ID")
    }
  )
  public static class DeployFlowResultSchema extends DeployFlowResult {
  }

  @Schema(
    properties = {
      @SchemaProperty(name = "errCode", examples = {"0"}, description = "错误码"),
      @SchemaProperty(name = "errMsg", examples = {"success"}, description = "错误信息"),
    }
  )
  public static class UpdateFlowResultSchema extends UpdateFlowResult {

  }

  @Schema(
    properties = {
      @SchemaProperty(name = "errCode", examples = {"0"}, description = "错误码"),
      @SchemaProperty(name = "errMsg", examples = {"success"}, description = "错误信息"),
      @SchemaProperty(name = "flowModuleId", examples = {"flow_module_001"}, description = "流程模块ID"),
      @SchemaProperty(name = "flowDeployId", examples = {"flow_deploy_001"}, description = "流程部署ID")
    }
  )
  public static class FlowModuleResultSchema extends FlowModuleResult {
  }

  @Schema(
    properties = {
      @SchemaProperty(name = "flowModuleId", examples = {"flow_module_001"}, description = "流程模块ID"),
      @SchemaProperty(name = "flowDeployId", examples = {"flow_deploy_001"}, description = "流程部署ID"),
      @SchemaProperty(name = "variables", description = "流程变量", type = SchemaType.ARRAY)
    }
  )
  public static class StartProcessParamSchema extends StartProcessParam {
  }

  @Schema(
    properties = {
      @SchemaProperty(name = "errCode", examples = {"0"}, description = "错误码"),
      @SchemaProperty(name = "errMsg", examples = {"success"}, description = "错误信息"),
      @SchemaProperty(name = "flowInstanceId", examples = {"flow_inst_001"}, description = "流程实例ID"),
      @SchemaProperty(name = "status", examples = {"1"}, description = "状态"),
      @SchemaProperty(name = "flowDeployId", examples = {"flow_deploy_001"}, description = "流程部署ID"),
      @SchemaProperty(name = "flowModuleId", examples = {"flow_module_001"}, description = "流程模块ID")
    }
  )
  public static class StartProcessResultSchema extends StartProcessResult {
  }

  @Schema(
    properties = {
      @SchemaProperty(name = "flowInstanceId", examples = {"flow_inst_001"}, description = "流程实例ID"),
      @SchemaProperty(name = "nodeInstanceId", examples = {"node_inst_001"}, description = "节点实例ID"),
      @SchemaProperty(name = "variables", description = "流程变量", type = SchemaType.ARRAY),
      @SchemaProperty(name = "projectId", examples = {"default"}, description = "项目ID"),
      @SchemaProperty(name = "caller", examples = {"admin"}, description = "调用者"),
      @SchemaProperty(name = "operator", examples = {"admin"}, description = "操作者")
    }
  )
  public static class CommitTaskParamSchema extends CommitTaskParam {
  }

  @Schema(
    properties = {
      @SchemaProperty(name = "errCode", examples = {"0"}, description = "错误码"),
      @SchemaProperty(name = "errMsg", examples = {"success"}, description = "错误信息"),
      @SchemaProperty(name = "flowInstanceId", examples = {"flow_inst_001"}, description = "流程实例ID"),
      @SchemaProperty(name = "status", examples = {"1"}, description = "状态")
    }
  )
  public static class CommitTaskResultSchema extends CommitTaskResult {
  }

  @Schema(
    properties = {
      @SchemaProperty(name = "flowInstanceId", examples = {"flow_inst_001"}, description = "流程实例ID"),
      @SchemaProperty(name = "nodeInstanceId", examples = {"node_inst_001"}, description = "节点实例ID"),
      @SchemaProperty(name = "projectId", examples = {"default"}, description = "项目ID"),
      @SchemaProperty(name = "caller", examples = {"admin"}, description = "调用者"),
      @SchemaProperty(name = "operator", examples = {"admin"}, description = "操作者")
    }
  )
  public static class RollbackTaskParamSchema extends RollbackTaskParam {
  }

  @Schema(
    properties = {
      @SchemaProperty(name = "errCode", examples = {"0"}, description = "错误码"),
      @SchemaProperty(name = "errMsg", examples = {"success"}, description = "错误信息"),
      @SchemaProperty(name = "flowInstanceId", examples = {"flow_inst_001"}, description = "流程实例ID"),
      @SchemaProperty(name = "status", examples = {"1"}, description = "状态")
    }
  )
  public static class RollbackTaskResultSchema extends RollbackTaskResult {
  }

  @Schema(
    properties = {
      @SchemaProperty(name = "errCode", examples = {"0"}, description = "错误码"),
      @SchemaProperty(name = "errMsg", examples = {"success"}, description = "错误信息"),
      @SchemaProperty(name = "flowInstanceId", examples = {"flow_inst_001"}, description = "流程实例ID"),
      @SchemaProperty(name = "status", examples = {"1"}, description = "状态")
    }
  )
  public static class TerminateResultSchema extends TerminateResult {
    public TerminateResultSchema() {
      super(ErrorEnum.SUCCESS);
    }
  }

  @Schema(
    properties = {
      @SchemaProperty(name = "list", description = "流程模块列表", type = SchemaType.ARRAY),
      @SchemaProperty(name = "total", examples = {"100"}, description = "总记录数")
    }
  )
  @Getter
  @Setter
  public static class FlowModuleListResponseSchema {
    private java.util.List<FlowModuleResponseSchema> list;
    private Long total;
  }

  @Schema(
    properties = {
      @SchemaProperty(name = "flowModuleId", examples = {"flow_module_001"}, description = "流程模块ID"),
      @SchemaProperty(name = "flowName", examples = {"订单处理流程"}, description = "流程名称"),
      @SchemaProperty(name = "flowKey", examples = {"order_process"}, description = "流程键"),
      @SchemaProperty(name = "status", examples = {"4"}, description = "状态：1-草稿，2-设计，3-测试，4-已发布"),
      @SchemaProperty(name = "remark", examples = {"处理订单的完整业务流程"}, description = "备注"),
      @SchemaProperty(name = "projectId", examples = {"default"}, description = "项目ID"),
      @SchemaProperty(name = "caller", examples = {"admin"}, description = "调用者"),
      @SchemaProperty(name = "operator", examples = {"admin"}, description = "操作者"),
      @SchemaProperty(name = "modifyTime", description = "修改时间", readOnly = true)
    }
  )
  public static class FlowModuleResponseSchema extends FlowModuleResponse {
  }

  @Schema(
    properties = {
      @SchemaProperty(name = "list", description = "流程实例列表", type = SchemaType.ARRAY),
      @SchemaProperty(name = "total", examples = {"100"}, description = "总记录数")
    }
  )
  @Getter
  @Setter
  public static class FlowInstanceListResponseSchema {
    private java.util.List<FlowInstanceSchema> list;
    private Long total;

    public FlowInstanceListResponseSchema() {
    }

  }

  @Schema(
    properties = {
      @SchemaProperty(name = "flowInstanceId", examples = {"flow_inst_001"}, description = "流程实例ID"),
      @SchemaProperty(name = "flowModuleId", examples = {"flow_module_001"}, description = "流程模块ID"),
      @SchemaProperty(name = "flowDeployId", examples = {"flow_deploy_001"}, description = "流程部署ID"),
      @SchemaProperty(name = "status", examples = {"1"}, description = "流程实例状态：1-运行中，2-已完成，3-已终止，4-已暂停"),
      @SchemaProperty(name = "parentFlowInstanceId", examples = {"parent_inst_001"}, description = "父流程实例ID"),
      @SchemaProperty(name = "projectId", examples = {"default"}, description = "项目ID"),
      @SchemaProperty(name = "caller", examples = {"admin"}, description = "调用者"),
      @SchemaProperty(name = "operator", examples = {"admin"}, description = "操作者"),
      @SchemaProperty(name = "createTime", description = "创建时间", readOnly = true),
      @SchemaProperty(name = "modifyTime", description = "修改时间", readOnly = true)
    }
  )
  @Getter
  @Setter
  public static class FlowInstanceSchema {
    private String flowInstanceId;
    private String flowModuleId;
    private String flowDeployId;
    private Integer status;
    private String parentFlowInstanceId;
    private String tenant;
    private String caller;
    private java.time.LocalDateTime createTime;
    private java.time.LocalDateTime modifyTime;
  }

  @Schema(
    properties = {
      @SchemaProperty(name = "nodeInstanceId", examples = {"node_inst_001"}, description = "节点实例ID"),
      @SchemaProperty(name = "Key", examples = {"user_task_001"}, description = "模型键"),
      @SchemaProperty(name = "modelName", examples = {"用户审批任务"}, description = "模型名称"),
      @SchemaProperty(name = "status", examples = {"2"}, description = "节点状态：1-待处理，2-已完成，3-已跳过，4-已禁用"),
      @SchemaProperty(name = "flowElementType", examples = {"1"}, description = "流程元素类型：1-用户任务，2-服务任务，3-网关，4-事件"),
      @SchemaProperty(name = "properties", description = "节点属性", type = SchemaType.OBJECT),
      @SchemaProperty(name = "subNodeResultList", description = "子节点执行结果列表", type = SchemaType.ARRAY),
      @SchemaProperty(name = "subFlowInstanceIdList", description = "子流程实例ID列表", type = SchemaType.ARRAY),
      @SchemaProperty(name = "subElementInstanceList", description = "子元素实例列表", type = SchemaType.ARRAY),
      @SchemaProperty(name = "instanceDataId", examples = {"data_001"}, description = "实例数据ID"),
      @SchemaProperty(name = "createTime", description = "创建时间", readOnly = true),
      @SchemaProperty(name = "modifyTime", description = "修改时间", readOnly = true)
    }
  )
  public static class NodeInstanceSchema extends NodeInstance {
  }

  @Schema(
    properties = {
      @SchemaProperty(name = "Key", examples = {"start_event_001"}, description = "键"),
      @SchemaProperty(name = "name", examples = {"开始事件"}, description = "名称"),
      @SchemaProperty(name = "status", examples = {"2"}, description = "元素状态：1-待处理，2-已完成，3-已跳过，4-已禁用"),
      @SchemaProperty(name = "properties", description = "元素属性", type = SchemaType.OBJECT),
      @SchemaProperty(name = "nodeInstanceId", examples = {"node_inst_001"}, description = "节点实例ID"),
      @SchemaProperty(name = "subFlowInstanceIdList", description = "子流程实例ID列表", type = SchemaType.ARRAY),
      @SchemaProperty(name = "subElementInstanceList", description = "子元素实例列表", type = SchemaType.ARRAY),
      @SchemaProperty(name = "instanceDataId", examples = {"data_001"}, description = "实例数据ID")
    }
  )
  public static class ElementInstanceSchema extends ElementInstance {
  }

  @Schema(
    properties = {
      @SchemaProperty(name = "errCode", examples = {"0"}, description = "错误码"),
      @SchemaProperty(name = "errMsg", examples = {"success"}, description = "错误信息"),
      @SchemaProperty(name = "flowInstanceId", examples = {"flow_inst_001"}, description = "流程实例ID"),
      @SchemaProperty(name = "status", examples = {"2"}, description = "流程状态：1-运行中，2-已完成，3-已终止，4-已暂停"),
      @SchemaProperty(name = "nodeExecuteResults", description = "节点执行结果列表", type = SchemaType.ARRAY),
      @SchemaProperty(name = "extendProperties", description = "扩展属性", type = SchemaType.OBJECT)
    }
  )
  public static class RuntimeResultSchema extends RuntimeResult {
  }

  @Schema(
    properties = {
      @SchemaProperty(name = "errCode", examples = {"0"}, description = "错误码"),
      @SchemaProperty(name = "errMsg", examples = {"success"}, description = "错误信息"),
      @SchemaProperty(name = "activeTaskInstance", description = "活跃任务实例"),
      @SchemaProperty(name = "variables", description = "流程变量列表", type = SchemaType.ARRAY)
    }
  )
  public static class NodeExecuteResultSchema extends RuntimeResult.NodeExecuteResult {
  }

}
