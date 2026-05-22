package com.shea.aipassagecreator.service;

import com.shea.aipassagecreator.enums.ImageMethodEnum;

/**
 * 图像搜索服务
 * @author : Shea.
 * @since : 2026/5/20 19:39
 */
public interface IImageSearchService {

    /**
     * 根据关键词搜索图片
     * @param keywords 关键词
     * @return 图片URL
     */
    String searchImage(String keywords);

    /**
     * 获取图像搜索方法
     * @return 图像搜索方法枚举
     */
    ImageMethodEnum getMethod();

    /**
     * 获取备用图片
     * @param position 位置
     * @return 图片URL
     */
    String getFallbackImage(int position);
}
