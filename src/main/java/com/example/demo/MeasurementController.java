package com.example.demo;

import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import java.util.List;

@RestController
@RequestMapping("/api/measurements")
public class MeasurementController {

    private final MeasurementRepository repository;
    private final MeasurementService measurementService;

    public MeasurementController(
            MeasurementRepository repository,
            MeasurementService measurementService
    ) {
        this.repository = repository;
        this.measurementService = measurementService;
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
    public Measurement create(@RequestBody MeasurementRequest request,
                              @AuthenticationPrincipal DevicePrincipal device) {
        return measurementService.record(request.detectorId(), request.value(), request.unit(),
                request.messageId(), request.measuredAt(), device.stationId());
    }
}
