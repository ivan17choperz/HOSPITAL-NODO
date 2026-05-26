package com.parcial_final.ms_sync_agent.messaging;

import com.parcial_final.ms_sync_agent.config.RabbitMQConfig;
import com.parcial_final.ms_sync_agent.dto.SyncEvent;
import com.parcial_final.ms_sync_agent.service.SyncForwarder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class SyncEventListener {

    private final SyncForwarder syncForwarder;
    private final StringRedisTemplate redisTemplate;

    @Value("${node.id:node1}")
    private String nodeId;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_SYNC_ALL)
    public void onLocalEvent(Message message) {
        String syncSource = (String) message.getMessageProperties().getHeader("X-Sync-Source");
        if (syncSource != null) {
            log.debug("[SYNC] Skipping re-forward of event from node {}", syncSource);
            return;
        }

        String routingKey = message.getMessageProperties().getReceivedRoutingKey();
        String typeId = (String) message.getMessageProperties().getHeader("__TypeId__");
        String payloadJson = new String(message.getBody(), StandardCharsets.UTF_8);
        String eventId = UUID.randomUUID().toString();

        String redisKey = "sync:processed:" + eventId;
        redisTemplate.opsForValue().set(redisKey, nodeId, Duration.ofMinutes(5));

        SyncEvent syncEvent = SyncEvent.builder()
                .eventId(eventId)
                .eventType(routingKey)
                .sourceNode(nodeId)
                .payloadJson(payloadJson)
                .typeId(typeId)
                .timestamp(LocalDateTime.now().toString())
                .build();

        log.info("[SYNC] Local event captured: type={}, node={}, eventId={}", routingKey, nodeId, eventId);
        syncForwarder.forward(syncEvent);
    }
}
