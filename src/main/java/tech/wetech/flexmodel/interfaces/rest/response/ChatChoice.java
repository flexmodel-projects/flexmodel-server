package tech.wetech.flexmodel.interfaces.rest.response;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record ChatChoice(
    Integer index,
    ChatMessage message,
    String finishReason
) {
}
