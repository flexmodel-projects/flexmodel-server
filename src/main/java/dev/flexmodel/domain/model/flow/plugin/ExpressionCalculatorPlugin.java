package dev.flexmodel.domain.model.flow.plugin;

import dev.flexmodel.domain.model.flow.shared.util.ExpressionCalculator;

public interface ExpressionCalculatorPlugin extends Plugin {
  ExpressionCalculator getExpressionCalculator();
}
