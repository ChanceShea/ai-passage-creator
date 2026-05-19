package com.shea.aipassagecreator.domain.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 登录用户信息
 * @author : Shea.
 * @since : 2026/5/18 15:04
 */
@Data
public class LoginUserVO implements Serializable {

    private Long id;
    private String userAccount;
    private String userName;
    private String userAvatar;
    private String userProfile;
    private String userRole;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
