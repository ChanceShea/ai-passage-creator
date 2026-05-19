package com.shea.aipassagecreator.service;


import com.mybatisflex.core.service.IService;
import com.shea.aipassagecreator.domain.dto.UserAddDTO;
import com.shea.aipassagecreator.domain.entity.User;
import com.shea.aipassagecreator.domain.vo.LoginUserVO;
import jakarta.servlet.http.HttpServletRequest;

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

    LoginUserVO login(String userAccount, String userPassword, HttpServletRequest request);

    User getLoginUser(HttpServletRequest request);

    boolean logout(HttpServletRequest request);

    LoginUserVO getLoginUserVO(HttpServletRequest request);

    boolean addUser(UserAddDTO dto);
}
