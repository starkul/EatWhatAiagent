package com.star.aiagent.app;

import com.star.aiagent.advisor.MyLoggerAdvisor;
import com.star.aiagent.chatmemory.FileBasedChatMemory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

@Component
@Slf4j
public class EatApp {

    private final ChatClient chatClient;

    record EatReport(String title, List<String> suggestions) {
    }

    private static final String SYSTEM_PROMPT = "扮演专注美食推荐的生活顾问。开场向用户表明身份，告知可帮解决「吃什么」的选择难题。\n" +
            "围绕不同用餐场景与需求提问：日常工作餐场景，询问预算、口味偏好（如麻辣/清淡/酸甜等）、是否需便捷出餐；\n" +
            "朋友聚会场景，了解参与人数、想尝试的菜系（如中餐/西餐/日料等）、有无特别忌口；\n" +
            "情侣约会场景，询问对方喜好风格（浪漫氛围/轻松随性等）、对菜品精致度要求、有没有特别想尝试的料理类型；\n" +
            "家庭用餐场景，了解家庭成员年龄构成、大家普遍能接受的口味、有无需要照顾的饮食需求（如老人牙口、小孩辅食等）。\n" +
            "引导用户详述用餐场景细节、个人及同行人员饮食偏好、对餐食的特殊期待（如想尝试新菜品、追求健康养生等），以便给出专属美食推荐方案。\n" +
            "每次回答尽量简洁";
    /**
     * 初始化AI客户端
     * @param dashscopeChatModel
     */
    public EatApp(ChatModel dashscopeChatModel) {
        // 初始化基于内存的对话记忆
        ChatMemory chatMemory = new InMemoryChatMemory();
        chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(chatMemory),
                        new MyLoggerAdvisor()
                        // 自定义推理增强 Advisor，可按需开启
//                        new ReReadingAdvisor()
                )
                .build();

    }

//    public EatApp(ChatModel dashscopeChatModel) {
//        // 初始化基于文件的对话记忆
//        String fileDir = System.getProperty("user.dir") + "/tmp/chat-memory";
//        ChatMemory chatMemory = new FileBasedChatMemory(fileDir);
//        chatClient = ChatClient.builder(dashscopeChatModel)
//                .defaultAdvisors(
//                        new MessageChatMemoryAdvisor(chatMemory),
//                        new MyLoggerAdvisor()
//                        // 自定义推理增强 Advisor，可按需开启
////                        new ReReadingAdvisor()
//                )
//                .build();
//    }

    /**
     * AI基础对话（多轮记忆）
     * @param message
     * @param chatId
     * @return
     */
    public String doChat(String message, String chatId) {
        ChatResponse response = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .call()
                .chatResponse();
        String content = response.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

    /**
     * AI饮食建议报告功能
     * @param message
     * @param chatId
     * @return
     */
    public EatReport doChatWithReport(String message, String chatId) {
        EatReport eatReport = chatClient
                .prompt()
                .system(SYSTEM_PROMPT + "每次对话后都要生成饮食报告，标题为{用户名}的饮食建议，内容为建议列表")
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .call()
                .entity(EatReport.class);
        log.info("eatReport: {}", eatReport);
        return eatReport;
    }

    @Resource
    private VectorStore eatAppVectorStore;

    public String doChatWithRag(String message, String chatId) {
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                // 开启日志，便于观察效果
                .advisors(new MyLoggerAdvisor())
                // 应用知识库问答
                .advisors(new QuestionAnswerAdvisor(eatAppVectorStore))
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

    /**
     * 工具集合
     */
    @Resource
    private ToolCallback[] allTools;

    /**
     * 工具调用
     * @param message
     * @param chatId
     * @return
     */

    public String doChatWithTools(String message, String chatId) {
        ChatResponse response = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                // 开启日志，便于观察效果
                .advisors(new MyLoggerAdvisor())
                .tools(allTools)
                .call()
                .chatResponse();
        String content = response.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

    @Resource
    private ToolCallbackProvider toolCallbackProvider;

    public String doChatWithMcp(String message, String chatId) {
        ChatResponse response = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                // 开启日志，便于观察效果
                .advisors(new MyLoggerAdvisor())
                .tools(toolCallbackProvider)
                .call()
                .chatResponse();
        String content = response.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

    /**
     * 流式输出
     * @param message
     * @param chatId
     * @return
     */
    public Flux<String> doChatByStream(String message, String chatId) {
        return chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .stream()
                .content();
    }



}
