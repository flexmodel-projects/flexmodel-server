package dev.flexmodel.domain.model.schedule.config;

import lombok.Getter;
import lombok.Setter;
import dev.flexmodel.domain.model.schedule.TriggerException;

/**
 * Cron表达式定时触发器配置类
 * 基于Cron表达式的定时触发器配置
 *
 * @author cjbi
 */
@Getter
@Setter
public class CronScheduledTriggerConfig extends ScheduledTriggerConfig {

  /**
   * Cron表达式
   * 用于定义定时执行的时间规则，格式为：秒 分 时 日 月 周
   * 例如：0 0 12 * * ? 表示每天中午12点执行
   */
  private String cronExpression;

  /**
   * 获取触发器来源
   * @return 触发器来源 "cron"
   */
  @Override
  public String type() {
    return "cron";
  }

  @Override
  public void validate() {
    if (cronExpression == null || cronExpression.isEmpty()) {
      throw new TriggerException("Cron表达式不能为空");
    }
  }
}
