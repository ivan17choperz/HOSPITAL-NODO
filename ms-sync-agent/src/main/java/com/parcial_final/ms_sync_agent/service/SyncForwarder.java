package com.parcial_final.ms_sync_agent.service;

import com.parcial_final.ms_sync_agent.dto.SyncEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Slf4j
public class SyncForwarder {

    private final RestTemplate restTemplate;
    private final List<String> peerNodes;

    /** Counts total events successfully forwarded (used by /sync/status). */
    private final AtomicLong forwardedCount = new AtomicLong(0);

    public SyncForwarder(
            RestTemplate restTemplate,
            @Value("${ms-services.peer-nodes:}") String peerNodesRaw) {
        this.restTemplate = restTemplate;
        this.peerNodes = Arrays.stream(peerNodesRaw.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();
        log.info("[SYNC] Peer nodes configured: {}", this.peerNodes);
    }

    public void forward(SyncEvent syncEvent) {
        for (String peer : peerNodes) {
            try {
                restTemplate.postForEntity(peer + "/events", syncEvent, Void.class);
                forwardedCount.incrementAndGet();
                log.info("[SYNC] Forwarded {} to {}", syncEvent.getEventType(), peer);
            } catch (Exception e) {
                log.warn("[SYNC] Failed to forward event {} to {}: {}", syncEvent.getEventId(), peer, e.getMessage());
            }
        }
    }

    public List<String> getPeerNodes() {
        return peerNodes;
    }

    public long getForwardedCount() {
        return forwardedCount.get();
    }

    public Map<String, String> getPeerStatuses() {
        Map<String, String> statuses = new LinkedHashMap<>();
        for (String peer : peerNodes) {
            try {
                restTemplate.getForEntity(peer + "/actuator/health", String.class);
                statuses.put(peer, "UP");
            } catch (Exception e) {
                log.warn("[SYNC] Peer {} health check failed: {}", peer, e.getMessage());
                statuses.put(peer, "DOWN");
            }
        }
        return statuses;
    }
}
