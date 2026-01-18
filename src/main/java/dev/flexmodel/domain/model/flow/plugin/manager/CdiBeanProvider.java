package dev.flexmodel.domain.model.flow.plugin.manager;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.CDI;

import java.util.ArrayList;
import java.util.List;

/**
 * 通过 CDI 提供 Bean 查询能力，替代 Spring BeanFactory。
 */
@ApplicationScoped
public class CdiBeanProvider {

  public <T> List<T> getBeansForType(Class<T> type) {
    Instance<T> instance = CDI.current().select(type);
    List<T> result = new ArrayList<>();
    for (T t : instance) {
      result.add(t);
    }
    return result;
  }

  @SuppressWarnings("unchecked")
  public <T> T getBeanByClassName(String className) {
    try {
      Class<?> clazz = Class.forName(className);
      return (T) CDI.current().select(clazz).get();
    } catch (Exception e) {
      return null;
    }
  }
}


