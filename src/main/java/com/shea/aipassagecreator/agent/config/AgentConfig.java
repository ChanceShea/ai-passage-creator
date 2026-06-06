package com.shea.aipassagecreator.agent.config;

import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 智能体配置类
 * @author : Shea.
 * @since : 2026/6/4 15:49
 */
@Configuration
@Getter
public class AgentConfig {

    /**
     * 是否启用多智能体编排器
     * true: 使用 Spring AI Alibaba 多智能体编排
     * false: 使用原有的 ArticleAgentService
     */
    @Value("${article.agent.orchestrator.enabled:true}")
    private boolean orchestratorEnabled;

    /**
     * Agent最大迭代次数
     */
    @Value("${article.agent.max-iterations:10}")
    private int maxIterations;

    @Bean
    public MemorySaver memorySaver() {
        return new MemorySaver();
    }
}
