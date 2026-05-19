package com.shea.aipassagecreator.domain.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户注册DTO
 * @author : Shea.
 * @since : 2026/5/18 15:03
 */
@Data
public class UserRegisterDTO implements Serializable {

    private String userAccount;
    private String userPassword;
    private String checkPassword;
}
