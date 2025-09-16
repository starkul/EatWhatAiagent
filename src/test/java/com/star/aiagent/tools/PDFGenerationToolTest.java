package com.star.aiagent.tools;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class PDFGenerationToolTest {

    @Test
    public void testGeneratePDF() {
        PDFGenerationTool tool = new PDFGenerationTool();
        String fileName = "今天晚上吃什么.pdf";
        String content = "澎湖湾的鱼香肉丝";
        String result = tool.generatePDF(fileName, content);
        assertNotNull(result);
    }
}
