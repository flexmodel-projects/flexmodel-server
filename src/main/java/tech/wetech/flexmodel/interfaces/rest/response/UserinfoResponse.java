package tech.wetech.flexmodel.interfaces.rest.response;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import tech.wetech.flexmodel.domain.model.settings.Settings;

import java.util.List;

/**
 * @author cjbi
 */
@Getter
@Setter
@ToString
public class UserinfoResponse {
  private String token;
  private UserResponse user;
  private Settings settings;
  private List<String> permissions = List.of("*");

  public record UserResponse(String id, String username, String avatar) {
  }

}
