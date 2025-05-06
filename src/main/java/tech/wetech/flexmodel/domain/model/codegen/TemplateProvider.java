package tech.wetech.flexmodel.domain.model.codegen;

import java.nio.file.Path;
import java.util.List;

/**
 * @author cjbi
 */
public interface TemplateProvider {

  Path getTemplatePath(String templateName);

  List<String> getTemplateNames();

}
