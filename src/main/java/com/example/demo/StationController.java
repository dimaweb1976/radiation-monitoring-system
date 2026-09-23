package com.example.demo;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
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
    public Station create(@RequestBody StationRequest request) {
        if (request == null || request.name() == null || request.name().isBlank() || request.name().length() > 255
                || request.location() != null && request.location().length() > 255
                || request.ipAddress() != null && request.ipAddress().length() > 255
                || request.latitude() != null && (request.latitude().doubleValue() < -90 || request.latitude().doubleValue() > 90)
                || request.longitude() != null && (request.longitude().doubleValue() < -180 || request.longitude().doubleValue() > 180)
                || request.latitude() != null && request.latitude().scale() > 6
                || request.longitude() != null && request.longitude().scale() > 6) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid station");
        }
        Station station = new Station();
        station.setName(request.name());
        station.setLocation(request.location());
        station.setIpAddress(request.ipAddress());
        station.setLatitude(request.latitude());
        station.setLongitude(request.longitude());
        return repository.save(station);
    }
}
