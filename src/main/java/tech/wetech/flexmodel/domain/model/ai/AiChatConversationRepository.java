package tech.wetech.flexmodel.domain.model.ai;

import tech.wetech.flexmodel.codegen.entity.AiChatConversation;
import tech.wetech.flexmodel.query.Predicate;

import java.util.List;

/**
 * @author cjbi
 */
public interface AiChatConversationRepository {

  List<AiChatConversation> findAll();

  List<AiChatConversation> find(Predicate filter);

  AiChatConversation findById(String id);

  AiChatConversation save(AiChatConversation conversation);

  void delete(String id);
}
