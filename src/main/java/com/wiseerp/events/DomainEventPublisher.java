package com.wiseerp.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class DomainEventPublisher {
    private static final Logger log = LoggerFactory.getLogger(DomainEventPublisher.class);

    @org.springframework.beans.factory.annotation.Autowired
    private ApplicationEventPublisher publisher;

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private KafkaTemplate<String, Object> kafkaTemplate;

    public void publish(Object event) {
        try {
            publisher.publishEvent(event);
        } catch (Exception ex) {
            log.warn("Failed to publish spring event: {}", ex.getMessage());
        }
        if (kafkaTemplate != null) {
            try {
                kafkaTemplate.send("domain-events", event);
            } catch (Exception ex) {
                log.warn("Failed to send domain event to kafka: {}", ex.getMessage());
            }
        }
    }
}
