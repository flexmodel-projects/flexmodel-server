package dev.flexmodel.interfaces.rest.json.jackson.mixin;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import dev.flexmodel.domain.model.schedule.config.CronScheduledTriggerConfig;
import dev.flexmodel.domain.model.schedule.config.EventTriggerConfig;
import dev.flexmodel.domain.model.schedule.config.IntervalScheduledTriggerConfig;

/**
 * 定时触发器配置 MixIn 类
 * 用于 Jackson JSON 序列化/反序列化时的类型映射配置
 *
 * @author cjbi
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
  // Cron 表达式定时触发器
  @JsonSubTypes.Type(value = CronScheduledTriggerConfig.class, name = "cron"),
  // 间隔定时触发器
  @JsonSubTypes.Type(value = IntervalScheduledTriggerConfig.class, name = "interval"),
  // 每日时间间隔定时触发器
  @JsonSubTypes.Type(value = EventTriggerConfig.class, name = "event"),
})
public class ScheduledTriggerConfigMixIn {

}
