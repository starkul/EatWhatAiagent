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

    @Test
    void doChatWithRag() {
        String chatId = UUID.randomUUID().toString();
        String message = "学生早饭应该吃什么？";
        String answer =  eatApp.doChatWithRag(message, chatId);
        Assertions.assertNotNull(answer);
    }


    @Test
    void doChatWithTools() {
        // 测试联网搜索问题的答案
        testMessage("周末想带女朋友去武汉聚餐，推荐几个适合情侣的吃饭地点？");

        // 测试网页抓取：恋爱案例分析
        testMessage("这几天不知道吃什么，看看美食网站（https://www.meishichina.com/）有没有什么推荐饭菜呢？");

        // 测试资源下载：图片下载
        testMessage("直接下载一张https://ts1.tc.mm.bing.net/th/id/R-C.b3a7697d2793ba094a861d546c31190d?rik=NevOIW4XmkUuMA&riu=http%3a%2f%2fseopic.699pic.com%2fphoto%2f50069%2f5445.jpg_wh1200.jpg&ehk=wuLPicg%2b9wXz8QAwp%2fAVFBtJQ6loBUiVfQZu2bbZODA%3d&risl=&pid=ImgRaw&r=0的美食图片为文件");

        // 测试终端操作：执行代码
        testMessage("执行 Python3 脚本来生成美食分析报告");

        // 测试文件操作：保存用户档案
        testMessage("保存我的美食食谱为文件");

        // 测试 PDF 生成
        testMessage("生成一份‘今天你吃了吗’PDF，包含餐厅预订、美食推荐和口味偏好");
    }

    private void testMessage(String message) {
        String chatId = UUID.randomUUID().toString();
        String answer = eatApp.doChatWithTools(message, chatId);
        Assertions.assertNotNull(answer);
    }

}
