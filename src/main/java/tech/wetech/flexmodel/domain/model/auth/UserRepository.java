package tech.wetech.flexmodel.domain.model.auth;

import tech.wetech.flexmodel.codegen.entity.User;

/**
 * @author cjbi
 */
public interface UserRepository {

  User findByUsername(String username);


  User findById(String userId);
}
