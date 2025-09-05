package tech.wetech.flexmodel.infrastructure;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import tech.wetech.flexmodel.codegen.CodeGenerationService;
import tech.wetech.flexmodel.session.SessionFactory;

/**
 * @author cjbi
 */
@ApplicationScoped
public class CodeGenConfig {

  @Produces
  @ApplicationScoped
  public CodeGenerationService codeGenerationService(SessionFactory sessionFactory) {
    return new CodeGenerationService(sessionFactory);
  }

}
