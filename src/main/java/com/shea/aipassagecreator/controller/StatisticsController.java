package com.shea.aipassagecreator.controller;

import com.shea.aipassagecreator.annotation.AuthCheck;
import com.shea.aipassagecreator.common.Result;
import com.shea.aipassagecreator.constant.UserConstant;
import com.shea.aipassagecreator.domain.vo.StatisticsVO;
import com.shea.aipassagecreator.service.IStatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 统计信息控制器
 * @author : Shea.
 * @since : 2026/6/3 22:31
 */
@RestController
@RequestMapping("/statistics")
@Slf4j
@RequiredArgsConstructor
public class StatisticsController {

    private final IStatisticsService statisticsService;

    @GetMapping("/overview")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public Result<StatisticsVO> getStatistics() {
        return Result.success(statisticsService.getStatistics());
    }
}
