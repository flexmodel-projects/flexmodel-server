package tech.wetech.flexmodel.application;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.wetech.flexmodel.application.dto.PageDTO;
import tech.wetech.flexmodel.application.dto.TriggerDTO;
import tech.wetech.flexmodel.codegen.entity.FlowDeployment;
import tech.wetech.flexmodel.codegen.entity.Trigger;
import tech.wetech.flexmodel.domain.model.flow.service.FlowDeploymentService;
import tech.wetech.flexmodel.domain.model.trigger.TriggerException;
import tech.wetech.flexmodel.domain.model.trigger.TriggerService;
import tech.wetech.flexmodel.query.Predicate;

import java.util.List;

/**
 * @author cjbi
 */
@ApplicationScoped
public class TriggerApplicationService {

  @Inject
  TriggerService triggerService;
  @Inject
  FlowDeploymentService flowService;

  private TriggerDTO toTriggerDTO(Trigger trigger) {
    TriggerDTO dto = new TriggerDTO();
    dto.setId(trigger.getId());
    dto.setName(trigger.getName());
    dto.setDescription(trigger.getDescription());
    dto.setType(trigger.getType());
    dto.setConfig(trigger.getConfig());
    dto.setExecutorId(trigger.getExecutorId());
    dto.setExecutorType(trigger.getExecutorType());
    dto.setState(trigger.getState());
    FlowDeployment flowDeployment = flowService.findRecentByFlowKey(trigger.getExecutorId());
    dto.setExecutorName(flowDeployment.getFlowName());
    return dto;
  }

  public Trigger findById(String id) {
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

  public PageDTO<TriggerDTO> find(Predicate filter, Integer page, Integer size) {
    long total = triggerService.count(filter);
    if (total == 0) {
      return PageDTO.empty();
    }
    List<TriggerDTO> triggers = triggerService.find(filter, page, size).stream()
      .map(this::toTriggerDTO)
      .toList();
    return new PageDTO<>(triggers, total);
  }

}
