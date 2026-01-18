package dev.flexmodel.domain.model.flow.shared.util;

import java.util.UUID;

public final class StrongUuidGenerator implements IdGenerator {


  public StrongUuidGenerator() {
    initGenerator();
  }

  private void initGenerator() {
  }

  public String getNextId() {
    return UUID.randomUUID().toString();
  }

}
