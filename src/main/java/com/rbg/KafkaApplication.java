package com.rbg;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.KafkaListener;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@SpringBootApplication
public class KafkaApplication implements CommandLineRunner {


    private static HazelcastInstance hazelInstance = Hazelcast.newHazelcastInstance();
    public static Logger logger = LoggerFactory.getLogger(KafkaApplication.class);
    private ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    // private static Set<String> eventSet = new HashSet<>();

    public static void main(String[] args) {
        SpringApplication.run(KafkaApplication.class, args).close();
    }

    @Override
    public void run(String... args) throws Exception {

        while(true){

           Map<String, Set<String>> eventCountMap = hazelInstance.getMap("eventCountMap");
           Map<String, Integer> summaryCountMap = hazelInstance.getMap("summaryCountMap");
           Map<String, LocalDateTime> eventTimeStamp = hazelInstance.getMap("eventTimeStamp");
           
           // loop through eventsTimestamps to check if summary event has been received 
           try {
               for (String eventKey : eventTimeStamp.keySet()) {
                   if (summaryCountMap.get(eventKey) == null && Duration.between(eventTimeStamp.get(eventKey), LocalDateTime.now()).getSeconds() > 5) {
                       logger.info("{} is not balanced. Summary event is missing for more than 5 seconds! Total Events received={}", eventKey, eventCountMap.get(eventKey).size());
                       eventTimeStamp.remove(eventKey);
                   }
               }
           } catch (NullPointerException ex) {
               logger.info("Oops, Key already deleted by another instance");
           }

            // loop through summary to check if events have balanced.
            try {
                for (String key : summaryCountMap.keySet()) {
                    // check if counts are matched. Print Log, have a beer.
                    if (eventCountMap.get(key) != null && eventCountMap.get(key).size() == summaryCountMap.get(key)) {
                        logger.info("{} is balanced. Total Events received={}", key, eventCountMap.get(key).size());
                        eventCountMap.remove(key);
                        summaryCountMap.remove(key);
                        eventTimeStamp.remove(key);
                    }
                    // check if counts are mismatched for more than 5 seconds - Panic.ICEBERG AHEAD!!!
                    else if (eventTimeStamp.get(key) != null && Duration.between(eventTimeStamp.get(key), LocalDateTime.now()).getSeconds() > 5) {
                        if (eventCountMap.get(key) == null) {
                            logger.info("{} is not balanced. Raising ALERT!!!  Total Events expected={} but none were received", key, summaryCountMap.get(key));
                        } else {
                            logger.info("{} is not balanced. Raising ALERT!!!  Total Events received={} expected={}", key, eventCountMap.get(key).size(), summaryCountMap.get(key));
                        }
                        eventTimeStamp.remove(key);
                    }
                }
            } catch (NullPointerException ex){
                logger.info("Key already deleted by another instance");
            }
       }
    }

    @KafkaListener(topics = "${topic.events}")
    public void listenEvents(ConsumerRecord<String, String> consumerRecord) throws Exception {

        Map<String, Set<String>> eventCountMap = hazelInstance.getMap("eventCountMap");
        Map<String, LocalDateTime> eventTimeStamp = hazelInstance.getMap("eventTimeStamp");
        EventRequest request = mapper.readValue(consumerRecord.value(),EventRequest.class);
        eventTimeStamp.put(request.getCorrelationId(),LocalDateTime.now());
        Set<String> eventSet;

        if (eventCountMap.get(request.getCorrelationId()) == null) {
            eventSet = new HashSet<>();
        } else {
            eventSet = eventCountMap.get(request.getCorrelationId());
        }
        eventSet.add(request.getEventId());
        eventCountMap.put(request.getCorrelationId(),eventSet);
        logger.info(request.toString());

    }

    @KafkaListener(topics = "${topic.eventsSummary}")
    public void listenSummary(ConsumerRecord<String, String> consumerRecord) throws Exception {
        Map<String, LocalDateTime> eventTimeStamp = hazelInstance.getMap("eventTimeStamp");
        Map<String, Integer> summaryCountMap = hazelInstance.getMap("summaryCountMap");
        SummaryEvent summaryEvent = mapper.readValue(consumerRecord.value(),SummaryEvent.class);
        eventTimeStamp.put(summaryEvent.getCorrelationId(),LocalDateTime.now());
        summaryCountMap.put(summaryEvent.getCorrelationId(),summaryEvent.getTotalEvents());
        logger.info(consumerRecord.toString());
    }
}
