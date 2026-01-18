package dev.flexmodel.interfaces.rest.response;

public record ChatUsage(
    Integer promptTokens,
    Integer completionTokens,
    Integer totalTokens
) {
}
