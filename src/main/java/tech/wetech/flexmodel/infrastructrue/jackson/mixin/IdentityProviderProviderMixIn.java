package tech.wetech.flexmodel.infrastructrue.jackson.mixin;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import tech.wetech.flexmodel.domain.model.idp.IdentityProvider;

/**
 * @author cjbi
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
  @JsonSubTypes.Type(value = IdentityProvider.OIDC.class, name = "oidc"),
})
public class IdentityProviderProviderMixIn {
}
