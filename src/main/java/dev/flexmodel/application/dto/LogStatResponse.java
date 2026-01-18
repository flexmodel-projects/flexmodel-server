package dev.flexmodel.application.dto;

import lombok.*;
import dev.flexmodel.domain.model.api.LogApiRank;
import dev.flexmodel.domain.model.api.LogStat;

import java.util.ArrayList;
import java.util.List;

/**
 * @author cjbi
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LogStatResponse {
  private List<LogStat> apiStatList = new ArrayList<>();
  private List<LogApiRank> apiRankingList = new ArrayList<>();
  private ApiChart apiChart;

  @Getter
  @Setter
  public static class ApiChart {
    private List<String> dateList;
    private List<Long> successData;
    private List<Long> failData;
  }
}
