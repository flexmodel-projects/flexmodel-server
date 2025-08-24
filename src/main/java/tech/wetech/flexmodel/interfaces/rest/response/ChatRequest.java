package tech.wetech.flexmodel.interfaces.rest.response;

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.List;

@RegisterForReflection
public record ChatRequest(
  String model,
  List<ChatMessage> messages,
  Double temperature,
  Integer maxTokens
) {
}
