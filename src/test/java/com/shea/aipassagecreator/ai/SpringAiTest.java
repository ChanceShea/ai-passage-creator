package com.shea.aipassagecreator.ai;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

@SpringBootTest
public class SpringAiTest {

    @Resource
    private ChatModel dashScopeChatModel;

    @Test
    public void test() {
        String resp = dashScopeChatModel.call("Hello, how are you?");
        System.out.println(resp);
    }

    @Test
    public void testStream() {
        Flux<String> stream = dashScopeChatModel.stream("please introduce spring ai with one sentence");
        stream.subscribe(System.out::println);
        stream.blockLast();
    }
}
