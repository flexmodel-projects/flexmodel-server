package tech.wetech.flexmodel.interfaces.rest.response;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record ChatUsage(
    Integer promptTokens,
    Integer completionTokens,
    Integer totalTokens
) {
}
