package com.shea.aipassagecreator.service;


import com.mybatisflex.core.service.IService;
import com.shea.aipassagecreator.domain.entity.AgentLog;
import com.shea.aipassagecreator.domain.vo.AgentExecutionStatsVO;

import java.util.List;

/**
 * <p>
 * 智能体执行日志表 服务类
 * </p>
 *
 * @author Shea
 * @since 2026-06-02
 */
public interface IAgentLogService extends IService<AgentLog> {

    /**
     * 异步保存日志
     * @param agentLog 日志对象
     */
    void saveLogAsync(AgentLog agentLog);

    /**
     * 根据任务ID获取日志列表
     * @param taskId 任务ID
     * @return 日志列表
     */
    List<AgentLog> getLogsByTaskId(String taskId);

    /**
     * 根据任务ID获取执行统计信息
     * @param taskId 任务ID
     * @return 执行统计信息
     */
    AgentExecutionStatsVO getAgentExecutionStats(String taskId);
}
