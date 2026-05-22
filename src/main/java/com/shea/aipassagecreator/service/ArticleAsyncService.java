package com.shea.aipassagecreator.service;

import cn.hutool.json.JSONUtil;
import com.shea.aipassagecreator.domain.entity.ArticleState;
import com.shea.aipassagecreator.enums.ArticleStatusEnum;
import com.shea.aipassagecreator.enums.SseMessageTypeEnum;
import com.shea.aipassagecreator.manager.SseEmitterManager;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 文章异步服务类
 * @author : Shea.
 * @since : 2026/5/21 09:37
 */
@Slf4j
@Service
public class ArticleAsyncService {

    @Resource
    private ArticleAgentService articleAgentService;
    @Resource
    private SseEmitterManager sseEmitterManager;
    @Resource
    private IArticleService articleService;

    /**
     * 执行文章生成
     * @param taskId 任务ID
     * @param topic 文章选题
     */
    @Async("articleExecutor")
    public void executeArticleGeneration(String taskId,String topic) {
        log.info("异步任务开始，taskId={},topic={}", taskId, topic);
        try {
            articleService.updateArticleStatus(taskId, ArticleStatusEnum.PROCESSING, null);
            ArticleState state = new ArticleState();
            state.setTaskId(taskId);
            state.setTopic(topic);
            articleAgentService.executeArticleGeneration(state,message -> handleAgentMessage(taskId,message,state));
            articleService.updateArticleStatus(taskId,ArticleStatusEnum.COMPLETED,null);
            sendSseMessage(taskId, SseMessageTypeEnum.ALL_COMPLETE, Map.of("taskId",taskId));
            sseEmitterManager.complete(taskId);
            log.info("异步任务完成，taskId={}", taskId);
            articleService.saveArticleContent(taskId,state);
        }catch (Exception e) {
            log.error("异步任务失败，taskId={}",taskId,e);
            articleService.updateArticleStatus(taskId,ArticleStatusEnum.FAILED,e.getMessage());
            sendSseMessage(taskId,SseMessageTypeEnum.ERROR,Map.of("message",e.getMessage()));
            sseEmitterManager.complete(taskId);
        }
    }

    /**
     * 处理智能体消息并推送
     * @param taskId 任务ID
     * @param message 消息
     * @param state 文章状态
     */
    private void handleAgentMessage(String taskId, String message, ArticleState state) {
        Map<String,Object> data = buildMessageData(message,state);
        if (data != null) {
            boolean exists = sseEmitterManager.exists(taskId);
            log.debug("准备发送SSE消息，taskId={}，exists={}，messageType={}", taskId, exists, data.get("type"));
            if (!exists) {
                log.warn("SSE连接已断开，停止发送消息，taskId={}，messageType={}", taskId, data.get("type"));
                return;
            }
            sseEmitterManager.send(taskId,JSONUtil.toJsonStr(data));
        }
    }

    private Map<String, Object> buildMessageData(String message, ArticleState state) {
        String streamingPrefix2 = SseMessageTypeEnum.AGENT2_STREAMING.getStreamingPrefix();
        String streamingPrefix3 = SseMessageTypeEnum.AGENT3_STREAMING.getStreamingPrefix();
        String imageCompletePrefix = SseMessageTypeEnum.IMAGE_COMPLETE.getStreamingPrefix();
        if (message.startsWith(streamingPrefix2)) {
            return buildStreamingData(SseMessageTypeEnum.AGENT2_STREAMING,
                    message.substring(streamingPrefix2.length()));
        }
        if (message.startsWith(streamingPrefix3)) {
            return buildStreamingData(SseMessageTypeEnum.AGENT3_STREAMING,
                    message.substring(streamingPrefix3.length()));
        }
        if (message.startsWith(imageCompletePrefix)) {
            String imageJson = message.substring(imageCompletePrefix.length());
            return buildImageCompleteData(imageJson);
        }
        return buildCompleteMessageData(message,state);
    }

    /**
     * 构建完成消息数据
     * @param message 消息
     * @param state 文章状态
     * @return 消息数据
     */
    private Map<String, Object> buildCompleteMessageData(String message, ArticleState state) {
        Map<String,Object> data = new HashMap<>();
        if (SseMessageTypeEnum.AGENT1_COMPLETE.getValue().equals(message)) {
            data.put("type",SseMessageTypeEnum.AGENT1_COMPLETE.getValue());
            data.put("title",state.getTitle());
        } else if (SseMessageTypeEnum.AGENT2_COMPLETE.getValue().equals(message)) {
            data.put("type",SseMessageTypeEnum.AGENT2_COMPLETE.getValue());
            data.put("outline",state.getOutline().getSections());
        } else if (SseMessageTypeEnum.AGENT3_COMPLETE.getValue().equals(message)) {
            data.put("type",SseMessageTypeEnum.AGENT3_COMPLETE.getValue());
        } else if (SseMessageTypeEnum.AGENT4_COMPLETE.getValue().equals(message)) {
            data.put("type",SseMessageTypeEnum.AGENT4_COMPLETE.getValue());
            data.put("imageRequirements",state.getImageRequirementList());
        } else if (SseMessageTypeEnum.AGENT5_COMPLETE.getValue().equals(message)) {
            data.put("type",SseMessageTypeEnum.AGENT5_COMPLETE.getValue());
            data.put("images",state.getImages());
        } else if (SseMessageTypeEnum.MERGE_COMPLETE.getValue().equals(message)) {
            data.put("type",SseMessageTypeEnum.MERGE_COMPLETE.getValue());
            data.put("fullContent",state.getFullContent());
        } else {
            return null;
        }
        return data;
    }

    /**
     * 构建图片完成数据
     * @param imageJson 图片JSON数据
     * @return 消息数据
     */
    private Map<String, Object> buildImageCompleteData(String imageJson) {
        Map<String,Object> data = new HashMap<>();
        data.put("type",SseMessageTypeEnum.IMAGE_COMPLETE.getValue());
        data.put("data",JSONUtil.toBean(imageJson,ArticleState.ImageResult.class));
        return data;
    }

    /**
     * 构建流式数据
     * @param sseMessageTypeEnum 消息类型
     * @param content 内容
     * @return 消息数据
     */
    private Map<String, Object> buildStreamingData(SseMessageTypeEnum sseMessageTypeEnum, String content) {
        Map<String,Object> data = new HashMap<>();
        data.put("type",sseMessageTypeEnum.getValue());
        data.put("data",content);
        return data;
    }

    /**
     * 发送SSE消息
     * @param taskId 任务ID
     * @param sseMessageTypeEnum 消息类型
     * @param additionalData 额外数据
     */
    private void sendSseMessage(String taskId, SseMessageTypeEnum sseMessageTypeEnum, Map<String, Object> additionalData) {
        Map<String,Object> data = new HashMap<>();
        data.put("type",sseMessageTypeEnum.getValue());
        data.putAll(additionalData);
        sseEmitterManager.send(taskId, JSONUtil.toJsonStr(data));
    }
}
