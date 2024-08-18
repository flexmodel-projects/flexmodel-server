package tech.wetech.flexmodel.domain.model.idp.provider;

/**
 * @author cjbi
 */
public interface Provider {

  String getType();

  boolean checkToken(String token);

}
