package tech.wetech.flexmodel.infrastructrue.persistence;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import tech.wetech.flexmodel.codegen.dao.UserDAO;
import tech.wetech.flexmodel.codegen.entity.User;
import tech.wetech.flexmodel.domain.model.auth.UserRepository;

import static tech.wetech.flexmodel.codegen.System.user;

/**
 * @author cjbi
 */
@Singleton
public class UserFmRepository implements UserRepository {

  @Inject
  UserDAO userDAO;

  @Override
  public User findByUsername(String username) {
    return userDAO.find(user.username.eq(username)).stream().findFirst().orElse(null);
  }

  @Override
  public User findById(String userId) {
    return userDAO.findById(userId);
  }
}
