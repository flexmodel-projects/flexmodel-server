package tech.wetech.flexmodel.interfaces.rest.response;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record ChatDelta(
    String role,
    String content
) {
}
