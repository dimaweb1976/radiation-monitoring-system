package com.example.demo;

import org.springframework.stereotype.Service;

@Service
public class EventService {

    private final StationEventRepository eventRepository;

    public EventService(
            StationEventRepository eventRepository) {

        this.eventRepository = eventRepository;
    }

    public void addEvent(
            Station station,
            String eventType,
            String message) {

        System.out.println(
                "EVENT => "
                        + eventType
                        + " : "
                        + message);

        StationEvent event = new StationEvent();

        event.setStation(station);
        event.setEventType(eventType);
        event.setMessage(message);

        eventRepository.save(event);
    }
}
