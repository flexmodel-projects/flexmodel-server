package tech.wetech.flexmodel.domain.model.flow.plugin;

import tech.wetech.flexmodel.domain.model.flow.shared.util.IdGenerator;

public interface IdGeneratorPlugin extends Plugin {
  IdGenerator getIdGenerator();
}
