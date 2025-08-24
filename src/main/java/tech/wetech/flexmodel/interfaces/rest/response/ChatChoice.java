package tech.wetech.flexmodel.interfaces.rest.response;

public record ChatChoice(
    Integer index,
    ChatMessage message,
    String finishReason
) {
}
