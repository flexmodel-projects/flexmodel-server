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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@QuarkusTest
@QuarkusTestResource(SQLiteTestResource.class)
public class StartEventValidatorTest {

  @Inject
  StartEventValidator startEventValidator;

  /**
   * Test startEvent's incoming, whlile normal.
   *
   */
  @Test
  public void checkIncomingAccess() {
    FlowElement startEvent = EntityBuilder.buildStartEvent();
    Map<String, FlowElement> map = new HashMap<>();
    map.put(startEvent.getKey(), startEvent);
    startEventValidator.checkIncoming(map, startEvent);
  }

  /**
   * Test startEvent's incoming, whlile incoming is too much.
   *
   */
  @Test
  public void checkTooManyIncoming() {
    FlowElement startEventVaild = EntityBuilder.buildStartEvent();
    List<String> incomings = new ArrayList<>();
    incomings.add("sequence");
    startEventVaild.setIncoming(incomings);
    Map<String, FlowElement> map = new HashMap<>();
    map.put("startEvent", startEventVaild);
    startEventValidator.checkIncoming(map, startEventVaild);
  }

  /**
   * Test startEvent's incoming, whlile normal.
   *
   */
  @Test
  public void checkOutgoingAccess() {
    FlowElement startEvent = EntityBuilder.buildStartEvent();
    Map<String, FlowElement> map = new HashMap<>();
    map.put(startEvent.getKey(), startEvent);
    boolean access = false;
    try {
      startEventValidator.checkOutgoing(map, startEvent);
      access = true;
      Assertions.assertTrue(access);
    } catch (DefinitionException e) {
      log.error("", e);
      Assertions.assertTrue(access);
    }
  }

  /**
   * Test startEvent's incoming, whlile incoming is too much.
   *
   */
  @Test
  public void checkTooMuchOutgoing() {
    FlowElement startEventVaild = EntityBuilder.buildStartEvent();
    List<String> outgoings = new ArrayList<>();
    startEventVaild.setOutgoing(outgoings);
    Map<String, FlowElement> map = new HashMap<>();
    map.put("startEvent", startEventVaild);
    boolean access = false;
    try {
      startEventValidator.checkOutgoing(map, startEventVaild);
      access = true;
      Assertions.assertFalse(access);
    } catch (DefinitionException e) {
      log.error("", e);
      Assertions.assertFalse(access);
    }

  }
}
