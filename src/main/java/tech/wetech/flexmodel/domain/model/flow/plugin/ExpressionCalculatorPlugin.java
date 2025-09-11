package tech.wetech.flexmodel.domain.model.flow.plugin;

import tech.wetech.flexmodel.domain.model.flow.shared.util.ExpressionCalculator;

public interface ExpressionCalculatorPlugin extends Plugin {
  ExpressionCalculator getExpressionCalculator();
}
