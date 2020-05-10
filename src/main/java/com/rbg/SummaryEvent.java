package com.rbg;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

public class SummaryEvent {

    private static ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger LOG = getLogger(SummaryEvent.class);

    @JsonProperty("correlation_id")
    private String correlationId;

    public int getTotalEvents() {
        return totalEvents;
    }

    public void setTotalEvents(int totalEvents) {
        this.totalEvents = totalEvents;
    }

    @JsonProperty("total_events")
    private int totalEvents;

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
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
