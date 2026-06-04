package com.shea.aipassagecreator.service;

import com.shea.aipassagecreator.domain.vo.StatisticsVO;

/**
 * 统计服务接口
 * @author : Shea.
 * @since : 2026/6/3 20:40
 */
public interface IStatisticsService {

    /**
     * 获取统计信息
     * @return 统计信息
     */
    StatisticsVO getStatistics();
}
