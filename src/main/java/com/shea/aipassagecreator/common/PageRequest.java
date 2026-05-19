package com.shea.aipassagecreator.common;

import lombok.Data;

/**
 * 分页请求
 * @author : Shea.
 * @since : 2026/5/18 09:36
 */
@Data
public class PageRequest {

    private int page;

    private int size;
}
