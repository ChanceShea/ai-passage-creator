package com.shea.aipassagecreator.service.impl;

import com.mybatisflex.core.query.QueryWrapper;
import com.shea.aipassagecreator.constant.UserConstant;
import com.shea.aipassagecreator.domain.entity.Article;
import com.shea.aipassagecreator.domain.entity.User;
import com.shea.aipassagecreator.domain.vo.StatisticsVO;
import com.shea.aipassagecreator.enums.ArticleStatusEnum;
import com.shea.aipassagecreator.enums.UserRoleEnum;
import com.shea.aipassagecreator.mapper.ArticleMapper;
import com.shea.aipassagecreator.mapper.UserMapper;
import com.shea.aipassagecreator.service.IAgentLogService;
import com.shea.aipassagecreator.service.IStatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 统计服务实现类
 * @author : Shea.
 * @since : 2026/6/3 20:41
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class StatisticsServiceImpl implements IStatisticsService {

    private static final String STATISTICS_CACHE_KEY = "statistics:overview";
    private static final long CACHE_EXPIRE_HOURS = 1;

    private final ArticleMapper articleMapper;
    private final UserMapper userMapper;
    private final IAgentLogService agentLogService;
    private final RedisTemplate<String,Object> redisTemplate;

    @Override
    public StatisticsVO getStatistics() {
        // 先从缓存中获取统计数据
        StatisticsVO cachedVo = (StatisticsVO) redisTemplate.opsForValue().get(STATISTICS_CACHE_KEY);
        if (cachedVo != null) {
            log.info("从Redis缓存中获取数据");
            return cachedVo;
        }

        // 缓存数据不存在，重新计算并存储到缓存中
        // 今天创作数量
        Long todayCount = countArticlesByDateRange(getTodayStart(), LocalDateTime.now());
        // 本周创作数量
        Long weekCount = countArticlesByDateRange(getWeekStart(), LocalDateTime.now());
        // 本月创作数量
        Long monthCount = countArticlesByDateRange(getMonthStart(), LocalDateTime.now());
        // 总创作数量
        Long totalCount = countTotalArticles();
        // 成功率
        Double successRate = calculateSuccessRate();
        // 平均耗时统计
        Integer avgDurationMs = calculateAvgDuration();
        // 活跃用户统计（本周所有有创作的用户）
        Long activeUserCount = countActiveUsers(getWeekStart());
        // 统计总用户数
        Long totalUserCount = countTotalUsers();
        // 统计VIP用户数
        Long vipUserCount = countVipUsers();
        // 统计配额使用情况
        Long quotaUsed = calculateQuotaUsed();
        StatisticsVO vo = StatisticsVO.builder()
                .todayCount(todayCount)
                .weekCount(weekCount)
                .monthCount(monthCount)
                .totalCount(totalCount)
                .successRate(successRate)
                .avgDurationMs(avgDurationMs)
                .activeUserCount(activeUserCount)
                .totalUserCount(totalUserCount)
                .vipUserCount(vipUserCount)
                .quotaUsed(quotaUsed)
                .build();
        redisTemplate.opsForValue().set(STATISTICS_CACHE_KEY, vo,CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
        log.info("统计数据已缓存，过期时间：{}小时", CACHE_EXPIRE_HOURS);
        return vo;
    }

    /**
     * 计算配额使用情况
     * @return 配额使用情况
     */
    private Long calculateQuotaUsed() {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("userRole", UserRoleEnum.USER.getValue());
        try {
            List<User> normalUsers = userMapper.selectListByQuery(queryWrapper);
            Long normalUserCount = (long)normalUsers.size();
            long remainQuota = normalUsers.stream()
                    .mapToInt(user -> user.getQuota() != null ? user.getQuota() : 0)
                    .sum();
            return (normalUserCount * UserConstant.DEFAULT_QUOTA) - remainQuota;
        }catch (Exception e) {
            log.warn("计算配额使用情况失败",e);
        }
        return 0L;
    }

    /**
     * 统计VIP用户数
     * @return VIP用户数
     */
    private Long countVipUsers() {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("userRole", UserRoleEnum.VIP.getValue());
        return userMapper.selectCountByQuery(queryWrapper);
    }

    /**
     * 统计总用户数
     * @return 总用户数
     */
    private Long countTotalUsers() {
        return userMapper.selectCountByQuery(QueryWrapper.create());
    }

    /**
     * 统计活跃用户数
     * @param start 开始时间
     * @return 活跃用户数
     */
    private Long countActiveUsers(LocalDateTime start) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .ge("createTime", start);
        try {
            List<Article> articles = articleMapper.selectListByQuery(queryWrapper);
            return articles.stream()
                    .map(Article::getUserId)
                    .distinct().count();
        }catch (Exception e) {
            log.warn("统计活跃用户失败",e);
        }
        return 0L;
    }

    /**
     * 计算平均耗时
     * @return 平均耗时
     */
    private Integer calculateAvgDuration() {
         QueryWrapper queryWrapper = QueryWrapper.create()
                 .eq("status",ArticleStatusEnum.COMPLETED.getValue())
                 .isNotNull("completedTime");
         try {
             List<Article> completedArticles = articleMapper.selectListByQuery(queryWrapper);
             if (completedArticles == null || completedArticles.isEmpty()) {
                 return 0;
             }
             double avgDuration = completedArticles.stream()
                     .filter(article -> article.getCreateTime() != null && article.getCompletedTime() != null)
                     .mapToLong(article -> {
                         long createMillis = Timestamp.valueOf(article.getCreateTime()).getTime();
                         long completedMillis = Timestamp.valueOf(article.getCompletedTime()).getTime();
                         return completedMillis - createMillis;
                     })
                     .average()
                     .orElse(0.0);
             return (int) avgDuration;
         }catch (Exception e) {
             log.warn("计算平均耗时失败",e);
         }
         return 0;
    }

    /**
     * 计算成功率
     * @return 成功率
     */
    private Double calculateSuccessRate() {
        Long totalCount = countTotalArticles();
        if (totalCount == 0) {
            return 0.0;
        }
        QueryWrapper status = QueryWrapper.create()
                .eq("status", ArticleStatusEnum.COMPLETED.getValue());
        long successCount = articleMapper.selectCountByQuery(status);
        return ((double) successCount / totalCount.doubleValue()) * 100;
    }

    /**
     * 统计总文章数量
     * @return 文章数量
     */
    private Long countTotalArticles() {
        return articleMapper.selectCountByQuery(QueryWrapper.create());
    }

    /**
     * 获取本月的开始时间
     * @return 本月的开始时间
     */
    private LocalDateTime getMonthStart() {
        LocalDate today = LocalDate.now();
        LocalDate firstDayOfMonth = today.withDayOfMonth(1);
        return LocalDateTime.of(firstDayOfMonth, LocalTime.MIN);
    }

    /**
     * 获取本周的开始时间
     * @return 本周的开始时间
     */
    private LocalDateTime getWeekStart() {
        LocalDate today = LocalDate.now();
        LocalDate monday = today.minusDays(today.getDayOfWeek().getValue() - 1);
        return LocalDateTime.of(monday, LocalTime.MIN);
    }

    /**
     * 根据时间范围统计文章数量
     * @param start 开始时间
     * @param end 结束时间
     * @return 文章数量
     */
    private Long countArticlesByDateRange(LocalDateTime start, LocalDateTime end) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.ge("createTime", start)
                .le("createTime", end);
        return articleMapper.selectCountByQuery(queryWrapper);
    }

    /**
     * 获取今天的开始时间
     * @return 今天的开始时间
     */
    private LocalDateTime getTodayStart() {
        return LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
    }
}
