package com.star.aiagent.tools;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class FileOperationToolTest {

    @Test
    public void testReadFile() {
        FileOperationTool tool = new FileOperationTool();
        String fileName = "武汉武昌区吃饭路径规划.txt";
        String result = tool.readFile(fileName);
        assertNotNull(result);
    }

    @Test
    public void testWriteFile() {
        FileOperationTool tool = new FileOperationTool();
        String fileName = "武汉武昌区吃饭路径规划.txt";
        String content = "### 傍晚：晚餐约会（18:30-20:30）\n" +
                "**半秋山西餐厅** - 浪漫西餐厅\n" +
                "- 地址：武汉市武昌区中南路29号（近地铁中南路站）\n" +
                "- 交通：从东湖乘坐公交402路至中南路站，约30分钟车程；或打车前往约15分钟\n" +
                "- 特色：武汉知名连锁西餐厅，环境优雅浪漫，氛围温馨，适合情侣约会\n" +
                "- 推荐菜品：\n" +
                "  - **菲力牛排**：肉质鲜嫩，口感极佳\n" +
                "  - **榴莲披萨**：香味浓郁，芝士丰富\n" +
                "  - **黑松露意面**：口感醇厚，风味独特\n" +
                "  - **水果沙拉**：新鲜清爽，解腻开胃\n" +
                "  - **招牌甜品**：可以尝试他们的提拉米苏或芝士蛋糕\n" +
                "- 用餐环境：安静舒适，灯光柔和，有私密的二人座位\n" +
                "- 人均消费：约80-100元/人";
        String result = tool.writeFile(fileName, content);
        assertNotNull(result);
    }
}