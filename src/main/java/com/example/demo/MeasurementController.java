package com.example.demo;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/measurements")
public class MeasurementController {

    private final MeasurementRepository repository;
    private final DetectorRepository detectorRepository;
    private final EventService eventService;

    public MeasurementController(
            MeasurementRepository repository,
            DetectorRepository detectorRepository,
            EventService eventService
    ) {
        this.repository = repository;
        this.detectorRepository = detectorRepository;
        this.eventService = eventService;
    }

    @GetMapping
    public List<Measurement> getAll() {
        return repository.findTop100ByOrderByIdDesc();
    }

    @GetMapping("/latest")
    public List<Measurement> latest() {
        return repository.findTop100ByOrderByIdDesc();
    }
    @GetMapping("/detector/{id}")
    public List<Measurement> byDetector(@PathVariable Long id) {
        return repository.findTop100ByDetectorIdOrderByIdDesc(id);
    }

    @GetMapping("/detector/{id}/500")
    public List<Measurement> byDetector500(@PathVariable Long id) {
        return repository.findTop500ByDetectorIdOrderByIdDesc(id);
    }
    @PostMapping
    public Measurement create(@RequestBody Measurement measurement) {

        if (measurement.getDetector() != null &&
                measurement.getDetector().getId() != null) {

            Detector detector = detectorRepository
                    .findById(measurement.getDetector().getId())
                    .orElseThrow();

            measurement.setDetector(detector);
        }

        Measurement saved = repository.save(measurement);

        if (saved.getValue() != null &&
                saved.getDetector() != null &&
                saved.getDetector().getStation() != null) {

            double value = saved.getValue().doubleValue();

            if (value >= 1.0) {
                eventService.addEvent(
                        saved.getDetector().getStation(),
                        "RADIATION_ALARM",
                        saved.getDetector().getName() + " value is " + saved.getValue()
                );
            } else if (value >= 0.5) {
                eventService.addEvent(
                        saved.getDetector().getStation(),
                        "RADIATION_WARNING",
                        saved.getDetector().getName() + " value is " + saved.getValue()
                );
            }
        }

        return saved;
    }
}