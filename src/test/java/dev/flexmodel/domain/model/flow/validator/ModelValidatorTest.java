package dev.flexmodel.domain.model.flow.validator;


import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import dev.flexmodel.SQLiteTestResource;
import dev.flexmodel.domain.model.flow.exception.DefinitionException;
import dev.flexmodel.domain.model.flow.exception.ProcessException;
import dev.flexmodel.domain.model.flow.shared.EntityBuilder;


@Slf4j
@QuarkusTest
@QuarkusTestResource(SQLiteTestResource.class)
public class ModelValidatorTest {

  @Inject
  ModelValidator modelValidator;

  /**
   * Test modelValidator, while model is normal.
   *
   */
  @Test
  public void validateAccess() {
    String modelStr = EntityBuilder.buildModelStringAccess();
    boolean access = false;
    try {
      modelValidator.validate(modelStr);
      access = true;
      Assertions.assertTrue(access);
    } catch (DefinitionException | ProcessException e) {
      log.error("", e);
      Assertions.assertTrue(access);
    }


  }

  /**
   * Test modelValidator, while model is empty.
   *
   */
  @Test
  public void validateEmptyModel() {
    String modelStr = null;
    boolean access = false;
    try {
      modelValidator.validate(modelStr);
      access = true;
      Assertions.assertFalse(access);
    } catch (DefinitionException | ProcessException e) {
      log.error("", e);
      Assertions.assertFalse(access);
    }
  }

}
