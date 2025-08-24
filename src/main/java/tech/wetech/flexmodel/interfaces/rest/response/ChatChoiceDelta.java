package tech.wetech.flexmodel.interfaces.rest.response;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record ChatChoiceDelta(
    Integer index,
    ChatDelta delta,
    String finishReason
) {
}
