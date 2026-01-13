package tech.wetech.flexmodel.infrastructure.persistence;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import tech.wetech.flexmodel.codegen.entity.User;
import tech.wetech.flexmodel.domain.model.auth.UserRepository;
import tech.wetech.flexmodel.session.Session;

import java.util.List;

import static tech.wetech.flexmodel.query.Expressions.field;

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
