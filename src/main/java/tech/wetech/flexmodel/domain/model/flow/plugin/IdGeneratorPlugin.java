package tech.wetech.flexmodel.domain.model.flow.plugin;

import tech.wetech.flexmodel.domain.model.flow.util.IdGenerator;

public interface IdGeneratorPlugin extends Plugin {
  IdGenerator getIdGenerator();
}
