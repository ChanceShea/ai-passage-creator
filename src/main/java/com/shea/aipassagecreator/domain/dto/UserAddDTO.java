package com.shea.aipassagecreator.domain.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户新增DTO
 * @author : Shea.
 * @since : 2026/5/19 10:14
 */
@Data
public class UserAddDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String userAccount;
    private String userPassword;
    private String userName;
    private String userAvatar;
    private String userProfile;
    private String userRole;

}
