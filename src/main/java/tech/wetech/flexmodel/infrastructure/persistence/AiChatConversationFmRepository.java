package tech.wetech.flexmodel.infrastructure.persistence;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.wetech.flexmodel.codegen.entity.AiChatConversation;
import tech.wetech.flexmodel.domain.model.ai.AiChatConversationRepository;
import tech.wetech.flexmodel.query.Predicate;
import tech.wetech.flexmodel.session.Session;

import java.util.List;

import static tech.wetech.flexmodel.query.Expressions.field;

/**
 * @author cjbi
 */
@ApplicationScoped
public class AiChatConversationFmRepository implements AiChatConversationRepository {

  @Inject
  Session session;

  @Override
  public List<AiChatConversation> findAll() {
    return session.dsl()
      .selectFrom(AiChatConversation.class)
      .execute();
  }

  @Override
  public List<AiChatConversation> find(Predicate filter) {
    return session.dsl()
      .selectFrom(AiChatConversation.class)
      .where(filter)
      .execute();
  }

  @Override
  public AiChatConversation findById(String id) {
    return session.dsl()
      .selectFrom(AiChatConversation.class)
      .where(field(AiChatConversation::getId).eq(id))
      .executeOne();
  }

  @Override
  public AiChatConversation save(AiChatConversation conversation) {
    session.dsl()
      .mergeInto(AiChatConversation.class)
      .values(conversation)
      .execute();

    return conversation;
  }

  @Override
  public void delete(String id) {
    session.dsl().deleteFrom(AiChatConversation.class)
      .where(field(AiChatConversation::getId).eq(id))
      .execute();
  }
}
