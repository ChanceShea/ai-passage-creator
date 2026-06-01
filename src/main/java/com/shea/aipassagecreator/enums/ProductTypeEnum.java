package com.shea.aipassagecreator.enums;

import lombok.Getter;

import java.math.BigDecimal;

/**
 * 产品类型枚举
 * @author : Shea.
 * @since : 2026/5/30 21:07
 */
@Getter
public enum ProductTypeEnum {

    VIP_PERMANENT("VIP_PERMANENT", "永久VIP会员", new BigDecimal("199.00"));

    private final String value;
    private final String description;
    private final BigDecimal price;

    ProductTypeEnum(String value, String description, BigDecimal price) {
        this.value = value;
        this.description = description;
        this.price = price;
    }

    /**
     * 根据值获取枚举
     * @param value 值
     * @return 枚举
     */
    public static ProductTypeEnum getByValue(String value) {
        if (value == null) {
            return null;
        }
        for (ProductTypeEnum productTypeEnum : ProductTypeEnum.values()) {
            if (productTypeEnum.value.equals(value)) {
                return productTypeEnum;
            }
        }
        return null;
    }
}
