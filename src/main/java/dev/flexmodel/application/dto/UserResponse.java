package dev.flexmodel.application.dto;

import lombok.Getter;
import dev.flexmodel.codegen.entity.User;
import dev.flexmodel.codegen.entity.Role;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author cjbi
 */
@Setter
@Getter
public class UserResponse {

  private String id;
  private String name;
  private String email;
  private String createdBy;
  private String updatedBy;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private List<UserRole> roles;

  public static UserResponse fromUser(User user) {
    return fromUser(user, null);
  }

  public static UserResponse fromUser(User user, List<Role> allRoles) {
    UserResponse response = new UserResponse();
    response.setId(user.getId());
    response.setName(user.getName());
    response.setEmail(user.getEmail());
    response.setCreatedBy(user.getCreatedBy());
    response.setUpdatedBy(user.getUpdatedBy());
    response.setCreatedAt(user.getCreatedAt());
    response.setUpdatedAt(user.getUpdatedAt());

    if (user.getRoleIds() != null && !user.getRoleIds().isEmpty() && allRoles != null) {
      List<String> roleIdList = Arrays.asList(user.getRoleIds().split(","));
      List<UserRole> userRoles = allRoles.stream()
        .filter(role -> roleIdList.contains(role.getId()))
        .map(role -> {
          UserRole userRole = new UserRole();
          userRole.setId(role.getId());
          userRole.setName(role.getName());
          return userRole;
        })
        .collect(Collectors.toList());
      response.setRoles(userRoles);
    }

    return response;
  }

  @Getter
  @Setter
  public static class UserRole {
    private String id;
    private String name;
  }

}
