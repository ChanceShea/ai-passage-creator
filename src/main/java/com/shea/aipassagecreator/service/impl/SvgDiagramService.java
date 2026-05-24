package com.shea.aipassagecreator.service.impl;

import cn.hutool.core.util.StrUtil;
import com.shea.aipassagecreator.config.SvgDiagramConfig;
import com.shea.aipassagecreator.constant.PromptConstant;
import com.shea.aipassagecreator.domain.dto.ImageDTO;
import com.shea.aipassagecreator.domain.dto.ImageData;
import com.shea.aipassagecreator.enums.ImageMethodEnum;
import com.shea.aipassagecreator.service.IImageSearchService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

import static com.shea.aipassagecreator.constant.ArticleConstant.PICSUM_URL_TEMPLATE;

/**
 * SVG概念示意图服务实现类
 * @author : Shea.
 * @since : 2026/5/24 19:49
 */
@Service
@Slf4j
public class SvgDiagramService implements IImageSearchService {

    @Resource
    private SvgDiagramConfig svgDiagramConfig;
    @Resource
    private ChatModel dashScopeChatModel;

    @Override
    public String searchImage(String keywords) {
        return null;
    }

    @Override
    public ImageData getImageData(ImageDTO dto) {
        String requirement = dto.getEffectiveParam(true);
        return generateSvgDiagramData(requirement);
    }

    /**
     * 根据要求生成SVG概念示意图
     * @param requirement 要求
     * @return ImageData
     */
    private ImageData generateSvgDiagramData(String requirement) {
        if(StrUtil.isEmpty(requirement)) {
            log.warn("SVG概念示意图生成要求为空,requirement={}", requirement);
            return null;
        }
        try {
            String svgCode = calLlm2GenerateSvg(requirement);
            if (StrUtil.isEmpty(svgCode)) {
                log.error("LLM生成SVG代码为空,requirement={}", requirement);
                return null;
            }
            if (!isValidSvg(svgCode)) {
                log.error("生成的SVG代码无效,requirement={}", requirement);
                return null;
            }
            byte[] bytes = svgCode.getBytes(StandardCharsets.UTF_8);
            log.info("SVG代码生成成功,requirement={},size={} bytes", requirement, bytes.length);
            return ImageData.fromBytes(bytes, "image/svg+xml");
        }catch (Exception e) {
            log.error("SVG概念示意图生成异常,requirement={}", requirement, e);
            return null;
        }
    }

    /**
     * 验证SVG代码是否有效
     * @param svgCode SVG代码
     * @return 是否有效
     */
    private boolean isValidSvg(String svgCode) {
        if (StrUtil.isBlank(svgCode)) {
            return false;
        }
        return svgCode.contains("<svg") && svgCode.contains("</svg>");
    }

    /**
     * 调用LLM生成SVG代码
     * @param requirement 要求
     * @return SVG代码
     */
    private String calLlm2GenerateSvg(String requirement) {
        String prompt = PromptConstant.SVG_DIAGRAM_GENERATION_PROMPT
                .replace("{requirement}", requirement);
        ChatResponse response = dashScopeChatModel.call(new Prompt(new UserMessage(prompt)));
        String svgCode = response.getResult().getOutput().getText().trim();
        svgCode = extractSvgCode(svgCode);
        return svgCode;
    }

    /**
     * 提取SVG代码
     * @param svgCode SVG代码
     * @return 提取后的SVG代码
     */
    private String extractSvgCode(String svgCode) {
        if (svgCode == null || svgCode.isEmpty()) {
            return null;
        }
        // 去除代码块标记
        svgCode = svgCode.replace("```xml","")
                .replace("```svg","")
                .replace("```","")
                .trim();
        // 确保包含XML声明
        if (!svgCode.startsWith("<?xml")) {
            // 如果没有XML声明但包含<svg>标签，则添加XML声明
            if (svgCode.contains("<svg")) {
                svgCode = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + svgCode;
            }
        }
        return svgCode;
    }

    @Override
    public ImageMethodEnum getMethod() {
        return ImageMethodEnum.SVG_DIAGRAM;
    }

    @Override
    public String getFallbackImage(int position) {
        return String.format(PICSUM_URL_TEMPLATE,position);
    }
}
