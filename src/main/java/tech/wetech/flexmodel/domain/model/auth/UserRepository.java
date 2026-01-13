package tech.wetech.flexmodel.domain.model.auth;

import tech.wetech.flexmodel.codegen.entity.User;

import java.util.List;

/**
 * @author cjbi
 */
public interface UserRepository {

  User findByUsername(String username);

  User findById(String userId);

  List<User> findAll();

  User save(User user);

  void delete(String userId);
}
