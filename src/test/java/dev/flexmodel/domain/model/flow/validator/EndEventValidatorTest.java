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
public class EndEventValidatorTest {

  @Inject
  EndEventValidator endEventValidator;

  /**
   * Test endEvent's checkIncoming, while incoming is normal.
   *
   */
  @Test
  public void checkIncomingAcess() {
    FlowElement endEvent = EntityBuilder.buildEndEvent();
    Map<String, FlowElement> map = new HashMap<>();
    map.put(endEvent.getKey(), endEvent);
    boolean access = false;
    try {
      endEventValidator.checkIncoming(map, endEvent);
      access = true;
      Assertions.assertTrue(access);
    } catch (DefinitionException e) {
      log.error("", e);
      Assertions.assertTrue(access);
    }
  }


  /**
   * Test endEvent's checkIncoming, while incoming is null.
   *
   */
  @Test
  public void checkWithoutIncoming() {
    FlowElement endEventInvalid = EntityBuilder.buildEndEvent();
    endEventInvalid.setIncoming(null);
    Map<String, FlowElement> map = new HashMap<>();
    map.put(endEventInvalid.getKey(), endEventInvalid);
    boolean access = false;
    try {
      endEventValidator.checkIncoming(map, endEventInvalid);
      access = true;
      Assertions.assertFalse(access);
    } catch (DefinitionException e) {
      log.error("", e);
      Assertions.assertFalse(access);
    }
  }

  /**
   * Test endEvent's checkOutgoing, while outgoing is normal.
   *
   */
  @Test
  public void checkOutgoingAccess() {
    FlowElement endEvent = EntityBuilder.buildEndEvent();
    Map<String, FlowElement> map = new HashMap<>();
    map.put(endEvent.getKey(), endEvent);
    endEventValidator.checkOutgoing(map, endEvent);
  }

  /**
   * Test endEvent's checkOutgoing, while outgoing is not null.
   *
   */
  @Test
  public void checkOutgoingIsNotNull() {
    FlowElement endEventInvalid = EntityBuilder.buildEndEvent();
    List<String> setOutgoing = new ArrayList<>();
    setOutgoing.add("sequenceFlow2");
    endEventInvalid.setOutgoing(setOutgoing);
    Map<String, FlowElement> map = new HashMap<>();
    map.put(endEventInvalid.getKey(), endEventInvalid);
    endEventValidator.checkOutgoing(map, endEventInvalid);
  }
}
