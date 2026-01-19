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
public class ExclusiveGatewayValidatorTest {


  @Inject
  ExclusiveGatewayValidator exclusiveGatewayValidator;

  /**
   * Test exclusiveGateway's checkIncoming, while exclusiveGateway's incoming is normal.
   *
   */
  @Test
  public void checkIncomingAccess() {
    FlowElement exclusiveGateway = EntityBuilder.buildExclusiveGateway();
    FlowElement outgoningSequence = EntityBuilder.buildSequenceFlow2();
    FlowElement outgoningSequence1 = EntityBuilder.buildSequenceFlow3();
    Map<String, FlowElement> map = new HashMap<>();
    map.put(exclusiveGateway.getKey(), exclusiveGateway);
    map.put(outgoningSequence.getKey(), outgoningSequence);
    map.put(outgoningSequence1.getKey(), outgoningSequence1);
    boolean access = false;
    try {
      exclusiveGatewayValidator.checkIncoming(map, exclusiveGateway);
      access = true;
      Assertions.assertTrue(access);
    } catch (DefinitionException e) {
      log.error("", e);
      Assertions.assertTrue(access);
    }
  }

  /**
   * Test exclusiveGateway's checkIncoming, while incoming is null.
   *
   */
  @Test
  public void checkWithoutIncoming() {
    FlowElement exclusiveGateway = EntityBuilder.buildExclusiveGateway();
    FlowElement outgoningSequence = EntityBuilder.buildSequenceFlow2();
    FlowElement outgoningSequence1 = EntityBuilder.buildSequenceFlow3();
    exclusiveGateway.setIncoming(null);
    Map<String, FlowElement> map = new HashMap<>();
    map.put(exclusiveGateway.getKey(), exclusiveGateway);
    map.put(outgoningSequence.getKey(), outgoningSequence);
    map.put(outgoningSequence1.getKey(), outgoningSequence1);
    boolean access = false;
    try {
      exclusiveGatewayValidator.checkIncoming(map, exclusiveGateway);
      access = true;
      Assertions.assertFalse(access);
    } catch (DefinitionException e) {
      log.error("", e);
      Assertions.assertFalse(access);
    }
  }

  /**
   * Test exclusiveGateway's checkOutgoing, while exclusiveGateway's outgoing is normal.
   *
   */
  @Test
  public void checkOutgoingAccess() {
    FlowElement exclusiveGateway = EntityBuilder.buildExclusiveGateway();
    FlowElement outgoningSequence = EntityBuilder.buildSequenceFlow2();
    FlowElement outgoningSequence1 = EntityBuilder.buildSequenceFlow3();
    Map<String, FlowElement> map = new HashMap<>();
    map.put(exclusiveGateway.getKey(), exclusiveGateway);
    map.put(outgoningSequence.getKey(), outgoningSequence);
    map.put(outgoningSequence1.getKey(), outgoningSequence1);
    boolean access = false;
    try {
      exclusiveGatewayValidator.checkOutgoing(map, exclusiveGateway);
      access = true;
      Assertions.assertTrue(access);
    } catch (DefinitionException e) {
      log.error("", e);
      Assertions.assertTrue(access);
    }
  }

  /**
   * Test exclusiveGateway's checkOutgoing, while exclusiveGateway's outgoing is empty.
   *
   */
  @Test
  public void checkEmptyOutgoing() {
    FlowElement exclusiveGateway = EntityBuilder.buildExclusiveGateway();
    FlowElement outgoningSequence = EntityBuilder.buildSequenceFlow2();
    Map<String, Object> properties = new HashMap<>();
    properties.put("defaultConditions", "false");
    properties.put("conditionsequenceflow", "");
    outgoningSequence.setProperties(properties);
    FlowElement outgoningSequence1 = EntityBuilder.buildSequenceFlow3();
    Map<String, FlowElement> map = new HashMap<>();
    map.put(exclusiveGateway.getKey(), exclusiveGateway);
    map.put(outgoningSequence.getKey(), outgoningSequence);
    map.put(outgoningSequence1.getKey(), outgoningSequence1);
    boolean access = false;
    try {
      exclusiveGatewayValidator.checkOutgoing(map, exclusiveGateway);
      access = true;
      Assertions.assertFalse(access);
    } catch (DefinitionException e) {
      log.error("", e);
      Assertions.assertFalse(access);
    }
  }

  /**
   * Test exclusiveGateway's checkOutgoing, while too many default sequence.
   *
   */
  @Test
  public void checkTooManySequenceOutgoig() {
    FlowElement exclusiveGateway = EntityBuilder.buildExclusiveGateway();
    FlowElement outgoningSequence = EntityBuilder.buildSequenceFlow2();
    Map<String, Object> properties = new HashMap<>();
    properties.put("defaultConditions", "true");
    properties.put("conditionsequenceflow", "");
    outgoningSequence.setProperties(properties);
    FlowElement outgoningSequence1 = EntityBuilder.buildSequenceFlow3();
    Map<String, Object> properties1 = new HashMap<>();
    properties.put("defaultConditions", "true");
    properties.put("conditionsequenceflow", "");
    outgoningSequence1.setProperties(properties);
    Map<String, FlowElement> map = new HashMap<>();
    map.put(exclusiveGateway.getKey(), exclusiveGateway);
    map.put(outgoningSequence.getKey(), outgoningSequence);
    map.put(outgoningSequence1.getKey(), outgoningSequence1);
    boolean access = false;
    try {
      exclusiveGatewayValidator.checkOutgoing(map, exclusiveGateway);
      access = true;
      Assertions.assertFalse(access);
    } catch (DefinitionException e) {
      log.error("", e);
      Assertions.assertFalse(access);
    }
  }
}
