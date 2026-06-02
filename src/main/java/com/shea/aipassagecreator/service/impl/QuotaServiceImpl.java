package com.shea.aipassagecreator.service.impl;

import com.shea.aipassagecreator.constant.UserConstant;
import com.shea.aipassagecreator.domain.entity.User;
import com.shea.aipassagecreator.exception.BusinessException;
import com.shea.aipassagecreator.exception.ErrorCode;
import com.shea.aipassagecreator.mapper.UserMapper;
import com.shea.aipassagecreator.service.IQuotaService;
import com.shea.aipassagecreator.service.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 配额服务实现类
 * @author : Shea.
 * @since : 2026/6/1 16:37
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class QuotaServiceImpl implements IQuotaService {

    private final IUserService userService;
    private final UserMapper userMapper;

    /**
     * 检查用户是否有配额
     * @param user 用户
     * @return 是否有配额
     */
    @Override
    public boolean hasQuota(User user) {
        if (isAdmin(user) || isVip(user)) {
            return true;
        }
        User freshUser = userService.getById(user.getId());
        if (freshUser == null) {
            return false;
        }
        return freshUser.getQuota() != null && freshUser.getQuota() > 0;
    }

    /**
     * 检查用户是否是VIP
     * @param user 用户
     * @return 是否是VIP
     */
    private boolean isVip(User user) {
        return UserConstant.VIP_ROLE.equals(user.getUserRole());
    }

    /**
     * 检查用户是否是管理员
     * @param user 用户
     * @return 是否是管理员
     */
    private boolean isAdmin(User user) {
        return UserConstant.ADMIN_ROLE.equals(user.getUserRole());
    }

    /**
     * 消费用户配额
     * @param user 用户
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void consumeQuota(User user) {
        if (isAdmin(user) || isVip(user)) {
            return;
        }
        // 原子更新扣减用户配额，避免并发更新问题
        int affectedRows = userMapper.decrementQuota(user.getId());
        if (affectedRows > 0) {
            log.info("用户配额已消耗，userId={}", user.getId());
        } else {
            log.warn("用户配额扣减失败（可能配额不足或并发冲突），userId={}", user.getId());
        }
    }

    /**
     * 检查并消费用户配额
     * @param user 用户
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void checkAndConsumeQuota(User user) {
        if (isAdmin(user) || isVip(user)) {
            return;
        }
        // 使用原子更新：检查与消费合并为一个原子操作
        int affectedRows = userMapper.decrementQuota(user.getId());
        if (affectedRows == 0) {
            // 影响行数为0，说明配额不足（已被其他请求消耗）
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"配额不足，无法创建文章");
        }
        log.info("用户配额检查并消耗成功，userId={}", user.getId());
    }
}
