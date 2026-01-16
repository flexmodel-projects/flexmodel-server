package tech.wetech.flexmodel.application;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.wetech.flexmodel.application.dto.MemberRequest;
import tech.wetech.flexmodel.application.dto.MemberResponse;
import tech.wetech.flexmodel.codegen.entity.User;
import tech.wetech.flexmodel.domain.model.auth.SecurityUtil;
import tech.wetech.flexmodel.domain.model.auth.UserRepository;

import java.util.List;

/**
 * @author cjbi
 */
@ApplicationScoped
public class MemberApplicationService {

  @Inject
  UserRepository userRepository;

  public List<MemberResponse> findAll() {
    return userRepository.findAll().stream()
      .map(MemberResponse::fromUser)
      .toList();
  }

  public MemberResponse findById(String userId) {
    User user = userRepository.findById(userId);
    return user != null ? MemberResponse.fromUser(user) : null;
  }

  public MemberResponse createMember(MemberRequest request) {
    User user = new User();
    user.setId(request.getId());
    user.setName(request.getName());
    user.setEmail(request.getEmail());
    user.setCreatedBy(request.getCreatedBy());
    user.setUpdatedBy(request.getUpdatedBy());

    if (request.getPassword() != null && !request.getPassword().isEmpty()) {
      try {
        user.setPasswordHash(SecurityUtil.md5(request.getId(), request.getPassword()));
      } catch (Exception e) {
        throw new RuntimeException("Failed to hash password", e);
      }
    }

    User savedUser = userRepository.save(user);
    return MemberResponse.fromUser(savedUser);
  }

  public MemberResponse updateMember(MemberRequest request) {
    User existingUser = userRepository.findById(request.getId());
    if (existingUser == null) {
      throw new RuntimeException("User not found");
    }

    User user = new User();
    user.setId(request.getId());
    user.setName(request.getName());
    user.setEmail(request.getEmail());
    user.setCreatedBy(existingUser.getCreatedBy());
    user.setUpdatedBy(request.getUpdatedBy());
    user.setCreatedAt(existingUser.getCreatedAt());

    if (request.getPassword() != null && !request.getPassword().isEmpty()) {
      try {
        user.setPasswordHash(SecurityUtil.md5(request.getId(), request.getPassword()));
      } catch (Exception e) {
        throw new RuntimeException("Failed to hash password", e);
      }
    } else {
      user.setPasswordHash(existingUser.getPasswordHash());
    }

    User savedUser = userRepository.save(user);
    return MemberResponse.fromUser(savedUser);
  }

  public void deleteMember(String userId) {
    userRepository.delete(userId);
  }
}
