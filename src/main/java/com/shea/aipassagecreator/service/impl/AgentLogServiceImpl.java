package com.shea.aipassagecreator.service.impl;


import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.shea.aipassagecreator.domain.entity.AgentLog;
import com.shea.aipassagecreator.domain.vo.AgentExecutionStatsVO;
import com.shea.aipassagecreator.mapper.AgentLogMapper;
import com.shea.aipassagecreator.service.IAgentLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 智能体执行日志表 服务实现类
 * </p>
 *
 * @author Shea
 * @since 2026-06-02
 */
@Service
@Slf4j
public class AgentLogServiceImpl extends ServiceImpl<AgentLogMapper, AgentLog> implements IAgentLogService {

    @Override
    @Async
    public void saveLogAsync(AgentLog agentLog) {
        try {
            this.save(agentLog);
            log.info("智能体日志已保存，taskId={}，agentName={}，status={}，durationMs={}",
                    agentLog.getTaskId(), agentLog.getAgentName(), agentLog.getStatus(), agentLog.getDurationMs());
        } catch (Exception e) {
            log.error("保存智能体日志时出错，taskId={}，agentName={}",
                    agentLog.getTaskId(), agentLog.getAgentName(), e);
        }
    }

    @Override
    public List<AgentLog> getLogsByTaskId(String taskId) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("taskId", taskId)
                .orderBy("createTime",true);
        return this.list(queryWrapper);
    }

    @Override
    public AgentExecutionStatsVO getAgentExecutionStats(String taskId) {
        List<AgentLog> logs = getLogsByTaskId(taskId);
        if (logs == null || logs.isEmpty()) {
            return AgentExecutionStatsVO.builder()
                    .taskId(taskId)
                    .agentCount(0)
                    .totalDurationMs(0)
                    .overallStatus("NOT_FOUND")
                    .build();
        }

        int totalDuration = 0;
        Map<String,Integer> agentDurations = new HashMap<>();
        String overallStatus = "SUCCESS";
        for (AgentLog log : logs) {
            // 累加总耗时
            if (log.getDurationMs() != null) {
                totalDuration += log.getDurationMs();
                agentDurations.put(log.getAgentName(), log.getDurationMs());
            }
            // 判断总体状态
            if ("FAILED".equals(log.getStatus())) {
                overallStatus = "FAILED";
            } else if ("RUNNING".equals(log.getStatus()) && !"FAILED".equals(overallStatus)) {
                overallStatus = "RUNNING";
            }

        }
        return AgentExecutionStatsVO.builder()
                .taskId(taskId)
                .totalDurationMs(totalDuration)
                .agentDurations(agentDurations)
                .agentCount(logs.size())
                .overallStatus(overallStatus)
                .logs(logs)
                .build();
    }
}
