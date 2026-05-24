package com.shea.aipassagecreator.ai.service;

import com.shea.aipassagecreator.domain.dto.ImageDTO;
import com.shea.aipassagecreator.domain.dto.ImageData;
import com.shea.aipassagecreator.service.impl.NanoBananaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author : Shea.
 * @since : 2026/5/24 15:53
 */
@SpringBootTest
public class NanoBananaTest {

    @Autowired
    private NanoBananaService nanoBananaService;

    @Test
    void test() {
        ImageDTO dto = ImageDTO.builder()
                .prompt("A beautiful landscape painting")
                .build();

        ImageData imageData = nanoBananaService.getImageData(dto);
        System.out.println(imageData);
    }
}
