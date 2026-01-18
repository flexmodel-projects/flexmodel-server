package dev.flexmodel.domain.model.flow.validator;

import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import dev.flexmodel.domain.model.flow.dto.model.FlowElement;
import dev.flexmodel.domain.model.flow.shared.common.ErrorEnum;
import dev.flexmodel.shared.utils.CollectionUtils;

import java.util.List;
import java.util.Map;

@Singleton
public class EndEventValidator extends ElementValidator {

  protected static final Logger LOGGER = LoggerFactory.getLogger(EndEventValidator.class);

  /**
   * CheckOutgoing: check endEvent's outgoing, warn while outgoing is not empty.
   *
   * @param flowElementMap, flowElement
   */
  @Override
  protected void checkOutgoing(Map<String, FlowElement> flowElementMap, FlowElement flowElement) {
    List<String> outgoing = flowElement.getOutgoing();

    if (!CollectionUtils.isEmpty(outgoing)) {
      recordElementValidatorException(flowElement, ErrorEnum.ELEMENT_TOO_MUCH_OUTGOING);
    }
  }
}
