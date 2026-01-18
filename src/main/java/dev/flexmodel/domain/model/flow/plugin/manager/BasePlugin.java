package dev.flexmodel.domain.model.flow.plugin.manager;

/**
 * 插件可选依赖的 BeanProvider，用于从 CDI 获取运行期对象。
 */
public class BasePlugin {
  protected CdiBeanProvider beanProvider;

  public CdiBeanProvider getBeanProvider() {
    return beanProvider;
  }

  public void setBeanProvider(CdiBeanProvider beanProvider) {
    this.beanProvider = beanProvider;
  }
}
