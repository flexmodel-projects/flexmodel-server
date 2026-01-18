package dev.flexmodel.interfaces.rest.response;

public record ChatChoiceDelta(
    Integer index,
    ChatDelta delta,
    String finishReason
) {
}
