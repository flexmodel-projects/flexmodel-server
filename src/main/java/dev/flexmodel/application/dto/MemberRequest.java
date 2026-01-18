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
public class MemberRequest {

  private String id;
  private String name;
  private String email;
  private String password;
  private String createdBy;
  private String updatedBy;
}
