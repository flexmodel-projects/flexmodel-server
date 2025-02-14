package tech.wetech.flexmodel.application;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.wetech.flexmodel.application.dto.OverviewDTO;
import tech.wetech.flexmodel.codegen.entity.ApiInfo;
import tech.wetech.flexmodel.domain.model.api.ApiInfoService;
import tech.wetech.flexmodel.domain.model.api.ApiLogService;
import tech.wetech.flexmodel.domain.model.connect.DatasourceService;
import tech.wetech.flexmodel.dsl.Predicate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static tech.wetech.flexmodel.codegen.System.apiLog;

/**
 * @author cjbi
 */
@ApplicationScoped
public class OverviewApplicationService {

  @Inject
  ApiInfoService apiInfoService;

  @Inject
  ApiLogService apiLogService;

  @Inject
  DatasourceService datasourceService;

  public OverviewDTO getOverview(LocalDateTime startDate, LocalDateTime endDate) {
    int queryCount = 0;
    int mutationCount = 0;
    int subscribeCount = 0;

    List<ApiInfo> list = apiInfoService.findList();
    for (ApiInfo apiInfo : list) {
      if (apiInfo.getMeta() instanceof Map<?, ?> metaMap) {
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
    Predicate filter = apiLog.createdAt.between(startDate, endDate);
    overviewDTO.setApiStatList(apiLogService.stat(filter));
    overviewDTO.setApiRankingList(apiLogService.ranking(filter));

    return overviewDTO;

  }

}
