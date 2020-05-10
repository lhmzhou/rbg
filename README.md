# rbg  

`rbg` listens on Kafka topics and numerate unique events published on active topics. Topics can maintain zero to several consumers that subscribe to its events. `rbg`'s two designated Kafka topics:
1. events
2. eventsSummary

Based on an identifiable key `event_id`, the program counts the number of unique events published on an `events` topic. The schema of an `events` topic is as follows:

```
{
  "correlation_id": "ID for the File or group of events",
  "event_id": "ID for single event",
  "event_publisher_id": "ID of the publisher",
  "source_event_id": "UUID of the Source system of the Publisher",
  "root_event_id": "UUID of the root system the event",
  "root_publisher_id": "ID of root system of the event",
  "event_type": "Type of Event",
  "event_type_version": "<version no>",
  "event_publish_ts": "Timestamp when the event is published yyyy-MM-dd HH:mm:ss",
  "event_data": {
  
  }
}
```

The `eventsSummary` represents the summary event that is published. The schema of an `eventsSummary` topic is as follows:

```
{ 
  "correlation_id": "ID for the File or group of events", 
  "total_events": "Total number of events published for the correlation_id"
}
```

`rbg` checks if the `events` is associated with a summary event on the `eventsSummary` topic. If there is a match, the events are designated as "balanced," otherwise, the events are designated as "unbalanced" and an alert is prompted.


### Key scenarios

1. (Happy Path) `events` are published and `eventsSummary` is published, counts match
> Print total `events` received={} expected={}

2. `events` are published and `eventsSummary` is published, counts do not match
> Print Total `events` received={} expected={}

3. `events` are published and `eventsSummary` is not published
> Print Summary event is missing, received={}

4. `eventsSummary` is published, `events` are not published 
> Print none were received,  expected={}


## Prerequisites 

```
a kafka server running on localhost:9092

Hazelcast

Spring Boot
```


## Build

Run `./mvnw spring-boot:run`


## TODOs

1. Persist the counts in DB or Redis
2. Add APIs to retrieve the counts and status based on primary key `corelation_ID`
3. Create a dashboard of in progress files, last event time received
4. Add unit tests with embedded kafka
5. Test using a large-scale message processing application


## See Also

[Hazelcast](https://github.com/hazelcast/hazelcast)
</br>
[meteor](https://github.com/obsidiandynamics/meteor)
</br>
[Counting large numbers of events in small registers](https://www.inf.ed.ac.uk/teaching/courses/exc/reading/morris.pdf)
</br>
[Debugging with kafkacat](https://medium.com/@coderunner/debugging-with-kafkacat-df7851d21968)