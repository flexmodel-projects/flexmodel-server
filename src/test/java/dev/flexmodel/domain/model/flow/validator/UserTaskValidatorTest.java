package dev.flexmodel.domain.model.flow.validator;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import dev.flexmodel.SQLiteTestResource;
import dev.flexmodel.domain.model.flow.dto.model.FlowElement;
import dev.flexmodel.domain.model.flow.exception.DefinitionException;
import dev.flexmodel.domain.model.flow.shared.EntityBuilder;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@QuarkusTest
@QuarkusTestResource(SQLiteTestResource.class)
public class UserTaskValidatorTest {

  @Inject
  UserTaskValidator userTaskValidator;

  /**
   * Check userTask's incoming, whlile normal.
   *
   */
  @Test
  public void checkIncomingAccess() {
    FlowElement userTask = EntityBuilder.buildUserTask();
    Map<String, FlowElement> map = new HashMap<>();
    map.put(userTask.getKey(), userTask);
    boolean access = false;
    try {
      userTaskValidator.checkIncoming(map, userTask);
      access = true;
      Assertions.assertTrue(access);
    } catch (DefinitionException e) {
      log.error("", e);
      Assertions.assertTrue(access);
    }
  }

  /**
   * Check userTask's incoming, while incoming is lack.
   *
   */
  @Test
  public void checkWithoutIncoming() {
    FlowElement userTask = EntityBuilder.buildUserTask();
    userTask.setIncoming(null);
    Map<String, FlowElement> map = new HashMap<>();
    map.put(userTask.getKey(), userTask);
    boolean access = false;
    try {
      userTaskValidator.checkIncoming(map, userTask);
      access = true;
      Assertions.assertFalse(access);
    } catch (DefinitionException e) {
      log.error("", e);
      Assertions.assertFalse(access);
    }
  }


  /**
   * Check userTask's outgoing, whlile normal.
   *
   */
  @Test
  public void checkOutgoingAccess() {
    FlowElement userTask = EntityBuilder.buildUserTask();
    Map<String, FlowElement> map = new HashMap<>();
    map.put(userTask.getKey(), userTask);
    boolean access = false;
    try {
      userTaskValidator.checkOutgoing(map, userTask);
      access = true;
      Assertions.assertTrue(access);
    } catch (DefinitionException e) {
      log.error("", e);
      Assertions.assertTrue(access);
    }
  }

  /**
   * Check userTask's outgoing, while outgoing is lack.
   *
   */
  @Test
  public void checkWithoutOutgoing() {
    FlowElement userTask = EntityBuilder.buildUserTask();
    userTask.setOutgoing(null);
    Map<String, FlowElement> map = new HashMap<>();
    map.put(userTask.getKey(), userTask);
    boolean access = false;
    try {
      userTaskValidator.checkOutgoing(map, userTask);
      access = true;
      Assertions.assertFalse(access);
    } catch (DefinitionException e) {
      log.error("", e);
      Assertions.assertFalse(access);
    }
  }
}
