package dev.flexmodel.domain.model.ai;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import dev.flexmodel.codegen.entity.AiChatConversation;
import dev.flexmodel.codegen.entity.AiChatMessage;
import dev.flexmodel.shared.SessionContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * @author cjbi
 */
@ApplicationScoped
public class AiChatService {

  @Inject
  AiChatConversationRepository conversationRepository;

  @Inject
  AiChatMessageRepository messageRepository;

  /**
   * 创建新的聊天会话
   */
  public AiChatConversation createConversation(String title) {
    String projectId = SessionContextHolder.getProjectId();
    AiChatConversation conversation = new AiChatConversation();
    conversation.setId(UUID.randomUUID().toString());
    conversation.setProjectId(projectId);
    conversation.setTitle(title);
    conversation.setCreatedAt(LocalDateTime.now());

    return conversationRepository.save(projectId, conversation);
  }

  /**
   * 获取所有会话
   */
  public List<AiChatConversation> getAllConversations() {
    String projectId = SessionContextHolder.getProjectId();
    return conversationRepository.findAll(projectId);
  }

  /**
   * 根据ID获取会话
   */
  public AiChatConversation getConversationById(String conversationId) {
    String projectId = SessionContextHolder.getProjectId();
    return conversationRepository.findById(projectId, conversationId);
  }

  /**
   * 删除会话
   */
  public void deleteConversation(String conversationId) {
    String projectId = SessionContextHolder.getProjectId();
    messageRepository.deleteByConversationId(projectId, conversationId);
    conversationRepository.delete(projectId, conversationId);
  }

  /**
   * 发送消息
   */
  public AiChatMessage sendMessage(String conversationId, String role, String content) {
    String projectId = SessionContextHolder.getProjectId();
    AiChatMessage message = new AiChatMessage();
    message.setId(UUID.randomUUID().toString());
    message.setConversationId(conversationId);
    message.setRole(role);
    message.setContent(content);
    message.setCreatedAt(LocalDateTime.now());

    return messageRepository.save(projectId, message);
  }

  /**
   * 获取会话的所有消息
   */
  public List<AiChatMessage> getMessagesByConversationId(String conversationId) {
    String projectId = SessionContextHolder.getProjectId();
    return messageRepository.findByConversationId(projectId, conversationId);
  }

  public AiChatMessage saveMessage(AiChatMessage message) {
    String projectId = message.getProjectId();
    return messageRepository.save(projectId, message);
  }
}
