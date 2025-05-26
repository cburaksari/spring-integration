package com.example.legacy.configuration;

import com.example.legacy.model.Message;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.ip.tcp.TcpInboundGateway;
import org.springframework.integration.ip.tcp.connection.AbstractServerConnectionFactory;
import org.springframework.integration.ip.tcp.connection.TcpNetServerConnectionFactory;
import org.springframework.integration.ip.tcp.serializer.ByteArrayLengthHeaderSerializer;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Configuration
@Slf4j
public class IntegrationServerConfiguration {

    public static final String MESSAGE_RECEIVED = "Message received\n";

    @Bean
    public AbstractServerConnectionFactory serverConnectionFactory() {
        TcpNetServerConnectionFactory factory = new TcpNetServerConnectionFactory(9091);
        factory.setSerializer(new ByteArrayLengthHeaderSerializer());
        factory.setDeserializer(new ByteArrayLengthHeaderSerializer());
        return factory;
    }

    @Bean
    public TcpInboundGateway inboundGateway(AbstractServerConnectionFactory factory) {
        TcpInboundGateway gateway = new TcpInboundGateway();
        gateway.setConnectionFactory(factory);
        gateway.setRequestChannel(legacyInputChannel());
        gateway.setReplyChannel(legacyOutputChannel());
        return gateway;
    }

    @Bean
    @ServiceActivator(inputChannel = "legacyInputChannel")
    public MessageHandler handleIncomingMessage() {
        return message -> {
            String json = new String((byte[]) message.getPayload(), StandardCharsets.UTF_8);
            try {
                processReceivedMessage(json);
                sendAnAcknowledgement(message);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        };
    }

    private static void sendAnAcknowledgement(org.springframework.messaging.Message<?> message) {
        byte[] responseBytes = MESSAGE_RECEIVED.getBytes(StandardCharsets.UTF_8);
        org.springframework.messaging.Message<byte[]> messageReceived = MessageBuilder.withPayload(responseBytes).build();
        MessageChannel replyChannel = (MessageChannel) Objects.requireNonNull(message.getHeaders().getReplyChannel());
        replyChannel.send(messageReceived);
    }

    private static void processReceivedMessage(String json) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        log.info("Received json:{}", json);
        Message clientMessage = mapper.readValue(json, Message.class);
        log.info("Received message from legacy input channel: {}", clientMessage);
    }

    @Bean
    public MessageChannel legacyOutputChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageChannel legacyInputChannel() {
        return new DirectChannel();
    }
}
