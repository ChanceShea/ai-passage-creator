package com.shea.aipassagecreator.service;

import com.shea.aipassagecreator.domain.entity.User;

/**
 * 配额服务接口
 * @author : Shea.
 * @since : 2026/6/1 16:23
 */
public interface IQuotaService {

    /**
     * 判断用户是否还有配额
     * @param user 用户
     * @return 是否有配额
     */
    boolean hasQuota(User user);

    /**
     * 消费用户配额
     * @param user 用户
     */
    void consumeQuota(User user);

    /**
     * 检查并消费用户配额
     * @param user 用户
     */
    void checkAndConsumeQuota(User user);
}
