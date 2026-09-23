package com.example.demo;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/events")
public class StationEventController {

    private final StationEventRepository eventRepository;

    public StationEventController(
            StationEventRepository eventRepository
    ) {
        this.eventRepository = eventRepository;
    }

    @GetMapping
    public List<StationEvent> getAll() {
        return eventRepository.findTop20ByOrderByCreatedAtDesc();
    }

    @GetMapping("/latest")
    public List<StationEvent> latest() {
        return eventRepository.findTop20ByOrderByCreatedAtDesc();
    }

}
