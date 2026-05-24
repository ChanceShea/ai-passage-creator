package com.shea.aipassagecreator.service;

import com.shea.aipassagecreator.domain.dto.ImageDTO;
import com.shea.aipassagecreator.domain.dto.ImageData;
import com.shea.aipassagecreator.enums.ImageMethodEnum;

/**
 * 图像搜索服务
 * @author : Shea.
 * @since : 2026/5/20 19:39
 */
public interface IImageSearchService {

    /**
     * 根据图片请求对象获取图片
     * @param dto 图片请求对象
     * @return 图片URL
     */
    default String getImage(ImageDTO dto) {
        String param = dto.getEffectiveParam(getMethod().isAiGenerated());
        return searchImage(param);
    }

    /**
     * 根据图片请求对象获取图片数据，用于上传到COS
     * @param dto 图片请求对象
     * @return 图片数据
     */
    default ImageData getImageData(ImageDTO dto) {
        String url = getImage(dto);
        return ImageData.fromUrl(url);
    }

    /**
     * 获取服务是否可用
     * @return true表示可用，false表示不可用
     */
    default boolean isAvailable() {
        return true;
    }

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
