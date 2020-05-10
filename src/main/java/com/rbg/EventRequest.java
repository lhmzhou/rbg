package com.rbg;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

public class EventRequest {
    private static final Logger LOG = getLogger(EventRequest.class);
    private static ObjectMapper objectMapper = new ObjectMapper();

    @JsonProperty("event_id")
    private String eventId;

    // @JsonProperty("event_publisher_id")
    // private String eventPublisherId;

    @JsonProperty("correlation_id")
    private String correlationId;

    // @JsonProperty("event_type")
    // private String eventType;

    // @JsonProperty("event_type_version")
    // private String eventTypeVersion;

    // @JsonProperty("event_publish_ts")
    // @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    // private LocalDateTime eventPublishTS;

    @JsonProperty("event_data")
    private Object eventData;

    public EventRequest() {
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    @Override
    public String toString() {
        String eventObject;
        try {
            eventObject = objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            LOG.error("EventRequest.toString() error: ", e);
            eventObject = "";
        }
        return eventObject;
    }

}
