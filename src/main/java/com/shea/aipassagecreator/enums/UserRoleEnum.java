package com.shea.aipassagecreator.enums;

import lombok.Getter;

/**
 * 用户角色枚举
 * @author : Shea.
 * @since : 2026/5/19 09:56
 */
@Getter
public enum UserRoleEnum {

    ADMIN("admin", "管理员"),
    USER("user", "普通用户");

    private final String text;
    private final String value;

    UserRoleEnum(String value, String text) {
        this.value = value;
        this.text = text;
    }

    public static UserRoleEnum getEnumByValue(String value) {
        if (value == null) {
            return null;
        }
        for (UserRoleEnum userRoleEnum : UserRoleEnum.values()) {
            if (userRoleEnum.value.equals(value)) {
                return userRoleEnum;
            }
        }
        return null;
    }
}
