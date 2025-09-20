package tech.wetech.flexmodel.application;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.quartz.Scheduler;
import tech.wetech.flexmodel.application.dto.PageDTO;
import tech.wetech.flexmodel.application.dto.TriggerDTO;
import tech.wetech.flexmodel.codegen.entity.FlowDeployment;
import tech.wetech.flexmodel.codegen.entity.Trigger;
import tech.wetech.flexmodel.domain.model.flow.service.FlowDeploymentService;
import tech.wetech.flexmodel.domain.model.trigger.TriggerException;
import tech.wetech.flexmodel.domain.model.trigger.TriggerService;
import tech.wetech.flexmodel.query.Expressions;
import tech.wetech.flexmodel.query.Predicate;

import java.util.List;

/**
 * @author cjbi
 */
@ApplicationScoped
public class ScheduleApplicationService {

  @Inject
  TriggerService triggerService;
  @Inject
  FlowDeploymentService flowService;
  @Inject
  Scheduler scheduler;

  private TriggerDTO toTriggerDTO(Trigger trigger) {
    if (trigger == null) {
      return null;
    }
    TriggerDTO dto = new TriggerDTO();
    dto.setId(trigger.getId());
    dto.setName(trigger.getName());
    dto.setDescription(trigger.getDescription());
    dto.setType(trigger.getType());
    dto.setConfig(trigger.getConfig());
    dto.setJobId(trigger.getJobId());
    dto.setJobType(trigger.getJobType());
    dto.setState(trigger.getState());
    dto.setCreatedAt(trigger.getCreatedAt());
    dto.setUpdatedAt(trigger.getUpdatedAt());
    FlowDeployment flowDeployment = flowService.findRecentByFlowModuleId(trigger.getJobId());
    if (flowDeployment != null) {
      dto.setJobName(flowDeployment.getFlowName());
    }
    return dto;
  }

  public TriggerDTO findById(String id) {
    return toTriggerDTO(triggerService.findById(id));
  }

  public Trigger create(Trigger trigger) {
    return triggerService.save(trigger);
  }

  public Trigger update(Trigger req) {
    Trigger record = findById(req.getId());
    if (record == null) {
      throw new TriggerException("记录不存在");
    }
    return triggerService.save(req);
  }

  public void deleteById(String id) {
    triggerService.deleteById(id);
  }

  public PageDTO<TriggerDTO> findPage(String name, Integer page, Integer size) {
    Predicate filter = Expressions.TRUE;
    if (name != null) {
      filter = filter.and(Expressions.field("name").eq(name));
    }
    long total = triggerService.count(filter);
    if (total == 0) {
      return PageDTO.empty();
    }
    List<TriggerDTO> triggers = triggerService.find(filter, page, size).stream()
      .map(this::toTriggerDTO)
      .toList();
    return new PageDTO<>(triggers, total);
  }

  public void executeNow(String id) {

  }
}
