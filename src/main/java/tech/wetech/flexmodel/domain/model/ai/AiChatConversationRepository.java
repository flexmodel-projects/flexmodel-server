package tech.wetech.flexmodel.domain.model.ai;

import tech.wetech.flexmodel.codegen.entity.AiChatConversation;
import tech.wetech.flexmodel.query.Predicate;

import java.util.List;

/**
 * @author cjbi
 */
public interface AiChatConversationRepository {

  List<AiChatConversation> findAll(String projectId);

  List<AiChatConversation> find(String projectId, Predicate filter);

  AiChatConversation findById(String projectId, String id);

  AiChatConversation save(String projectId, AiChatConversation conversation);

  void delete(String projectId, String id);
}
