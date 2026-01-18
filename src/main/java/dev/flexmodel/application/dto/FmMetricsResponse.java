package dev.flexmodel.application.dto;

import lombok.*;

/**
 * @author cjbi
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FmMetricsResponse {

  /**
   * 自定义接口数量
   */
  private int customApiCount;
  /**
   * 请求数量
   */
  private int requestCount;
  /**
   * 数据源数量
   */
  private int dataSourceCount;
  /**
   * 模型数量
   */
  private int modelCount;
  /**
   * 流程数量
   */
  private int flowDefCount;
  /**
   * 流程成功数量
   */
  private int flowExecCount;
  /**
   * 触发器数量
   */
  private int triggerTotalCount;
  /**
   * 触发器成功数量
   */
  private int jobSuccessCount;
  /**
   * 触发器失败数量
   */
  private int jobFailureCount;


}
