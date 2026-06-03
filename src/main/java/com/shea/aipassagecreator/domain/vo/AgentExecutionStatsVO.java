package com.shea.aipassagecreator.domain.vo;

import com.shea.aipassagecreator.domain.entity.AgentLog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 智能体执行统计信息视图对象
 * @author : Shea.
 * @since : 2026/6/2 21:25
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentExecutionStatsVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 任务ID
     */
    private String taskId;

    /**
     * 总执行时长（毫秒）
     */
    private Integer totalDurationMs;

    /**
     * 智能体数量
     */
    private Integer agentCount;

    /**
     * 智能体执行时长（毫秒）
     */
    private Map<String,Integer> agentDurations;

    /**
     * 总体状态:SUCCESS、FAILED、RUNNING
     */
    private String overallStatus;

    /**
     * 详细日志列表
     */
    private List<AgentLog> logs;
}
