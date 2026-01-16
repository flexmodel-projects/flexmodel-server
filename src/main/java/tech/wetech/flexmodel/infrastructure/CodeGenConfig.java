package tech.wetech.flexmodel.infrastructure;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;
import tech.wetech.flexmodel.codegen.CodeGenerationService;
import tech.wetech.flexmodel.session.SessionFactory;

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
