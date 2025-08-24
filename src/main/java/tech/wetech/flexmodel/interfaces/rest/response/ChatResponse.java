package tech.wetech.flexmodel.interfaces.rest.response;

import java.util.List;

public record ChatResponse(
    String object,
    String id,
    String created,
    String model,
    List<ChatChoice> choices,
    ChatUsage usage
) {
}
