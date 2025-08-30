package tech.wetech.flexmodel.interfaces.rest.request;

import tech.wetech.flexmodel.interfaces.rest.response.ChatMessage;

import java.util.List;

public record ChatRequest(
  String conversationId,
  String model,
  List<ChatMessage> messages,
  Double temperature,
  Integer maxTokens
) {
}
