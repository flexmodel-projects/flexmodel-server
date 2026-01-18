package dev.flexmodel.infrastructure.llm;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageJsonCodec;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import io.quarkiverse.langchain4j.QuarkusChatMessageJsonCodecFactory;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import dev.flexmodel.codegen.entity.AiChatMemory;
import dev.flexmodel.session.Session;

import java.util.List;

/**
 * @author cjbi
 */
@Slf4j
@ApplicationScoped
public class FlexmodelChatMemoryStore implements ChatMemoryStore {

  @Inject
  Session session;

  private static final ChatMessageJsonCodec CODEC = new QuarkusChatMessageJsonCodecFactory().create();

  @Override
  public List<ChatMessage> getMessages(Object memoryId) {
    AiChatMemory aiChatMemory = session.dsl().selectFrom(AiChatMemory.class)
      .whereId(memoryId)
      .executeOne();
    if (aiChatMemory == null) {
      return List.of();
    }
    return CODEC.messagesFromJson(aiChatMemory.getMessages());
  }

  @Override
  public void updateMessages(Object memoryId, List<ChatMessage> messages) {
    AiChatMemory aiChatMemory = new AiChatMemory();
    aiChatMemory.setId(memoryId.toString());
    aiChatMemory.setMessages(CODEC.messagesToJson(messages));
    session.dsl().mergeInto(AiChatMemory.class).values(aiChatMemory).execute();
  }

  @Override
  public void deleteMessages(Object memoryId) {
    session.dsl().deleteFrom(AiChatMemory.class)
      .whereId(memoryId)
      .execute();
  }
}
