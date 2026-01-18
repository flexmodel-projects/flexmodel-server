package dev.flexmodel.domain.model.schedule.config;

import lombok.Getter;
import lombok.Setter;
import dev.flexmodel.codegen.StringUtils;
import dev.flexmodel.domain.model.schedule.TriggerException;
import dev.flexmodel.shared.utils.CollectionUtils;

import java.util.List;

/**
 * 事件触发器配置类
 * 用于配置基于数据模型变更事件的触发器
 *
 * @author cjbi
 */
@Getter
@Setter
public class EventTriggerConfig implements TriggerConfig {

  /**
   * 数据源名称
   * 指定要监听的数据源
   */
  private String datasourceName;

  /**
   * 模型名称
   * 指定要监听的模型
   */
  private String modelName;

  /**
   * 变更类型列表
   * 指定要监听的操作类型：create（创建）、update（更新）、delete（删除）
   * create | update | delete
   */
  private List<String> mutationTypes;

  /**
   * 触发时机
   * 指定在操作前还是操作后触发：before（操作前）、after（操作后）
   * before | after
   */
  private String triggerTiming = "after";

  /**
   * 获取触发器来源
   * @return 触发器来源 "event"
   */
  @Override
  public String type() {
    return "event";
  }

  @Override
  public void validate() {
    if (StringUtils.isEmpty(datasourceName) || StringUtils.isEmpty(modelName) || CollectionUtils.isEmpty(mutationTypes)) {
      throw new TriggerException("EventTriggerConfig is invalid.");
    }
  }
}
