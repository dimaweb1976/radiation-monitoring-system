package com.example.demo;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Random;

@Component
public class SensorSimulator {

    private final MeasurementRepository measurementRepository;
    private final DetectorRepository detectorRepository;
    private final Random random = new Random();

    public SensorSimulator(
            MeasurementRepository measurementRepository,
            DetectorRepository detectorRepository
    ) {
        this.measurementRepository = measurementRepository;
        this.detectorRepository = detectorRepository;
    }

    @Scheduled(fixedRate = 5000)
    public void generate() {
        List<Detector> detectors = detectorRepository.findAll();

        if (detectors.isEmpty()) {
            System.out.println("SIMULATOR: no detectors found");
            return;
        }

        Detector detector = detectors.get(random.nextInt(detectors.size()));

        double raw = random.nextDouble() * 1.4;
        BigDecimal value = BigDecimal.valueOf(raw).setScale(3, RoundingMode.HALF_UP);

        Measurement measurement = new Measurement();
        measurement.setDetector(detector);
        measurement.setValue(value);

        measurementRepository.save(measurement);

        System.out.println("SIMULATOR: " + detector.getName() + " = " + value);
    }
}
