package tech.wetech.flexmodel.application;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.wetech.flexmodel.application.dto.OverviewDTO;
import tech.wetech.flexmodel.codegen.entity.ApiDefinition;
import tech.wetech.flexmodel.domain.model.api.ApiDefinitionService;
import tech.wetech.flexmodel.domain.model.api.ApiLogRequestService;
import tech.wetech.flexmodel.domain.model.connect.DatasourceService;
import tech.wetech.flexmodel.dsl.Predicate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static tech.wetech.flexmodel.codegen.System.apiRequestLog;

/**
 * @author cjbi
 */
@ApplicationScoped
public class OverviewApplicationService {

  @Inject
  ApiDefinitionService apiDefinitionService;

  @Inject
  ApiLogRequestService apiLogService;

  @Inject
  DatasourceService datasourceService;

  public OverviewDTO getOverview(LocalDateTime startDate, LocalDateTime endDate) {
    int queryCount = 0;
    int mutationCount = 0;
    int subscribeCount = 0;

    List<ApiDefinition> list = apiDefinitionService.findList();
    for (ApiDefinition apiDefinition : list) {
      if (apiDefinition.getMeta() instanceof Map<?, ?> metaMap) {
        if (metaMap.get("execution") instanceof Map<?, ?> executionMap) {
          if (executionMap.get("query") instanceof String gql) {
            if (gql.startsWith("query")) {
              queryCount++;
            } else if (gql.startsWith("mutation")) {
              mutationCount++;
            } else if (gql.startsWith("subscription")) {
              subscribeCount++;
            }
          }
        }
      }
    }
    OverviewDTO overviewDTO = new OverviewDTO();
    overviewDTO.setQueryCount(queryCount);
    overviewDTO.setMutationCount(mutationCount);
    overviewDTO.setSubscribeCount(subscribeCount);
    overviewDTO.setDataSourceCount(datasourceService.findAll().size());
    Predicate filter = apiRequestLog.createdAt.between(startDate, endDate);
    overviewDTO.setApiStatList(apiLogService.stat(filter));
    overviewDTO.setApiRankingList(apiLogService.ranking(filter));

    return overviewDTO;

  }

}
