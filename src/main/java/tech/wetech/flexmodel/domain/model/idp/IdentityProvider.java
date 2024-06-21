package tech.wetech.flexmodel.domain.model.idp;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * @author cjbi
 */
@Getter
@Setter
public class IdentityProvider {

  String name;
  Provider provider;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  public interface Provider {

    String getType();

  }

  @Getter
  @Setter
  public static class OIDC implements Provider {

    private String issuer;
    private String clientId;
    private String clientSecret;

    @Override
    public String getType() {
      return "oidc";
    }

  }

}
