package com.shea.aipassagecreator.service;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.http.HttpProtocol;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.region.Region;
import com.shea.aipassagecreator.config.CosConfig;
import com.shea.aipassagecreator.domain.dto.ImageData;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.UUID;

/**
 * COS服务
 * @author : Shea.
 * @since : 2026/5/20 19:39
 */
@Service
@Slf4j
public class CosService {

    @Resource
    private CosConfig cosConfig;
    private COSClient cosClient;
    private final OkHttpClient client = new OkHttpClient();

    @PostConstruct
    public void init() {
        COSCredentials cred = new BasicCOSCredentials(cosConfig.getSecretId(), cosConfig.getSecretKey());
        Region region = new Region(cosConfig.getRegion());
        ClientConfig clientConfig = new ClientConfig(region);
        clientConfig.setHttpProtocol(HttpProtocol.https);
        cosClient = new COSClient(cred, clientConfig);
    }

    /**
     * 上传ImageData到COS
     * @param imageData ImageData
     * @param folder 上传到COS的目录
     * @return 上传后的图片URL
     */
    public String uploadImageData(ImageData imageData, String folder) {
        if (imageData == null || !imageData.isValid()) {
            log.warn("ImageData无效，无法上传");
            return null;
        }
        try {
            return switch (imageData.getDataType()) {
                case BYTES -> uploadBytes(imageData.getBytes(),imageData.getMimeType(),folder);
                case URL -> uploadFromUrl(imageData.getUrl(),folder);
                case DATA_URL -> uploadFromDataUrl(imageData,folder);
            };
        }catch (Exception e) {
            log.error("上传ImageData到COS失败，dataType={}",imageData.getDataType(),e);
            return null;
        }
    }

    /**
     * 直接使用URL，不上传到COS
     * @param imageUrl 图片URL
     * @return 图片URL
     */
    public String useDirectUrl(String imageUrl) {
        return imageUrl;
    }

    /**
     * 从Data URL上传图片到COS
     * @param imageData ImageData
     * @param folder 上传到COS的目录
     * @return 上传后的图片URL
     */
    private String uploadFromDataUrl(ImageData imageData, String folder) {
        byte[] bytes = imageData.getBytes();
        if (bytes == null || bytes.length == 0) {
            log.warn("ImageData无效，无法上传");
            return null;
        }
        try {
            return uploadBytes(bytes, imageData.getMimeType(), folder);
        } catch (Exception e) {
            log.error("上传Data URL到COS失败，url={}", imageData.getUrl(), e);
            return null;
        }
    }

    /**
     * 从URL上传图片到COS
     * @param url 图片URL
     * @param folder 上传到COS的目录
     * @return 上传后的图片URL
     */
    private String uploadFromUrl(String url, String folder) {
        if (url == null || url.isEmpty()) {
            log.warn("图片URL为空，无法上传");
            return null;
        }
        try {
            Request request = new Request.Builder().url(url).build();
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    log.error("下载图片失败：{},code={}",url, response.code());
                    return null;
                }
                byte[] bytes = response.body().bytes();
                String contentType = response.header("Content-Type", "image/jpeg");
                return uploadBytes(bytes,contentType,folder);
            }
        }catch (Exception e) {
            log.error("从URL上传图片到COS失败：{}", url, e);
            return null;
        }
    }

    /**
     * 上传字节数据到COS
     * @param bytes 字节数据
     * @param mimeType MIME类型
     * @param folder 上传到COS的目录
     * @return 上传后的图片URL
     */
    private String uploadBytes(byte[] bytes, String mimeType, String folder) {
        if (bytes == null || bytes.length == 0) {
            log.warn("字节数据为空，无法上传");
            return null;
        }
        try {
            String extension = getExtensionFromMimeType(mimeType);
            String fileName = folder + "/" + UUID.randomUUID() + extension;
            try (InputStream inputStream = new ByteArrayInputStream(bytes)) {
                ObjectMetadata objectMetadata = new ObjectMetadata();
                objectMetadata.setContentLength(bytes.length);
                objectMetadata.setContentType(mimeType != null ? mimeType : "image/png");
                PutObjectRequest putObjectRequest = new PutObjectRequest(cosConfig.getBucket(), fileName, inputStream, objectMetadata);
                cosClient.putObject(putObjectRequest);
                String cosUrl = buildCosUrl(fileName);
                log.info("字节数据上传成功，size={}bytes,url={}",bytes.length,cosUrl);
                return cosUrl;
            }
        }catch (Exception e) {
            log.error("上传字节数据到cos失败", e);
            return null;
        }
    }

    /**
     * 构建COS URL
     * @param fileName 文件名
     * @return URL
     */
    private String buildCosUrl(String fileName) {
        return String.format("https://%s.cos.%s.myqcloud.com/%s",
                cosConfig.getBucket(), cosConfig.getRegion(), fileName);
    }

    /**
     * 根据MIME类型获取扩展名
     * @param mimeType MIME类型
     * @return 扩展名
     */
    private String getExtensionFromMimeType(String mimeType) {
        if (mimeType == null) {
            return ".png";
        }
        return switch (mimeType.toLowerCase()) {
            case "image/jpeg", "image/jpg" -> ".jpg";
            case "image/gif" -> ".gif";
            case "image/webp" -> ".webp";
            case "image/svg+xml" -> ".svg";
            default -> ".png";
        };
    }

    /**
     * 根据扩展名获取 Content-Type
     */
    private String getContentType(String extension) {
        return switch (extension.toLowerCase()) {
            case ".svg" -> "image/svg+xml";
            case ".png" -> "image/png";
            case ".jpg", ".jpeg" -> "image/jpeg";
            case ".gif" -> "image/gif";
            case ".webp" -> "image/webp";
            case ".pdf" -> "application/pdf";
            default -> "application/octet-stream";
        };
    }

}
