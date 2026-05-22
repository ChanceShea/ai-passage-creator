package com.shea.aipassagecreator.manager;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.shea.aipassagecreator.constant.ArticleConstant.SSE_RECONNECT_TIME_MS;
import static com.shea.aipassagecreator.constant.ArticleConstant.SSE_TIMEOUT_MS;

/**
 * SSE管理器
 * @author : Shea.
 * @since : 2026/5/21 09:13
 */
@Component
@Slf4j
public class SseEmitterManager {
    /**
     * SSE连接管理器
     */
    private final Map<String, SseEmitter> emitterMap = new ConcurrentHashMap<>();

    /**
     * 创建SSE连接
     * @param id 连接ID
     * @return SseEmitter
     */
    public SseEmitter createEmitter(String id) {
        SseEmitter sseEmitter = new SseEmitter(SSE_TIMEOUT_MS);
        sseEmitter.onTimeout(() -> {
            log.warn("SSE连接超时，taskId={}", id);
            emitterMap.remove(id);
        });
        sseEmitter.onCompletion(() -> {
            log.info("SSE连接完成，taskId={}", id);
            emitterMap.remove(id);
        });
        sseEmitter.onError((e) -> {
            log.error("SSE连接错误，taskId={}，errorType={}，errorMsg={}", id, e.getClass().getSimpleName(), e.getMessage());
            emitterMap.remove(id);
        });
        emitterMap.put(id, sseEmitter);
        log.info("SSE连接创建成功，taskId={}", id);
        return sseEmitter;
    }

    /**
     * 发送SSE消息
     * @param taskId 连接ID
     * @param message 消息
     */
    public void send(String taskId, String message) {
        SseEmitter sseEmitter = emitterMap.get(taskId);
        if (sseEmitter == null) {
            log.warn("SseEmitter不存在，taskId={}，message={}", taskId, message);
            return;
        }

        try {
            sseEmitter.send(SseEmitter.event()
                    .data(message)
                    .reconnectTime(SSE_RECONNECT_TIME_MS));
            log.debug("SSE消息发送成功，taskId={}，message={}", taskId, message);
        }catch (Exception e) {
            log.error("SSE消息发送失败，taskId={}，error={}", taskId, e.getMessage());
            // 发送失败时才移除，避免重复remove
            emitterMap.remove(taskId);
        }
    }

    /**
     * 完成SSE连接
     * @param taskId 连接ID
     */
    public void complete(String taskId) {
        SseEmitter sseEmitter = emitterMap.get(taskId);
        if (sseEmitter == null) {
            log.warn("SseEmitter不存在，taskId={}", taskId);
            return;
        }
        try {
            sseEmitter.complete();
            log.info("SSE连接完成，taskId={}", taskId);
        }catch (Exception e) {
            log.error("SSE连接完成失败，taskId={}", taskId);
        } finally {
            emitterMap.remove(taskId);
        }
    }

    /**
     * 检查SSE连接是否存在
     * @param taskId 连接ID
     * @return 是否存在
     */
    public boolean exists(String taskId) {
        return emitterMap.containsKey(taskId);
    }
}
