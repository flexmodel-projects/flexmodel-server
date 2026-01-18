package dev.flexmodel.domain.model.flow.plugin;

import dev.flexmodel.domain.model.flow.shared.util.IdGenerator;

public interface IdGeneratorPlugin extends Plugin {
  IdGenerator getIdGenerator();
}
