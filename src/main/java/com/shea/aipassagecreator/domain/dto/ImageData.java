package com.shea.aipassagecreator.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Base64;

/**
 * @author : Shea.
 * @since : 2026/5/20 21:49
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ImageData {

    /**
     * 图片字节数据
     */
    private byte[] bytes;

    /**
     * 图片url
     */
    private String url;

    /**
     * MIME 类型(image/png,image/jpeg,image/svg+xml)
     */
    private String mimeType;

    /**
     * 图片数据类型
     */
    private DataType dataType;

    public enum DataType {
        BYTES,URL,DATA_URL
    }

    /**
     * 从外部URL创建ImageData
     * @param url 图片url
     * @return ImageData
     */
    public static ImageData fromUrl(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }

        // 判断是否为base64 data URL
        if (url.startsWith("data:")) {
            return fromDataUrl(url);
        }
        return ImageData.builder()
                .url(url)
                .dataType(DataType.URL)
                .build();
    }

    /**
     * 从Base64 Data URL创建ImageData
     * @param url data URL
     * @return ImageData
     */
    public static ImageData fromDataUrl(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }

        String mimeType = "image/png";
        int mimeEnd = url.indexOf(";");
        if (mimeEnd > 5) {
            mimeType = url.substring(5,mimeEnd);
        }

        return ImageData.builder()
                .url(url)
                .mimeType(mimeType)
                .dataType(DataType.DATA_URL)
                .build();
    }

    /**
     * 从字节数组创建ImageData
     * @param bytes 图片字节数组
     * @param mimeType 图片MIME类型
     * @return ImageData
     */
    public static ImageData fromBytes(byte[] bytes,String mimeType) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }

        return ImageData.builder()
                .bytes(bytes)
                .mimeType(mimeType != null ? mimeType : "image/png")
                .dataType(DataType.BYTES)
                .build();
    }

    /**
     * 获取图片字节数组
     * @return 图片字节数组
     */
    public byte[] getImageBytes() {
        if (dataType == DataType.BYTES) {
            return bytes;
        }
        if (dataType == DataType.DATA_URL && url != null) {
            int base64Start = url.indexOf(",");
            if (base64Start > 0) {
                String base64Data = url.substring(base64Start + 1);
                return Base64.getDecoder().decode(base64Data);
            }
        }
        return null;
    }

    /**
     * 判断图片数据是否有效
     * @return 是否有效
     */
    public boolean isValid(){
        return switch (dataType) {
            case BYTES -> bytes != null && bytes.length > 0;
            case URL,DATA_URL -> url != null && !url.isEmpty();
        };
    }

    /**
     * 获取图片文件扩展名
     * @return 文件扩展名
     */
    public String getFileExtension() {
        if(mimeType == null) {
            return ".png";
        }
        return switch (mimeType.toLowerCase()) {
            case "image/jpeg","image/jpg" -> ".jpg";
            case "image/gif" -> ".gif";
            case "image/webp" -> ".webp";
            case "image/svg+xml" -> ".svg";
            default -> ".png";
        };
    }
}
