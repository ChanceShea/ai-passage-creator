package com.shea.aipassagecreator.domain.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户登录DTO
 * @author : Shea.
 * @since : 2026/5/18 15:03
 */
@Data
public class UserLoginDTO implements Serializable {

    private String userAccount;
    private String userPassword;
}
