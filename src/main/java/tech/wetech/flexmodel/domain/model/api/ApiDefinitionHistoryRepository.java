package tech.wetech.flexmodel.domain.model.api;

import tech.wetech.flexmodel.codegen.entity.ApiDefinitionHistory;

import java.util.List;

/**
 * @author cjbi
 */
public interface ApiDefinitionHistoryRepository {
  /**
   * 根据apiDefinitionId查询历史记录
   *
   * @param apiDefinitionId apiDefinitionId
   * @return apiDefinitionHistory
   */
  List<ApiDefinitionHistory> findByApiDefinitionId(String apiDefinitionId);

  /**
   * 保存历史记录
   *
   * @param apiDefinitionHistory apiDefinitionHistory
   * @return apiDefinitionHistory
   */
  ApiDefinitionHistory save(ApiDefinitionHistory apiDefinitionHistory);

  ApiDefinitionHistory findById(String historyId);
}
