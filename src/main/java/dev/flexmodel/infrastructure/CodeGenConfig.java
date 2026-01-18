package dev.flexmodel.infrastructure;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;
import dev.flexmodel.codegen.CodeGenerationService;
import dev.flexmodel.session.SessionFactory;

/**
 * @author cjbi
 */
@ApplicationScoped
public class CodeGenConfig {

  @Produces
  @Singleton
  public CodeGenerationService codeGenerationService() {
    return new CodeGenerationService();
  }

}
