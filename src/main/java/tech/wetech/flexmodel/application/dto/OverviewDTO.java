package tech.wetech.flexmodel.application.dto;

import lombok.Getter;
import lombok.Setter;
import tech.wetech.flexmodel.domain.model.api.LogApiRank;
import tech.wetech.flexmodel.domain.model.api.LogStat;

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

  private List<LogStat> apiStatList = new ArrayList<>();
  private List<LogApiRank> apiRankingList = new ArrayList<>();

}
