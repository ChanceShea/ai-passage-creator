package com.shea.aipassagecreator.enums;

import lombok.Getter;

/**
 * SSE 消息类型枚举
 * @author : Shea.
 * @since : 2026/5/20 19:47
 */
@Getter
public enum SseMessageTypeEnum {

    AGENT1_COMPLETE("AGENT1_COMPLETE","标题生成完成"),
    AGENT2_STREAMING("AGENT2_STREAMING","大纲流式输出"),
    AGENT2_COMPLETE("AGENT2_COMPLETE","大纲生成完成"),
    AGENT3_STREAMING("AGENT3_STREAMING","正文流式输出"),
    AGENT3_COMPLETE("AGENT3_COMPLETE","正文生成完成"),
    AGENT4_COMPLETE("AGENT4_COMPLETE","分析配图需求完成"),
    IMAGE_COMPLETE("IMAGE_COMPLETE","单张配图生成完成"),
    AGENT5_COMPLETE("AGENT5_COMPLETE","配图生成完成"),
    MERGE_COMPLETE("MERGE_COMPLETE","图文合成完成"),
    ALL_COMPLETE("ALL_COMPLETE","全部生成完成"),
    ERROR("ERROR","错误");

    private final String value;
    private final String text;
    SseMessageTypeEnum(String value, String text) {
        this.value = value;
        this.text = text;
    }

    public String getStreamingPrefix() {
        return this.value + ":";
    }
}
