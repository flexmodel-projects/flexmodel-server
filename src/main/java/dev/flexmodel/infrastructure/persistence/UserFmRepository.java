package dev.flexmodel.infrastructure.persistence;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import dev.flexmodel.codegen.entity.User;
import dev.flexmodel.domain.model.auth.UserRepository;
import dev.flexmodel.session.Session;

import java.util.List;

import static dev.flexmodel.query.Expressions.field;

/**
 * @author cjbi
 */
@Singleton
public class UserFmRepository implements UserRepository {

  @Inject
  Session session;

  @Override
  public User findByUsername(String username) {
    return session.dsl()
      .selectFrom(User.class)
      .where(field(User::getId).eq(username)
        .or(field(User::getEmail).eq(username)))
      .executeOne();
  }

  @Override
  public User findById(String userId) {
    return session.dsl()
      .selectFrom(User.class)
      .where(field(User::getId).eq(userId))
      .executeOne();
  }

  @Override
  public List<User> findAll() {
    return session.dsl()
      .selectFrom(User.class)
      .execute();
  }

  @Override
  public User save(User user) {
    session.dsl()
      .mergeInto(User.class)
      .values(user)
      .execute();
    return user;
  }

  @Override
  public void delete(String userId) {
    session.dsl()
      .deleteFrom(User.class)
      .where(field(User::getId).eq(userId))
      .execute();
  }
}
