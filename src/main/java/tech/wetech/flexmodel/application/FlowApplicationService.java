package tech.wetech.flexmodel.application;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.wetech.flexmodel.domain.model.flow.service.ProcessService;

/**
 * @author cjbi
 */
@ApplicationScoped
public class FlowApplicationService {

  @Inject
  ProcessService processService;


}
