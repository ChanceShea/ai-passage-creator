package com.shea.aipassagecreator.domain.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 文章表
 * </p>
 *
 * @author Shea
 * @since 2026-05-20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Table(value = "article",camelToUnderline = false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Article implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 任务ID（UUID）
     */
    private String taskId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 选题
     */
    private String topic;

    /**
     * 主标题
     */
    private String mainTitle;

    /**
     * 副标题
     */
    private String subTitle;

    /**
     * 标题选项（JSON格式）
     */
    private String titleOptions;

    /**
     * 用户补充描述
     */
    private String userDescription;

    /**
     * 允许的图片生成方法（JSON数组）
     */
    private String enabledImageMethods;

    /**
     * 阶段：PENDING/TITLE_GENERATING/TITLE_SELECTING/
     * OUTLINE_GENERATING/OUTLINE_EDITING/CONTENT_GENERATING
     */
    private String phase;

    /**
     * 大纲（JSON格式）
     */
    private String outline;

    /**
     * 正文（Markdown格式）
     */
    private String content;

    /**
     * 完整图文（Markdown格式，含配图）
     */
    private String fullContent;

    /**
     * 封面图 URL
     */
    private String coverImage;

    /**
     * 配图列表（JSON数组）
     */
    private String images;

    /**
     * 状态：PENDING/PROCESSING/COMPLETED/FAILED
     */
    private String status;

    /**
     * 文章风格：tech/emotional/educational/humorous
     */
    private String style;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 完成时间
     */
    private LocalDateTime completedTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 是否删除
     */
    @Column(isLogicDelete = true)
    private Integer isDelete;


}
