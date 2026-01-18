package dev.flexmodel.domain.model.flow.plugin;

import dev.flexmodel.domain.model.flow.executor.ElementExecutor;
import dev.flexmodel.domain.model.flow.validator.ElementValidator;

public interface ElementPlugin extends Plugin {
  String ELEMENT_TYPE_PREFIX = "turbo.plugin.element_type.";

  ElementExecutor getElementExecutor();

  ElementValidator getElementValidator();

  Integer getFlowElementType();
}
