package com.shea.aipassagecreator.service;


import com.mybatisflex.core.service.IService;
import com.shea.aipassagecreator.domain.entity.User;

/**
 * <p>
 * 用户 服务类
 * </p>
 *
 * @author Shea
 * @since 2026-05-18
 */
public interface IUserService extends IService<User> {

    Long register(String userAccount, String userPassword, String checkPassword);
}
