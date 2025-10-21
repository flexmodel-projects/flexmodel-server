package tech.wetech.flexmodel.interfaces.rest.json.jackson.mixin;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import tech.wetech.flexmodel.domain.model.idp.provider.GroovyProvider;
import tech.wetech.flexmodel.domain.model.idp.provider.JsProvider;
import tech.wetech.flexmodel.domain.model.idp.provider.OIDCProvider;

/**
 * @author cjbi
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
  @JsonSubTypes.Type(value = OIDCProvider.class, name = "oidc"),
  @JsonSubTypes.Type(value = JsProvider.class, name = "js"),
  @JsonSubTypes.Type(value = GroovyProvider.class, name = "groovy"),
})
public class IdentityProviderProviderMixIn {
}
