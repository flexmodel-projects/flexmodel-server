package tech.wetech.flexmodel.domain.model.idp.provider;

/**
 * @author cjbi
 */
public interface Provider {

  String getType();

  ValidateResult validate(ValidateParam param);
}
