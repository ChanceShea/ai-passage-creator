package com.shea.aipassagecreator.agent.context;

import java.util.function.Consumer;

/**
 * 流式输出处理器上下文
 * 使用ThreadLocal保存streamHandler，避免将其放入StateGraph状态中（无法序列化）
 * @author : Shea.
 * @since : 2026/6/4 15:57
 */
public class StreamHandlerContext {

    private static final ThreadLocal<Consumer<String>> STREAM_HANDLER = new ThreadLocal<>();

    /**
     * 设置流式输出处理器
     * @param streamHandler 流式输出处理器
     */
    public static void set(Consumer<String> streamHandler) {
        STREAM_HANDLER.set(streamHandler);
    }

    /**
     * 获取流式输出处理器
     * @return 流式输出处理器
     */
    public static Consumer<String> get() {
        return STREAM_HANDLER.get();
    }

    /**
     * 清空流式输出处理器
     */
    public static void clear() {
        STREAM_HANDLER.remove();
    }

    /**
     * 发送消息到流式输出
     * @param message 消息
     */
    public static void send(String message) {
        Consumer<String> streamHandler = STREAM_HANDLER.get();
        if (streamHandler != null && message != null) {
            streamHandler.accept(message);
        }
    }
}
