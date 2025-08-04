package tech.wetech.flexmodel.infrastructrue.persistence;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import tech.wetech.flexmodel.codegen.entity.User;
import tech.wetech.flexmodel.domain.model.auth.UserRepository;
import tech.wetech.flexmodel.session.Session;

import static tech.wetech.flexmodel.query.expr.Expressions.field;

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
      .where(field(User::getUsername).eq(username))
      .executeOne();
  }

  @Override
  public User findById(String userId) {
    return session.dsl()
      .selectFrom(User.class)
      .where(field(User::getId).eq(userId))
      .executeOne();
  }
}
