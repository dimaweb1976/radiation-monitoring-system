package com.example.demo;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/stations")
public class StationController {

    private final StationRepository repository;

    public StationController(StationRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Station> getAll() {
        return repository.findAll();
    }

    @PostMapping
    public Station create(@RequestBody Station station) {
        return repository.save(station);
    }
}
