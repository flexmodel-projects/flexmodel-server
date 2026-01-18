package dev.flexmodel.application.dto;

import lombok.Getter;
import dev.flexmodel.codegen.entity.User;

/**
 * @author cjbi
 */
@Getter
public class MemberResponse {

  private String id;
  private String name;
  private String email;
  private String createdBy;
  private String updatedBy;
  private String createdAt;
  private String updatedAt;

  public static MemberResponse fromUser(User user) {
    MemberResponse response = new MemberResponse();
    response.setId(user.getId());
    response.setName(user.getName());
    response.setEmail(user.getEmail());
    response.setCreatedBy(user.getCreatedBy());
    response.setUpdatedBy(user.getUpdatedBy());
    response.setCreatedAt(user.getCreatedAt() != null ? user.getCreatedAt().toString() : null);
    response.setUpdatedAt(user.getUpdatedAt() != null ? user.getUpdatedAt().toString() : null);
    return response;
  }

  public void setId(String id) {
    this.id = id;
  }

  public void setName(String username) {
    this.name = username;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
  }

  public void setUpdatedBy(String updatedBy) {
    this.updatedBy = updatedBy;
  }

  public void setCreatedAt(String createdAt) {
    this.createdAt = createdAt;
  }

  public void setUpdatedAt(String updatedAt) {
    this.updatedAt = updatedAt;
  }
}
