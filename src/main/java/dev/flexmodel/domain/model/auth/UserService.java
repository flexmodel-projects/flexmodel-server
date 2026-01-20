package dev.flexmodel.domain.model.auth;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import dev.flexmodel.codegen.entity.User;

import java.util.List;

/**
 * @author cjbi
 */
@ApplicationScoped
public class UserService {

  @Inject
  UserRepository userRepository;

  public User findByUsername(String username) {
    return userRepository.findByUsername(username);
  }

  public User findById(String userId) {
    return userRepository.findById(userId);
  }

  public List<User> findAll() {
    return userRepository.findAll();
  }

  public User save(User user) {
    return userRepository.save(user);
  }

  public void delete(String userId) {
    userRepository.delete(userId);
  }

  public User login(String username, String password) {
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

  public User getUser(String userId) {
    return userRepository.findById(userId);
  }
}
