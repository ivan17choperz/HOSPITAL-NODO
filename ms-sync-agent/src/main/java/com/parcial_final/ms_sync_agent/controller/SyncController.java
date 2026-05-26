package com.parcial_final.ms_sync_agent.controller;

import com.parcial_final.ms_sync_agent.config.RabbitMQConfig;
import com.parcial_final.ms_sync_agent.dto.SyncEvent;
import com.parcial_final.ms_sync_agent.service.SyncForwarder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("")
@RequiredArgsConstructor
@Slf4j
public class SyncController {

    private final RabbitTemplate rabbitTemplate;
    private final StringRedisTemplate redisTemplate;
    private final SyncForwarder syncForwarder;

    @Value("${node.id:node1}")
    private String nodeId;

    /**
     * Receives a SyncEvent from a peer node and republishes it to the local
     * RabbitMQ exchange so that local microservices (EMR, FHIR, Lab) can process it.
     * The X-Sync-Source header prevents SyncEventListener from re-forwarding it.
     */
    @PostMapping("/events")
    public ResponseEntity<Void> receiveEvent(@RequestBody SyncEvent syncEvent) {
        String redisKey = "sync:processed:" + syncEvent.getEventId();

        if (Boolean.TRUE.equals(redisTemplate.hasKey(redisKey))) {
            log.debug("[SYNC] Duplicate event {} ignored (already processed)", syncEvent.getEventId());
            return ResponseEntity.ok().build();
        }

        redisTemplate.opsForValue().set(redisKey, syncEvent.getSourceNode(), Duration.ofMinutes(5));

        MessageProperties props = new MessageProperties();
        props.setHeader("__TypeId__", syncEvent.getTypeId());
        props.setHeader("X-Sync-Source", syncEvent.getSourceNode());
        props.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        byte[] body = syncEvent.getPayloadJson().getBytes(StandardCharsets.UTF_8);

        rabbitTemplate.send(RabbitMQConfig.EXCHANGE, syncEvent.getEventType(), new Message(body, props));

        log.info("[SYNC] Received {} from node {} → republished to local exchange", syncEvent.getEventType(), syncEvent.getSourceNode());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status() {
        return ResponseEntity.ok(Map.of(
                "nodeId", nodeId,
                "peers", syncForwarder.getPeerNodes(),
                "eventsForwarded", syncForwarder.getForwardedCount(),
                "status", "UP"
        ));
    }

    @GetMapping("/status/peers")
    public ResponseEntity<Map<String, String>> peerStatus() {
        return ResponseEntity.ok(syncForwarder.getPeerStatuses());
    }
}
