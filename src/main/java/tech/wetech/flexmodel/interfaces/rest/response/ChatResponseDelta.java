package tech.wetech.flexmodel.interfaces.rest.response;

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.List;

@RegisterForReflection
public record ChatResponseDelta(
    String object,
    String id,
    String created,
    String model,
    List<ChatChoiceDelta> choices,
    ChatUsage usage
) {
}
