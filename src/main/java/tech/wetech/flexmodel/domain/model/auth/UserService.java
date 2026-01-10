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

  public User login(String projectId, String username, String password) {
    User user = userRepository.findByUsername(username);
    if (user == null || !validateUser(username, password, user)) {
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

  public User getUser(String projectId, String userId) {
    return userRepository.findById(userId);
  }
}
