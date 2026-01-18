package dev.flexmodel.domain.model.api;

import dev.flexmodel.codegen.entity.ApiDefinition;

import java.util.List;

/**
 * @author cjbi
 */
public interface ApiDefinitionRepository {

  void deleteByParentId(String projectId, String parentId);

  ApiDefinition findById(String projectId, String id);

  List<ApiDefinition> findAll(String projectId);

  List<ApiDefinition> findByProjectId(String projectId);

  ApiDefinition save(ApiDefinition record);

  void delete(String projectId, String id);

  Integer count(String projectId);
}
