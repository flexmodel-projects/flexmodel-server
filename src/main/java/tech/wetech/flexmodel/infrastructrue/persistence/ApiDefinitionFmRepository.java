package tech.wetech.flexmodel.infrastructrue.persistence;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.wetech.flexmodel.codegen.dao.ApiDefinitionDAO;
import tech.wetech.flexmodel.codegen.entity.ApiDefinition;
import tech.wetech.flexmodel.domain.model.api.ApiDefinitionRepository;

import java.util.List;

import static tech.wetech.flexmodel.codegen.System.apiDefinition;

/**
 * @author cjbi
 */
@ApplicationScoped
public class ApiDefinitionFmRepository implements ApiDefinitionRepository {

  @Inject
  ApiDefinitionDAO apiDefinitionDAO;

  @Override
  public void deleteByParentId(String parentId) {
    apiDefinitionDAO.delete(apiDefinition.parentId.eq(parentId));
  }

  @Override
  public ApiDefinition findById(String id) {
    return apiDefinitionDAO.findById(id);
  }

  @Override
  public List<ApiDefinition> findAll() {
    return apiDefinitionDAO.findAll();
  }

  @Override
  public ApiDefinition save(ApiDefinition record) {
    return apiDefinitionDAO.save(record);
  }

  @Override
  public void delete(String id) {
    apiDefinitionDAO.deleteById(id);
  }

  @Override
  public void updateIgnoreNull(String id, ApiDefinition apiDefinition) {
    apiDefinitionDAO.updateIgnoreNullById(apiDefinition, id);
  }

}
