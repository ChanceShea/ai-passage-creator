package com.shea.aipassagecreator.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RuntimeUtil;
import cn.hutool.system.SystemUtil;
import com.shea.aipassagecreator.config.MermaidConfig;
import com.shea.aipassagecreator.domain.dto.ImageDTO;
import com.shea.aipassagecreator.domain.dto.ImageData;
import com.shea.aipassagecreator.enums.ImageMethodEnum;
import com.shea.aipassagecreator.service.IImageSearchService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;

import static com.shea.aipassagecreator.constant.ArticleConstant.PICSUM_URL_TEMPLATE;

/**
 * Mermaid图片搜索服务实现类
 * @author : Shea.
 * @since : 2026/5/24 16:11
 */
@Service
@Slf4j
public class MermaidService implements IImageSearchService {

    @Resource
    private MermaidConfig mermaidConfig;

    @Override
    public String searchImage(String keywords) {
        return null;
    }

    @Override
    public String getImage(ImageDTO dto) {
        return null;
    }

    @Override
    public ImageData getImageData(ImageDTO dto) {
        // 优先使用prompt，如果prompt为空则使用keywords
        String mermaidCode = dto.getEffectiveParam(true);
        return generateDiagramData(mermaidCode);
    }

    private ImageData generateDiagramData(String mermaidCode) {
        if (mermaidCode == null || mermaidCode.isEmpty()) {
            log.warn("mermaidCode 为空");
            return null;
        }

        File tempInputFile = null;
        File tempOutputFile = null;
        try {
            // 创建临时输入文件
            tempInputFile = FileUtil.createTempFile("mermaid_input_",".mmd",true);
            FileUtil.writeUtf8String(mermaidCode,tempInputFile);
            // 创建临时输出文件
            String outputExtension = "." + mermaidConfig.getOutputFormat();
            tempOutputFile = FileUtil.createTempFile("mermaid_output_",outputExtension,true);
            // 转换为图片
            convertMermaid2Image(tempInputFile, tempOutputFile);
            if (!tempOutputFile.exists() || tempOutputFile.length() == 0) {
                log.error("Mermaid CLI 执行失败，输出文件不存在或为空");
                return null;
            }
            byte[] imageBytes = FileUtil.readBytes(tempOutputFile);
            String mimeType = getMimeType(mermaidConfig.getOutputFormat());
            log.info("Mermaid图表生成成功，size={}bytes",imageBytes.length);
            return ImageData.fromBytes(imageBytes,mimeType);
        }catch (Exception e) {
            log.error("Mermaid图表生成异常",e);
            return null;
        }finally {
            FileUtil.del(tempInputFile);
            FileUtil.del(tempOutputFile);
        }
    }

    /**
     * 根据输出格式获取MIME类型
     * @param outputFormat 输出格式
     * @return MIME类型
     */
    private String getMimeType(String outputFormat) {
        return switch (outputFormat) {
            case "pdf" -> "application/pdf";
            case "svg" -> "image/svg+xml";
            default -> "image/png";
        };
    }

    /**
     * 将Mermaid代码转换为图片
     * @param inputFile 临时输入文件
     * @param outputFile 临时输出文件
     */
    private void convertMermaid2Image(File inputFile, File outputFile) {
        try {
            String command = SystemUtil.getOsInfo().isWindows() ? "mmdc.cmd" : mermaidConfig.getCliCommand();
            String cmdLine = String.format("%s -i %s -o %s -b %s",
                    command,
                    inputFile.getAbsolutePath(),
                    outputFile.getAbsolutePath(),
                    mermaidConfig.getBackgroundColor()
                    );
            if (mermaidConfig.getWidth() != null && mermaidConfig.getWidth() > 0) {
                cmdLine += " -w " + mermaidConfig.getWidth();
            }
            log.info("执行Mermaid CLI命令, {}", cmdLine);
            String result = RuntimeUtil.execForStr(cmdLine);
            log.debug("Mermaid CLI 执行结果: {}", result);
        } catch (Exception e) {
            log.error("Mermaid CLI 执行失败", e);
            throw new RuntimeException("Mermaid CLI执行失败：" + e.getMessage(),e);
        }
    }

    /**
     * 检查Mermaid CLI是否可用
     * @return 是否可用
     */
    @Override
    public boolean isAvailable() {
        try {
            String command = SystemUtil.getOsInfo().isWindows() ? "mmdc.cmd" : mermaidConfig.getCliCommand();
            String checkCmd = command + " --version";
            String version = RuntimeUtil.execForStr(checkCmd);
            log.info("Mermaid CLI 版本: {}", version);
            return version != null && !version.isEmpty();
        }catch (Exception e) {
            log.warn("Mermaid CLI 不可用:{}", e.getMessage());
            return false;
        }
    }

    /**
     * 获取图片生成方法
     * @return 图片生成方法
     */
    @Override
    public ImageMethodEnum getMethod() {
        return ImageMethodEnum.MERMAID;
    }

    /**
     * 获取降级图片URL
     * @param position 位置
     * @return 回退图片URL
     */
    @Override
    public String getFallbackImage(int position) {
        return String.format(PICSUM_URL_TEMPLATE,position);
    }
}
