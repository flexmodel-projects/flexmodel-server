package tech.wetech.flexmodel.domain.model.ai;

import tech.wetech.flexmodel.codegen.entity.AiChatMessage;
import tech.wetech.flexmodel.query.Predicate;

import java.util.List;

/**
 * @author cjbi
 */
public interface AiChatMessageRepository {

  List<AiChatMessage> findAll();

  List<AiChatMessage> find(Predicate filter);

  List<AiChatMessage> findByConversationId(String conversationId);

  AiChatMessage findById(String id);

  AiChatMessage save(AiChatMessage message);

  void delete(String id);

  void deleteByConversationId(String conversationId);
}
