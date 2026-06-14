package com.example.demo;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/measurements")
public class MeasurementController {

    private final MeasurementRepository repository;

    public MeasurementController(MeasurementRepository repository) {
        this.repository = repository;
    }

@GetMapping("/latest")
public List<Measurement> latest() {
    return repository.findTop100ByOrderByIdDesc();
}

@GetMapping
public List<Measurement> getAll() {
    return repository.findAll();
}

    @PostMapping
    public Measurement create(@RequestBody Measurement measurement) {
        return repository.save(measurement);
    }
}
