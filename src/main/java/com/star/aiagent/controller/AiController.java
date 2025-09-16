package com.star.aiagent.controller;

import com.star.aiagent.agent.model.OpenManus;
import com.star.aiagent.app.EatApp;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;

@RestController
@RequestMapping("/ai")
public class AiController {

    @Resource
    private EatApp eatApp;

    @Resource
    private ToolCallback[] allTools;

    @Resource
    private ChatModel dashscopeChatModel;

    @GetMapping("/eat_app/chat/sync")
    public String doChatWithEatAppSync(String message, String chatId) {

        return eatApp.doChat(message, chatId);
    }

    @GetMapping(value = "/eat_app/chat/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> doChatWithEatAppSSE(String message, String chatId) {
        return eatApp.doChatByStream(message, chatId);
    }

    @GetMapping(value = "/eat_app/chat/sse")
    public Flux<ServerSentEvent<String>> doChatWithEatAppSentEvent(String message, String chatId) {
        return eatApp.doChatByStream(message, chatId)
                .map(chunk -> ServerSentEvent.<String>builder()
                        .data(chunk)
                        .build());
    }

    @GetMapping("/eat_app/chat/sse/emitter")
    public SseEmitter doChatWithEatAppSseEmitter(String message, String chatId) {
        // 创建一个超时时间较长的 SseEmitter
        SseEmitter emitter = new SseEmitter(180000L); // 3分钟超时
        // 获取 Flux 数据流并直接订阅
        eatApp.doChatByStream(message, chatId)
                .subscribe(
                        // 处理每条消息
                        chunk -> {
                            try {
                                emitter.send(chunk);
                            } catch (IOException e) {
                                emitter.completeWithError(e);
                            }
                        },
                        // 处理错误
                        emitter::completeWithError,
                        // 处理完成
                        emitter::complete
                );
        // 返回emitter
        return emitter;
    }

    /**
     * 流式调用 Manus 超级智能体
     *
     * @param message
     * @return
     */
    @GetMapping("/manus/chat")
    public SseEmitter doChatWithManus(String message) {
        OpenManus openManus = new OpenManus(allTools, dashscopeChatModel);
        return openManus.runStream(message);
    }


}
