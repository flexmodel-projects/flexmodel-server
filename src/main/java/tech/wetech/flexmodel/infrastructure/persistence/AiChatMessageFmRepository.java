package tech.wetech.flexmodel.infrastructure.persistence;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.wetech.flexmodel.codegen.entity.AiChatMessage;
import tech.wetech.flexmodel.domain.model.ai.AiChatMessageRepository;
import tech.wetech.flexmodel.query.Direction;
import tech.wetech.flexmodel.query.Predicate;
import tech.wetech.flexmodel.session.Session;

import java.util.List;

import static tech.wetech.flexmodel.query.Expressions.field;

/**
 * @author cjbi
 */
@ApplicationScoped
public class AiChatMessageFmRepository implements AiChatMessageRepository {

  @Inject
  Session session;

  @Override
  public List<AiChatMessage> findAll() {
    return session.dsl()
      .selectFrom(AiChatMessage.class)
      .execute();
  }

  @Override
  public List<AiChatMessage> find(Predicate filter) {
    return session.dsl()
      .selectFrom(AiChatMessage.class)
      .where(filter)
      .execute();
  }

  @Override
  public List<AiChatMessage> findByConversationId(String conversationId) {
    return session.dsl()
      .selectFrom(AiChatMessage.class)
      .where(field(AiChatMessage::getConversationId).eq(conversationId))
      .orderBy("created_at", Direction.ASC)
      .execute();
  }

  @Override
  public AiChatMessage findById(String id) {
    return session.dsl()
      .selectFrom(AiChatMessage.class)
      .where(field(AiChatMessage::getId).eq(id))
      .executeOne();
  }

  @Override
  public AiChatMessage save(AiChatMessage message) {
    session.dsl()
      .mergeInto(AiChatMessage.class)
      .values(message)
      .execute();

    return message;
  }

  @Override
  public void delete(String id) {
    session.dsl().deleteFrom(AiChatMessage.class)
      .where(field(AiChatMessage::getId).eq(id))
      .execute();
  }

  @Override
  public void deleteByConversationId(String conversationId) {
    session.dsl().deleteFrom(AiChatMessage.class)
      .where(field(AiChatMessage::getConversationId).eq(conversationId))
      .execute();
  }
}
