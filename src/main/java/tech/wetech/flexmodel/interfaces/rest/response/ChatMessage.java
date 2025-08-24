package tech.wetech.flexmodel.interfaces.rest.response;

public record ChatMessage(
    String role,
    String content
) {
}
