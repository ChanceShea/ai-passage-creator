package com.shea.aipassagecreator.controller;


import cn.hutool.core.util.StrUtil;
import com.shea.aipassagecreator.common.Result;
import com.shea.aipassagecreator.domain.vo.AgentExecutionStatsVO;
import com.shea.aipassagecreator.exception.ErrorCode;
import com.shea.aipassagecreator.service.IAgentLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.shea.aipassagecreator.exception.ThrowUtils.throwIf;

/**
 * <p>
 * 智能体执行日志表 前端控制器
 * </p>
 *
 * @author Shea
 * @since 2026-06-02
 */
@RestController
@RequestMapping("/agent-log")
@RequiredArgsConstructor
public class AgentLogController {

    private final IAgentLogService agentLogService;

    /**
     * 根据任务ID获取执行日志
     * @param taskId 任务ID
     * @return 执行日志
     */
    @GetMapping("/execution-logs/{taskId}")
    public Result<AgentExecutionStatsVO> getExecutionLogs(@PathVariable String taskId) {
        throwIf(StrUtil.isBlank(taskId), ErrorCode.PARAMS_ERROR,"任务ID不能为空");
        AgentExecutionStatsVO stats = agentLogService.getAgentExecutionStats(taskId);
        return Result.success(stats);
    }
}
