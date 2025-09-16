package com.star.aiagent.agent.model;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class OpenManusTest {

    @Resource
    private OpenManus openManus;

    @Test
    void run() {
        String userPrompt = """  
                我的朋友居住在武汉市武昌区广八路，请帮我找到 5 公里内合适的吃饭地点，
                我们不能吃辣的，希望价钱不要太贵，并且环境安静
                并结合一些网络图片，制定一份详细的聚餐计划，  
                并以 PDF 格式输出""";
        String answer = openManus.run(userPrompt);
        Assertions.assertNotNull(answer);
    }
}
