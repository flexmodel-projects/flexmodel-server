package dev.flexmodel.interfaces.rest.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.jackson.ObjectMapperCustomizer;
import jakarta.inject.Singleton;
import dev.flexmodel.interfaces.rest.json.jackson.FlexmodelServerModule;

@Singleton
public class RegisterCustomModuleCustomizer implements ObjectMapperCustomizer {

  @Override
  public void customize(ObjectMapper objectMapper) {
    objectMapper.registerModule(new FlexmodelServerModule());
  }
}
