package com.star.aiagent.app;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

@SpringBootTest
class EatAppTest {

    @Resource
    private EatApp eatApp;

    @Test
    void testChat() {
        String chatId = UUID.randomUUID().toString();
        // 第一轮
        String message = "你好，我是丁真";
        String answer = eatApp.doChat(message, chatId);
        Assertions.assertNotNull(answer);
        // 第二轮
        message = "我想问问今天晚上吃什么好";
        answer = eatApp.doChat(message, chatId);
        Assertions.assertNotNull(answer);
        // 第三轮
        message = "你刚才推荐我吃什么来着，但是我现在刚运动完，吃不下去，有没有更推荐的";
        answer = eatApp.doChat(message, chatId);
        Assertions.assertNotNull(answer);
    }

    @Test
    void doChatWithReport() {
        String chatId = UUID.randomUUID().toString();
        // 第一轮
        String message = "你好，我是kuler，我今天晚上不知道吃什么，你有什么推荐吗";
        EatApp.EatReport eatReport = eatApp.doChatWithReport(message, chatId);
        Assertions.assertNotNull(eatReport);
    }

}
