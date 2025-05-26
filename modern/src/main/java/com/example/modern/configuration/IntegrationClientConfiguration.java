package com.example.modern.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.ip.dsl.Tcp;
import org.springframework.integration.ip.tcp.connection.AbstractClientConnectionFactory;
import org.springframework.integration.ip.tcp.connection.TcpNetClientConnectionFactory;
import org.springframework.integration.ip.tcp.serializer.ByteArrayLengthHeaderSerializer;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

import java.nio.charset.StandardCharsets;

@Configuration
public class IntegrationClientConfiguration {

    @Value("${legacy.host}")
    private String host;

    @Value("${legacy.port}")
    private int port;

    @Bean
    public MessageChannel fromLegacyChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageChannel toLegacyChannel() {
        return new DirectChannel();
    }

    @Bean
    public AbstractClientConnectionFactory clientConnectionFactory() {
        TcpNetClientConnectionFactory factory = new TcpNetClientConnectionFactory(host, port);
        factory.setSerializer(new ByteArrayLengthHeaderSerializer());
        factory.setDeserializer(new ByteArrayLengthHeaderSerializer());
        return factory;
    }

    @Bean
    public IntegrationFlow outboundFlow(AbstractClientConnectionFactory connectionFactory) {
        return IntegrationFlow
                .from("toLegacyChannel")
                .handle(Tcp.outboundGateway(connectionFactory))
                .channel("fromLegacyChannel")
                .get();
    }

    @Bean
    @ServiceActivator(inputChannel = "fromLegacyChannel")
    public MessageHandler clientReplyHandler() {
        return message -> {
            byte[] payload = (byte[]) message.getPayload();
            String replyJson = new String(payload, StandardCharsets.UTF_8).trim();
            System.out.println("Modern client received ack: " + replyJson);
        };
    }
}
