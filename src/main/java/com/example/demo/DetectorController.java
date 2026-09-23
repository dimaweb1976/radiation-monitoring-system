package com.example.demo;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@RestController
@RequestMapping("/api/detectors")
public class DetectorController {

    private final DetectorRepository repository;
    private final StationRepository stations;

    public DetectorController(DetectorRepository repository, StationRepository stations) {
        this.repository = repository;
        this.stations = stations;
    }

    @GetMapping
    public List<Detector> getAll() {
        return repository.findAll();
    }

    @PostMapping
    public Detector create(@RequestBody DetectorRequest request) {
        if (request == null || request.name() == null || request.name().isBlank() || request.name().length() > 255
                || request.type() == null || request.type().isBlank() || request.type().length() > 255
                || request.location() != null && request.location().length() > 255
                || request.stationId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid detector");
        }
        Station station = stations.findById(request.stationId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Station not found"));
        Detector detector = new Detector();
        detector.setName(request.name());
        detector.setLocation(request.location());
        detector.setType(request.type());
        detector.setStation(station);
        return repository.save(detector);
    }
}
