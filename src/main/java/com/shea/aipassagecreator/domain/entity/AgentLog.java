package com.shea.aipassagecreator.domain.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 智能体执行日志表
 * </p>
 *
 * @author Shea
 * @since 2026-06-02
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Table(value = "agent_log",camelToUnderline = false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 任务ID
     */
    private String taskId;

    /**
     * 智能体名称
     */
    private String agentName;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 耗时（毫秒）
     */
    private Integer durationMs;

    /**
     * 状态：SUCCESS/FAILED
     */
    private String status;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 使用的Prompt
     */
    private String prompt;

    /**
     * 输入数据（JSON格式）
     */
    private String inputData;

    /**
     * 输出数据（JSON格式）
     */
    private String outputData;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 是否删除
     */
    private Integer isDelete;


}
