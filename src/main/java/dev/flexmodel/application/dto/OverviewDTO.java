package dev.flexmodel.application.dto;

import lombok.Getter;
import lombok.Setter;
import dev.flexmodel.domain.model.api.LogApiRank;

import java.util.ArrayList;
import java.util.List;

/**
 * @author cjbi
 */
@Getter
@Setter
public class OverviewDTO {

  private int queryCount = 0;
  private int mutationCount = 0;
  private int subscribeCount = 0;
  private int dataSourceCount = 0;

  private List<LogApiRank> apiRankingList = new ArrayList<>();

  private ApiStatDTO apiStat;

  @Getter
  @Setter
  public static class ApiStatDTO {
    private List<String> dateList;
    private List<Long> successData;
    private List<Long> failData;
  }

}
