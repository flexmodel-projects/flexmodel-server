package dev.flexmodel.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 存储验证结果
 * @author cjbi
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidateStorageResult {

  private boolean success;

  private String errorMsg;

  private Long time;
}
