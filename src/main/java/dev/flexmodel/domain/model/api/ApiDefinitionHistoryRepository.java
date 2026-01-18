package dev.flexmodel.domain.model.api;

import dev.flexmodel.codegen.entity.ApiDefinitionHistory;

import java.util.List;

/**
 * @author cjbi
 */
public interface ApiDefinitionHistoryRepository {
  /**
   * 根据apiDefinitionId查询历史记录
   *
   * @param projectId projectId
   * @param apiDefinitionId apiDefinitionId
   * @return apiDefinitionHistory
   */
  List<ApiDefinitionHistory> findByApiDefinitionId(String projectId, String apiDefinitionId);

  /**
   * 保存历史记录
   *
   * @param projectId projectId
   * @param apiDefinitionHistory apiDefinitionHistory
   * @return apiDefinitionHistory
   */
  ApiDefinitionHistory save(String projectId, ApiDefinitionHistory apiDefinitionHistory);

  ApiDefinitionHistory findById(String projectId, String historyId);
}
