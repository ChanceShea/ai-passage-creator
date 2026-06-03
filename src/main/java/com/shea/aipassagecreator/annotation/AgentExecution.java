package com.shea.aipassagecreator.annotation;

import java.lang.annotation.*;

/**
 * 智能体执行注解，标记智能体方法，自动记录执行日志和性能数据
 * @author : Shea.
 * @since : 2026/6/2 21:28
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AgentExecution {

    /**
     * 智能体名称
     */
    String value();

    /**
     * 智能体描述
     */
    String description() default "";
}
