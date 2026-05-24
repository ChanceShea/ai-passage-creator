package com.shea.aipassagecreator.service;

import com.shea.aipassagecreator.domain.dto.ImageDTO;
import com.shea.aipassagecreator.domain.dto.ImageData;
import com.shea.aipassagecreator.enums.ImageMethodEnum;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * 图像搜索服务策略
 * @author : Shea.
 * @since : 2026/5/24 10:59
 */
@Slf4j
@Service
public class ImageServiceStrategy {

    @Resource
    private List<IImageSearchService> imageSearchServices;

    @Resource
    private CosService cosService;

    private final Map<ImageMethodEnum,IImageSearchService> serviceMap = new EnumMap<>(ImageMethodEnum.class);

    @PostConstruct
    public void init() {
        for (IImageSearchService imageSearchService : imageSearchServices) {
            ImageMethodEnum method = imageSearchService.getMethod();
            serviceMap.put(method, imageSearchService);
            log.info("注册图片服务：{} -> {} (AI生图：{}，降级：{})",
                    method.getValue(),imageSearchService.getClass().getSimpleName(),
                    method.isAiGenerated(), method.isFallback());
        }
    }

    public ImageResult getImageAndUpload(String imageSource, ImageDTO dto) {
        ImageMethodEnum method = resolveMethod(imageSource);
        IImageSearchService service = serviceMap.get(method);
        if (service == null || !service.isAvailable()) {
            log.warn("图片服务不可用：{},尝试降级", method);
            return handleFallbackWithUpload(dto.getPosition());
        }
        try {
            // 获取图片数据
            ImageData imageData = service.getImageData(dto);
            if (imageData == null || !imageData.isValid()) {
                log.warn("图片数据获取失败，使用降级方案，method={}",method);
                return handleFallbackWithUpload(dto.getPosition());
            }
            // 上传到COS
            String folder = getFolderForMethod(method);
            String cosUrl = cosService.uploadImageData(imageData, folder);
            if (cosUrl != null && !cosUrl.isEmpty()) {
                log.info("图片获取并上传成功，method={},cosUrl={}",method,cosUrl);
                return new ImageResult(cosUrl, method);
            } else {
                log.warn("图片上传COS失败，使用降级方案，method={}",method);
                return handleFallbackWithUpload(dto.getPosition());
            }
        }catch (Exception e) {
            log.error("获取图片并上传异常：method={}, {}", method, e.getMessage());
            return handleFallbackWithUpload(dto.getPosition());
        }
    }

    /**
     * 处理图片降级并上传
     * @param position 降级图片位置
     * @return 图片结果
     */
    private ImageResult handleFallbackWithUpload(Integer position) {
        int pos = position != null ? position : 1;
        String fallbackUrl = getFallbackImage(pos);

        // 将降级图片上传到COS
        ImageData fallbackData = ImageData.fromUrl(fallbackUrl);
        String cosUrl = cosService.uploadImageData(fallbackData, "fallback");
        // cos上传失败，使用原始URL
        String finalUrl = (cosUrl != null && !cosUrl.isEmpty()) ? cosUrl : fallbackUrl;
        return new ImageResult(finalUrl, ImageMethodEnum.getFallbackMethod());
    }

    /**
     * 获取降级图片URL
     * @param pos 图片位置
     * @return 图片URL
     */
    private String getFallbackImage(int pos) {
        IImageSearchService defaultService = serviceMap.get(ImageMethodEnum.getDefaultSearchMethod());
        if (defaultService != null) {
            return defaultService.getFallbackImage(pos);
        }
        return String.format("https://picsum.photos/800/600?random=%d",pos);
    }

    /**
     * 根据图片处理方法获取对应的COS文件夹
     * @param method 图片处理方法
     * @return COS文件夹名称
     */
    private String getFolderForMethod(ImageMethodEnum method) {
        return switch (method) {
            case PEXELS -> "pexels";
            case NANO_BANANA -> "nano-banana";
            case MERMAID -> "mermaid";
            case ICONIFY -> "iconify";
            case EMOJI_PACK -> "emoji-pack";
            case SVG_DIAGRAM -> "svg-diagram";
            case PICSUM -> "picsum";
        };
    }

    /**
     * 处理图片来源
     * @param imageSource 图片来源
     * @return 图片处理方法
     */
    private ImageMethodEnum resolveMethod(String imageSource) {
        ImageMethodEnum method = ImageMethodEnum.getByValue(imageSource);
        if (method == null) {
            log.warn("未知的图片来源：{},默认使用{}",imageSource,ImageMethodEnum.getDefaultSearchMethod());
            return ImageMethodEnum.getDefaultSearchMethod();
        }
        return method;
    }

    /**
     * 根据图片处理方法获取对应的图片服务
     * @param method 图片处理方法
     * @return 图片服务
     */
    public IImageSearchService getService(ImageMethodEnum method) {
        return serviceMap.get(method);
    }

    /**
     * 获取所有注册的图片处理方法
     * @return 图片处理方法列表
     */
    public List<ImageMethodEnum> getRegisteredMethods() {
        return List.copyOf(serviceMap.keySet());
    }

    /**
     * 图片结果
     */
    public record ImageResult(String cosUrl, ImageMethodEnum method) {

        public boolean isSuccess() {
            return cosUrl != null && !cosUrl.isEmpty();
        }
    }
}
