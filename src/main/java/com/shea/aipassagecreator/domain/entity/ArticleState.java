package com.shea.aipassagecreator.domain.entity;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 文章状态
 * @author : Shea.
 * @since : 2026/5/20 19:05
 */
@Data
public class ArticleState implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 任务ID
     */
    private String taskId;

    /**
     * 选题
     */
    private String topic;

    /**
     * 智能体1输出结果 标题结果
     */
    private TitleResult title;

    /**
     * 智能体2输出结果 大纲结果
     */
    private OutlineResult outline;

    /**
     * 智能体3输出结果 正文内容
     */
    private String content;

    /**
     * 智能体4输出结果 图片要求列表
     */
    private List<ImageRequirement> imageRequirementList;

    /**
     * 封面图片URL
     */
    private String coverImage;

    /**
     * 智能体5输出结果 图片结果列表
     */
    private List<ImageResult> images;

    /**
     * 完整图文内容
     */
    private String fullContent;

    /**
     * 标题结果
     */
    @Data
    public static class TitleResult implements Serializable {
        private String mainTitle;
        private String subTitle;
    }

    /**
     * 大纲结果
     */
    @Data
    public static class OutlineResult implements Serializable {
        private List<OutlineSection> sections;
    }

    /**
     * 大纲章节
     */
    @Data
    public static class OutlineSection implements Serializable {
        private Integer section;
        private String title;
        private List<String> points;
    }

    /**
     * 图片要求
     */
    @Data
    public static class ImageRequirement implements Serializable {
        private Integer position;
        private String type;
        private String sectionTitle;
        private String keywords;
    }

    /**
     * 图片结果
     */
    @Data
    public static class ImageResult implements Serializable {
        private Integer position;
        private String url;
        private String method;
        private String keywords;
        private String sectionTitle;
        private String description;
    }
}
