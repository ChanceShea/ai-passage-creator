package com.shea.aipassagecreator.enums;

import lombok.Getter;

/**
 * 支付方式枚举
 * @author : Shea.
 * @since : 2026/5/30 21:15
 */
@Getter
public enum PaymentTypeEnum {

    STRIPE("STRIPE","Stripe"),
    ALIPAY("ALIPAY","支付宝");

    private final String value;
    private final String description;

    PaymentTypeEnum(String value, String description) {
        this.value = value;
        this.description = description;
    }

    /**
     * 根据值获取枚举
     * @param value 值
     * @return 枚举
     */
    public static PaymentTypeEnum getByValue(String value) {
        if (value == null) {
            return null;
        }
        for (PaymentTypeEnum paymentType : values()) {
            if (paymentType.value.equals(value)) {
                return paymentType;
            }
        }
        return null;
    }
}
