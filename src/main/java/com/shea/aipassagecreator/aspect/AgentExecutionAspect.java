package com.shea.aipassagecreator.aspect;

import cn.hutool.json.JSONUtil;
import com.shea.aipassagecreator.annotation.AgentExecution;
import com.shea.aipassagecreator.domain.entity.AgentLog;
import com.shea.aipassagecreator.domain.entity.ArticleState;
import com.shea.aipassagecreator.service.IAgentLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 智能体执行AOP切面
 * @author : Shea.
 * @since : 2026/6/2 21:30
 */
@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class AgentExecutionAspect {

    private final IAgentLogService agentLogService;

    @Around("@annotation(agentExecution)")
    public Object aroundAgentExecution(
            ProceedingJoinPoint pjp,
            AgentExecution agentExecution
    ) throws Throwable {
        long start = Instant.now().toEpochMilli();
        LocalDateTime startDateTime = LocalDateTime.now();
        // 提取taskId和输入数据
        String taskId = extractTaskId(pjp);
        String inputData = extractInputData(pjp);
        String prompt = extractPrompt(pjp);
        // 创建日志对象
        AgentLog agentLog = AgentLog.builder()
                .taskId(taskId)
                .agentName(agentExecution.value())
                .startTime(startDateTime)
                .status("RUNNING")
                .prompt(prompt)
                .inputData(inputData)
                .build();

        Object result = null;
        try {
            // 执行目标方法
            result = pjp.proceed();
            // 记录成功状态
            agentLog.setStatus("SUCCESS");
            agentLog.setEndTime(LocalDateTime.now());
            agentLog.setDurationMs((int) (System.currentTimeMillis() - start));
            agentLog.setOutputData(extractOutputData(result));

            log.info("智能体执行成功：{}，taskId={}，耗时={}ms",
                    agentExecution.value(),
                    taskId,
                    agentLog.getDurationMs());
        } catch (Exception e) {
            // 记录失败信息
            agentLog.setStatus("FAILED");
            agentLog.setEndTime(LocalDateTime.now());
            agentLog.setDurationMs((int) (System.currentTimeMillis() - start));
            agentLog.setErrorMessage(e.getMessage() != null ? e.getMessage() : e.getClass().getName());
            log.error("智能体执行失败：{}，taskId={}，error={}",
                    agentExecution.value(), taskId, e.getMessage());
            throw e;
        }finally {
            // 异步保存日志
            agentLogService.saveLogAsync(agentLog);
        }
        return result;
    }

    /**
     * 从连接点中提取输出数据
     * @param result 输出数据
     * @return 输出数据
     */
    private String extractOutputData(Object result) {
        try {
            if (result == null) {
                return null;
            }
            // 只记录简单类型，避免数据过大
            if (result instanceof String || result instanceof Number || result instanceof Boolean) {
                return String.valueOf(result);
            }
            // 对于集合类型，只记录数量
            if (result instanceof List) {
                return "{\"listSize\":" + ((List<?>) result).size() + "}";
            }
            return "{\"type\": \"" + result.getClass().getSimpleName() + "\"}";
        }catch (Exception e) {
            log.warn("提取输出数据失败，{}", e.getMessage());
            return null;
        }
    }

    /**
     * 从连接点中提取提示信息
     * @param pjp 连接点
     * @return 提示信息
     */
    private String extractPrompt(ProceedingJoinPoint pjp) {
        try {
            MethodSignature signature = (MethodSignature) pjp.getSignature();
            Method method = signature.getMethod();
            return method.getDeclaringClass().getSimpleName() + "." + method.getName();
        }catch (Exception e) {
            return null;
        }
    }

    /**
     * 从连接点中提取输入数据
     * @param pjp 连接点
     * @return 输入数据
     */
    private String extractInputData(ProceedingJoinPoint pjp) {
        try {
            Object[] args = pjp.getArgs();
            if (args == null || args.length == 0) {
                return null;
            }
            Map<String,Object> inputMap = new HashMap<>();
            MethodSignature signature = (MethodSignature) pjp.getSignature();
            String[] parameterNames = signature.getParameterNames();
            for (int i = 0; i < args.length && i < parameterNames.length; i++) {
                Object arg = args[i];
                // 只记录基本类型和简单对象，避免数据过大
                if (arg instanceof String || arg instanceof Number || arg instanceof Boolean) {
                    inputMap.put(parameterNames[i], arg);
                } else if (arg instanceof ArticleState articleState) {
                    inputMap.put("taskId", articleState.getTaskId());
                    if (articleState.getTitle() != null) {
                        inputMap.put("mainTitle", articleState.getTitle().getMainTitle());
                    }
                }
            }
            return inputMap.isEmpty() ? null : JSONUtil.toJsonStr(inputMap);
        }catch (Exception e) {
            log.warn("提取输入数据失败",e);
            return null;
        }
    }

    /**
     * 从连接点中提取任务ID
     * @param pjp 连接点
     * @return 任务ID
     */
    private String extractTaskId(ProceedingJoinPoint pjp) {
        Object[] args = pjp.getArgs();
        if (args == null || args.length == 0) {
            return "Unknown";
        }
        // 优先从ArticleState中获取taskId
        for (Object arg : args) {
            if (arg instanceof ArticleState) {
                return ((ArticleState) arg).getTaskId();
            }
        }
        // 尝试从第一个String参数中获取
        for (Object arg : args) {
            if (arg instanceof String) {
                return (String) arg;
            }
        }
        return "Unknown";
    }
}
