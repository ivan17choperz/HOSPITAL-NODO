package com.parcial_final.ms_sync_agent.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SyncEvent {

    /** Unique ID used for Redis-based idempotency checks across nodes. */
    private String eventId;

    /** RabbitMQ routing key of the original event (e.g. patient.admitted). */
    private String eventType;

    /** Node that originally produced this event. */
    private String sourceNode;

    /** Raw JSON body of the original RabbitMQ message. */
    private String payloadJson;

    /** Value of the __TypeId__ header set by Jackson2JsonMessageConverter. */
    private String typeId;

    private String timestamp;
}
