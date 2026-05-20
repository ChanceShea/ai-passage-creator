package com.shea.aipassagecreator.service.impl;


import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.shea.aipassagecreator.constant.UserConstant;
import com.shea.aipassagecreator.domain.dto.UserAddDTO;
import com.shea.aipassagecreator.domain.entity.User;
import com.shea.aipassagecreator.domain.vo.LoginUserVO;
import com.shea.aipassagecreator.exception.BusinessException;
import com.shea.aipassagecreator.exception.ErrorCode;
import com.shea.aipassagecreator.mapper.UserMapper;
import com.shea.aipassagecreator.service.IUserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;

import static com.shea.aipassagecreator.constant.UserConstant.USER_LOGIN_STATE;
import static com.shea.aipassagecreator.exception.ThrowUtils.throwIf;

/**
 * <p>
 * 用户 服务实现类
 * </p>
 *
 * @author Shea
 * @since 2026-05-18
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Override
    public Long register(String userAccount, String userPassword, String checkPassword) {
        // 参数校验
        if (StrUtil.hasBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号长度过短");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度过短");
        }
        if (!checkPassword.equals(userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("userAccount", userAccount);
        long count = this.count(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号已存在");
        }
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword(userPassword));
        user.setUserName(userAccount);
        boolean saveResult = this.save(user);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "注册失败");
        }
        return user.getId();
    }

    @Override
    public LoginUserVO login(String userAccount, String userPassword, HttpServletRequest request) {
        throwIf(userAccount == null || userPassword == null, new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空"));
        String encryptedPassword = encryptPassword(userPassword);
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("userAccount", userAccount).eq("userPassword", encryptedPassword);
        User user = this.getOne(queryWrapper);
        throwIf(user == null, new BusinessException(ErrorCode.PARAMS_ERROR, "账号或密码错误"));
        // 登录成功，设置登录状态
        request.getSession().setAttribute(USER_LOGIN_STATE, user);
        return this.toVO(user);
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        User user = (User)request.getSession().getAttribute(USER_LOGIN_STATE);
        if (user == null || user.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        Long id = user.getId();
        user = this.getById(id);
        throwIf(user == null, new BusinessException(ErrorCode.NOT_LOGIN_ERROR));
        return user;
    }

    @Override
    public LoginUserVO getLoginUserVO(HttpServletRequest request) {
        return toVO(getLoginUser(request));
    }

    @Override
    public boolean addUser(UserAddDTO dto) {
        User user = new User();
        BeanUtils.copyProperties(dto, user);
        boolean save = this.save(user);
        throwIf(!save, new BusinessException(ErrorCode.PARAMS_ERROR, "新增用户失败"));
        return true;
    }

    @Override
    public User handleGithubLogin(OAuth2User oAuth2User) {
        // GitHub 返回的 id 是 Integer 类型,需要正确转换为 String
        Object githubIdObj = oAuth2User.getAttribute("id");
        String githubId = githubIdObj != null ? githubIdObj.toString() : null;
        String login = oAuth2User.getAttribute("login");
        String name = oAuth2User.getAttribute("name");
        String avatarUrl = oAuth2User.getAttribute("avatar_url");
        
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("githubId", githubId);
        User user = this.getOne(queryWrapper);
        
        if (user == null) {
            // 新用户，创建账号
            user = new User();
            user.setGithubId(githubId);
            user.setUserName(name != null ? name : login);
            user.setUserAvatar(avatarUrl);
            user.setUserAccount("github_" + githubId);
            user.setUserPassword("");
            user.setUserRole(UserConstant.DEFAULT_ROLE);
            this.save(user);
        }
        
        return user;
    }

    @Override
    public boolean logout(HttpServletRequest request) {
        Object attribute = request.getSession().getAttribute(USER_LOGIN_STATE);
        throwIf(attribute == null, new BusinessException(ErrorCode.NOT_LOGIN_ERROR));
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return true;
    }

    private String encryptPassword(String password) {
        String salt = "shea";
        return DigestUtils.md5DigestAsHex((password + salt).getBytes(StandardCharsets.UTF_8));
    }

    private LoginUserVO toVO(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(user, loginUserVO);
        return loginUserVO;
    }
}
