package dev.flexmodel.domain.model.api.execution;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

@ApplicationScoped
public class ExecutionStrategyFactory {

  @Inject
  Instance<ExecutionStrategy> strategies;

  public ExecutionStrategy getStrategy(String executionType) {

    // Find the matching strategy instance
    for (ExecutionStrategy strategy : strategies) {
      if (executionType.equals(strategy.getExecutionType())) {
        return strategy;
      }
    }

    throw new IllegalStateException("Execution strategy not available: " + executionType);
  }
}
