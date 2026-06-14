package com.example.demo;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/detectors")
public class DetectorController {

    private final DetectorRepository repository;

    public DetectorController(DetectorRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Detector> getAll() {
        return repository.findAll();
    }

    @PostMapping
    public Detector create(@RequestBody Detector detector) {
        return repository.save(detector);
    }
}
