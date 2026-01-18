package dev.flexmodel.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author cjbi
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProjectListRequest {
  /**
   * stat: 统计字段
   */
  private String incldue;
}
