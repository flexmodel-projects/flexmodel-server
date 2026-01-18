package dev.flexmodel.domain.model.flow.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import dev.flexmodel.domain.model.flow.plugin.ExpressionCalculatorPlugin;
import dev.flexmodel.domain.model.flow.plugin.IdGeneratorPlugin;
import dev.flexmodel.domain.model.flow.plugin.manager.PluginManager;
import dev.flexmodel.domain.model.flow.shared.util.ExpressionCalculator;
import dev.flexmodel.domain.model.flow.shared.util.IdGenerator;
import dev.flexmodel.domain.model.flow.shared.util.StrongUuidGenerator;
import dev.flexmodel.domain.model.flow.shared.util.impl.GroovyExpressionCalculator;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class PluginConfig {
  private static final Logger LOGGER = LoggerFactory.getLogger(PluginConfig.class);

  @ConfigProperty(name = "turbo.plugin.manager.custom-class")
  Optional<String> customManagerClass;

  /**
   * 若指定了自定义的PluginManager，则使用指定的，否则使用默认的DefaultPluginManager
   */
//  @Produces
//  @ApplicationScoped
//  public PluginManager pluginManager() {
//    if (customManagerClass.isEmpty() || customManagerClass.get().isBlank()) {
//      LOGGER.info("No custom PluginManager specified, using default PluginManager.");
//      DefaultPluginManager pluginManager = new DefaultPluginManager(new CdiBeanProvider());
//      return pluginManager;
//    } else {
//      try {
//        Class<?> clazz = Class.forName(customManagerClass.get());
//        return (PluginManager) clazz.getDeclaredConstructor().newInstance();
//      } catch (Exception e) {
//        throw new RuntimeException("Failed to instantiate custom PluginManager", e);
//      }
//    }
//  }

  /**
   * 优先从插件中获取表达式计算器，如有多个仅使用第一个，若未找到则使用默认实现
   */
  @Produces
  @ApplicationScoped
  public ExpressionCalculator getExpressionCalculator(PluginManager pluginManager) {
    List<ExpressionCalculatorPlugin> expressionCalculatorPlugins = pluginManager.getPluginsFor(ExpressionCalculatorPlugin.class);
    if (!expressionCalculatorPlugins.isEmpty()) {
      LOGGER.info("Found expression calculator plugin: {}", expressionCalculatorPlugins.get(0).getName());
      return expressionCalculatorPlugins.get(0).getExpressionCalculator();
    }
    return new GroovyExpressionCalculator();
  }

  /**
   * 优先从插件中获取id生成器，如有多个仅使用第一个，若未找到则使用默认实现
   */
  @Produces
  public IdGenerator getIdGenerator(PluginManager pluginManager) {
    List<IdGeneratorPlugin> expressionCalculatorPlugins = pluginManager.getPluginsFor(IdGeneratorPlugin.class);
    if (!expressionCalculatorPlugins.isEmpty()) {
      LOGGER.info("Found id generator plugin: {}", expressionCalculatorPlugins.get(0).getName());
      return expressionCalculatorPlugins.get(0).getIdGenerator();
    }
    return new StrongUuidGenerator();
  }
}
