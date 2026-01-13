package tech.wetech.flexmodel.interfaces.rest.response;

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
  private List<String> permissions = List.of("*");

  public record UserResponse(String id, String username, String email) {
  }

}
