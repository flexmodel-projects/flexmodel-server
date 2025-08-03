package tech.wetech.flexmodel.application;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.wetech.flexmodel.application.dto.OverviewDTO;
import tech.wetech.flexmodel.codegen.entity.ApiDefinition;
import tech.wetech.flexmodel.codegen.entity.ApiRequestLog;
import tech.wetech.flexmodel.domain.model.api.ApiDefinitionService;
import tech.wetech.flexmodel.domain.model.api.ApiLogRequestService;
import tech.wetech.flexmodel.domain.model.api.LogStat;
import tech.wetech.flexmodel.domain.model.connect.DatasourceService;
import tech.wetech.flexmodel.query.expr.Predicate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static tech.wetech.flexmodel.query.expr.Expressions.field;

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
    overviewDTO.setApiRankingList(apiLogService.ranking(field(ApiRequestLog::getCreatedAt).between(startDate, endDate)));

    String fmt;
    List<String> dateList = new ArrayList<>();
    if (ChronoUnit.DAYS.between(startDate, endDate) <= 1) {
      fmt = "yyyy-MM-dd HH:00:00";
      LocalDateTime currentTime = startDate;
      while (!currentTime.isAfter(endDate)) {
        dateList.add(currentTime.format(DateTimeFormatter.ofPattern(fmt)));
        currentTime = currentTime.plusHours(1);
      }
    } else if (ChronoUnit.DAYS.between(startDate, endDate) > 1 && ChronoUnit.DAYS.between(startDate, endDate) <= 7) {
      fmt = "yyyy-MM-dd";
      LocalDateTime currentTime = startDate;
      while (!currentTime.isAfter(endDate)) {
        dateList.add(currentTime.format(DateTimeFormatter.ofPattern(fmt)));
        currentTime = currentTime.plusDays(1);
      }

    } else if (ChronoUnit.DAYS.between(startDate, endDate) > 7 && ChronoUnit.DAYS.between(startDate, endDate) <= 31) {
      fmt = "yyyy-MM-dd";
      LocalDateTime currentTime = startDate;
      while (!currentTime.isAfter(endDate)) {
        dateList.add(currentTime.format(DateTimeFormatter.ofPattern(fmt)));
        currentTime = currentTime.plusDays(1);
      }
    } else if (ChronoUnit.DAYS.between(startDate, endDate) > 31 && ChronoUnit.DAYS.between(startDate, endDate) <= 365) {
      fmt = "yyyy-MM";
      LocalDateTime currentTime = startDate;
      while (!currentTime.isAfter(endDate)) {
        dateList.add(currentTime.format(DateTimeFormatter.ofPattern(fmt)));
        currentTime = currentTime.plusMonths(1);
      }
    } else {
      fmt = "yyyy";
      LocalDateTime currentTime = startDate;
      while (!currentTime.isAfter(endDate)) {
        dateList.add(currentTime.format(DateTimeFormatter.ofPattern(fmt)));
        currentTime = currentTime.plusYears(1);
      }
    }
    Predicate successFilter = field(ApiRequestLog::getCreatedAt).between(startDate, endDate).and(field(ApiRequestLog::getIsSuccess).eq(true));
    Predicate failFilter = field(ApiRequestLog::getCreatedAt).between(startDate, endDate).and(field(ApiRequestLog::getIsSuccess).eq(false));
    Map<String, Long> successMap = apiLogService.stat(successFilter, fmt).stream().collect(Collectors.toMap(LogStat::getDate, LogStat::getTotal));
    Map<String, Long> failMap = apiLogService.stat(failFilter, fmt).stream().collect(Collectors.toMap(LogStat::getDate, LogStat::getTotal));
    OverviewDTO.ApiStatDTO statDTO = new OverviewDTO.ApiStatDTO();
    List<Long> successData = new ArrayList<>();
    List<Long> failData = new ArrayList<>();
    for (String date : dateList) {
      successData.add(successMap.getOrDefault(date, 0L));
      failData.add(failMap.getOrDefault(date, 0L));
    }
    statDTO.setDateList(dateList);
    statDTO.setSuccessData(successData);
    statDTO.setFailData(failData);
    overviewDTO.setApiStat(statDTO);
    return overviewDTO;

  }

}
