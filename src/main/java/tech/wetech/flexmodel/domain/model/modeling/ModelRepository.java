package tech.wetech.flexmodel.domain.model.modeling;

import javax.swing.text.html.parser.Entity;
import java.util.List;

/**
 * @author cjbi
 */
public interface ModelRepository {

  List<Entity> findAll(String datasourceName);

  Entity save(Entity entity);

}
