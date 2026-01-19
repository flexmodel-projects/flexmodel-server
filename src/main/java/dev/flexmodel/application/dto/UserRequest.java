package dev.flexmodel.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author cjbi
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserRequest {

  private String id;
  private String name;
  private String email;
  private String password;
  private List<String> roleIds;
  private String createdBy;
  private String updatedBy;
}
