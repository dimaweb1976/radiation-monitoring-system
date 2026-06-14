package com.example.demo;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/events")
public class StationEventController {

    private final StationEventRepository eventRepository;
    private final StationRepository stationRepository;

    public StationEventController(
            StationEventRepository eventRepository,
            StationRepository stationRepository
    ) {
        this.eventRepository = eventRepository;
        this.stationRepository = stationRepository;
    }

    @GetMapping
    public List<StationEvent> getAll() {
        return eventRepository.findTop50ByOrderByCreatedAtDesc();
    }

    @GetMapping("/latest")
    public List<StationEvent> latest() {
        return eventRepository.findTop50ByOrderByCreatedAtDesc();
    }

    @PostMapping
    public StationEvent create(@RequestBody StationEvent event) {
        Station station = stationRepository.findById(event.getStation().getId())
                .orElseThrow();

        event.setStation(station);
        return eventRepository.save(event);
    }
}
