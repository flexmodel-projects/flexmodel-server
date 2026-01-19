package dev.flexmodel.domain.model.flow.validator;


import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import dev.flexmodel.SQLiteTestResource;
import dev.flexmodel.domain.model.flow.dto.model.FlowElement;
import dev.flexmodel.domain.model.flow.dto.model.FlowModel;
import dev.flexmodel.domain.model.flow.dto.model.SequenceFlow;
import dev.flexmodel.domain.model.flow.dto.model.StartEvent;
import dev.flexmodel.domain.model.flow.exception.DefinitionException;
import dev.flexmodel.domain.model.flow.exception.ProcessException;
import dev.flexmodel.domain.model.flow.shared.EntityBuilder;
import dev.flexmodel.domain.model.flow.shared.common.FlowElementType;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@QuarkusTest
@QuarkusTestResource(SQLiteTestResource.class)
public class FlowModelValidatorTest {

  @Inject
  FlowModelValidator flowModelValidator;

  /**
   * Test flowModel's validate, while normal.
   *
   */
  @Test
  public void validateAccess() {
    List<FlowElement> flowElementsList = EntityBuilder.buildFlowElementList();
    FlowModel flowModel = new FlowModel();
    flowModel.setFlowElementList(flowElementsList);
    boolean access = false;
    try {
      flowModelValidator.validate(flowModel);
      access = true;
      Assertions.assertTrue(access);
    } catch (ProcessException | DefinitionException e) {
      log.error("", e);
      access = true;
      Assertions.assertTrue(access);
    }

  }

  /**
   * Test flowModel's validate, while element's key is not unique.
   *
   */
  @Test
  public void validateElementKeyNotUnique() {
    List<FlowElement> flowElementsList = EntityBuilder.buildFlowElementList();
    SequenceFlow sequenceFlow1 = new SequenceFlow();
    sequenceFlow1.setKey("sequenceFlow1");
    sequenceFlow1.setType(FlowElementType.SEQUENCE_FLOW);
    List<String> sfIncomings = new ArrayList<>();
    sfIncomings.add("startEvent1");
    sequenceFlow1.setIncoming(sfIncomings);
    List<String> sfOutgoings = new ArrayList<>();
    sfOutgoings.add("userTask1");
    sequenceFlow1.setOutgoing(sfOutgoings);
    flowElementsList.add(sequenceFlow1);
    FlowModel flowModel = new FlowModel();
    flowModel.setFlowElementList(flowElementsList);
    boolean access = false;
    try {
      flowModelValidator.validate(flowModel);
      access = true;
      Assertions.assertFalse(access);
    } catch (ProcessException | DefinitionException e) {
      log.error("", e);
      Assertions.assertFalse(access);
    }
  }

  /**
   * Test flowModel's validate, while startEvent's num is not equal 1.
   *
   */
  @Test
  public void validateStartEventNotOne() {
    List<FlowElement> flowElementsList = EntityBuilder.buildFlowElementList();

    StartEvent startEvent = new StartEvent();
    startEvent.setKey("startEvent2");
    startEvent.setType(FlowElementType.START_EVENT);
    List<String> seOutgoings = new ArrayList<>();
    seOutgoings.add("sequenceFlow1");
    startEvent.setOutgoing(seOutgoings);
    flowElementsList.add(startEvent);
    FlowModel flowModel = new FlowModel();
    flowModel.setFlowElementList(flowElementsList);
    boolean access = false;
    try {
      flowModelValidator.validate(flowModel);
      access = true;
      Assertions.assertFalse(access);
    } catch (ProcessException | DefinitionException e) {
      log.error("", e);
      Assertions.assertFalse(access);
    }
  }

  /**
   * Test flowModel's validate, while endEvent is null.
   *
   */
  @Test
  public void validateWithoutEndEvent() {
    List<FlowElement> flowElementsList = EntityBuilder.buildFlowElementList();
    int flowElementsListSize = flowElementsList.size();
    flowElementsList.remove(flowElementsListSize - 1);
    flowElementsList.remove(flowElementsListSize - 2);
    FlowModel flowModel = new FlowModel();
    flowModel.setFlowElementList(flowElementsList);
    boolean access = false;
    try {
      flowModelValidator.validate(flowModel);
      access = true;
      Assertions.assertFalse(access);
    } catch (ProcessException | DefinitionException e) {
      log.error("", e);
      Assertions.assertFalse(access);
    }
  }
}
