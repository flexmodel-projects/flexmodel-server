package tech.wetech.flexmodel.infrastructrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.jackson.ObjectMapperCustomizer;
import jakarta.inject.Singleton;
import tech.wetech.flexmodel.infrastructrue.jackson.FlexModelServerModule;

@Singleton
public class RegisterCustomModuleCustomizer implements ObjectMapperCustomizer {

  @Override
  public void customize(ObjectMapper objectMapper) {
    objectMapper.registerModule(new FlexModelServerModule());
  }
}
