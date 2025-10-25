package tech.wetech.flexmodel.domain.model.api;

import tech.wetech.flexmodel.codegen.entity.ApiDefinition;

import java.util.List;

/**
 * @author cjbi
 */
public interface ApiDefinitionRepository {

  void deleteByParentId(String parentId);

  ApiDefinition findById(String id);

  List<ApiDefinition> findAll();

  List<ApiDefinition> findByTenantId(String tenantId);

  ApiDefinition save(ApiDefinition record);

  void delete(String id);

}
