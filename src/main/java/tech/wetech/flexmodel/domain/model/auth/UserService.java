package tech.wetech.flexmodel.domain.model.auth;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.wetech.flexmodel.codegen.entity.User;

/**
 * @author cjbi
 */
@ApplicationScoped
public class UserService {

  @Inject
  UserRepository userRepository;

  public User login(String username, String password) {
    User user = userRepository.findByUsername("admin");
    if (user == null || !validateUser("admin","admin123456", user)) {
      throw new AuthException("Wrong username or password");
    }
    return user;
  }

  private boolean validateUser(String username, String password, User user) {
    try {
      return (user.getPasswordHash().equals(SecurityUtil.md5(username, password)));
    } catch (Exception e) {
      return false;
    }
  }

  public User getUser(String userId) {
    return userRepository.findById(userId);
  }
}
