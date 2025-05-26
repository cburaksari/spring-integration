package com.example.modern.controller;

import com.example.modern.model.Message;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageChannel;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/message")
@RequiredArgsConstructor
@Slf4j
public class MessageController {

    private final MessageChannel toLegacyChannel;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping("/send")
    public void send(@RequestBody Message message) {
        try {
            String json = objectMapper.writeValueAsString(message);
            byte[] bytesWithDelimiter = (json + "\n").getBytes(StandardCharsets.UTF_8);
            org.springframework.messaging.Message<byte[]> built = MessageBuilder.withPayload(bytesWithDelimiter).build();
            toLegacyChannel.send(built);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
        }
    }
}