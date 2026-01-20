package dev.flexmodel.interfaces.rest.response;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * @author cjbi
 */
@Getter
@Setter
@ToString
public class UserinfoResponse {
  private String token;
  private Long expiresIn;
  private UserResponse user;
  private List<String> permissions;

  public record UserResponse(String id, String name, String email) {
  }

}
