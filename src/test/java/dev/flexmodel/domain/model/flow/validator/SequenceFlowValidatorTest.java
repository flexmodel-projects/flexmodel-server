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
public class SequenceFlowValidatorTest {

  @Inject
  SequenceFlowValidator sequenceFlowValidator;

  /**
   * Test sequenceFlow's checkIncoming, while incoming is normal.
   *
   */
  @Test
  public void checkIncomingAccess() {
    FlowElement sequenceFlow = EntityBuilder.buildSequenceFlow();
    Map<String, FlowElement> flowElementMap = new HashMap<>();
    flowElementMap.put(sequenceFlow.getKey(), sequenceFlow);
    boolean access = false;
    try {
      sequenceFlowValidator.checkIncoming(flowElementMap, sequenceFlow);
      access = true;
      Assertions.assertTrue(access);
    } catch (DefinitionException e) {
      log.error("", e);
      Assertions.assertTrue(access);
    }
  }


  /**
   * Test sequenceFlow's checkIncoming, while incoming  is too much.
   *
   */
  @Test
  public void checkTooMuchIncoming() {
    FlowElement sequenceFlow = EntityBuilder.buildSequenceFlow();
    List<String> sfIncomings = new ArrayList<>();
    sfIncomings.add("userTask2");
    sfIncomings.add("userTask1");
    sequenceFlow.setIncoming(sfIncomings);
    Map<String, FlowElement> flowElementMap = new HashMap<>();
    flowElementMap.put(sequenceFlow.getKey(), sequenceFlow);
    boolean access = false;
    try {
      sequenceFlowValidator.checkIncoming(flowElementMap, sequenceFlow);
      access = true;
      Assertions.assertFalse(access);
    } catch (DefinitionException e) {
      log.error("", e);
      Assertions.assertFalse(access);
    }
  }

  /**
   * Test sequenceFlow's checkOutgoing, while outgoing is normal.
   *
   */
  @Test
  public void checkOutgoingAccess() {

    FlowElement sequenceFlow = EntityBuilder.buildSequenceFlow();
    Map<String, FlowElement> flowElementMap = new HashMap<>();
    flowElementMap.put(sequenceFlow.getKey(), sequenceFlow);
    boolean access = false;
    try {
      sequenceFlowValidator.checkOutgoing(flowElementMap, sequenceFlow);
      access = true;
      Assertions.assertTrue(access);
    } catch (DefinitionException e) {
      log.error("", e);
      Assertions.assertTrue(access);
    }
  }

  /**
   * Test sequenceFlow's outgoing, while outgoing is lack.
   *
   */
  @Test
  public void checkWithoutOutgoing() {

    FlowElement sequenceFlow = EntityBuilder.buildSequenceFlow();
    sequenceFlow.setOutgoing(null);
    Map<String, FlowElement> flowElementMap = new HashMap<>();
    flowElementMap.put(sequenceFlow.getKey(), sequenceFlow);
    boolean access = false;
    try {
      sequenceFlowValidator.checkOutgoing(flowElementMap, sequenceFlow);
      access = true;
      Assertions.assertFalse(access);
    } catch (DefinitionException e) {
      log.error("", e);
      Assertions.assertFalse(access);
    }
  }
}
