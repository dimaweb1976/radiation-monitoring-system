package com.example.demo;

import java.math.BigDecimal;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class MeasurementService {
    private final MeasurementRepository measurements;
    private final DetectorRepository detectors;
    private final EventService events;
    private final BigDecimal warningThreshold;
    private final BigDecimal alarmThreshold;

    public MeasurementService(MeasurementRepository measurements, DetectorRepository detectors,
                              EventService events,
                              @Value("${radiation.threshold.warning:0.5}") BigDecimal warningThreshold,
                              @Value("${radiation.threshold.alarm:1.0}") BigDecimal alarmThreshold) {
        this.measurements = measurements;
        this.detectors = detectors;
        this.events = events;
        this.warningThreshold = warningThreshold;
        this.alarmThreshold = alarmThreshold;
        if (warningThreshold.signum() < 0 || alarmThreshold.compareTo(warningThreshold) <= 0)
            throw new IllegalArgumentException("Alarm threshold must exceed warning threshold");
    }

    @Transactional
    public Measurement record(Long detectorId, BigDecimal value, String unit, String messageId,
                              Instant measuredAt, Long stationId) {
        if (detectorId == null || value == null || value.signum() < 0
                || value.compareTo(new BigDecimal("999999999.999")) > 0
                || value.scale() > 3
                || !"uSv/h".equals(unit) || messageId == null || messageId.isBlank() || messageId.length() > 100
                || measuredAt == null || measuredAt.isAfter(Instant.now().plusSeconds(300))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid measurement");
        }
        Detector detector = detectors.findById(detectorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Detector not found"));
        if (detector.getStation() == null || stationId == null || !stationId.equals(detector.getStation().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Detector is not assigned to this station");
        }
        var existing = measurements.findByMessageId(messageId);
        if (existing.isPresent()) {
            Measurement old = existing.get();
            if (!old.getDetector().getId().equals(detectorId) || old.getValue().compareTo(value) != 0
                    || !unit.equals(old.getUnit()) || !measuredAt.equals(old.getMeasuredAt())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Message ID reused with different data");
            }
            return old;
        }
        int previousLevel = measurements.findTopByDetectorIdOrderByIdDesc(detectorId)
                .map(previous -> level(previous.getValue())).orElse(0);
        int currentLevel = level(value);
        Measurement measurement = new Measurement();
        measurement.setDetector(detector);
        measurement.setValue(value);
        measurement.setUnit(unit);
        measurement.setMessageId(messageId);
        measurement.setMeasuredAt(measuredAt);
        Measurement saved = measurements.save(measurement);

        if (currentLevel == 2 && previousLevel != 2) {
            events.addEvent(detector.getStation(), "RADIATION_ALARM", detector.getName() + " value is " + value + " " + unit);
        } else if (currentLevel == 1 && previousLevel != 1) {
            events.addEvent(detector.getStation(), "RADIATION_WARNING", detector.getName() + " value is " + value + " " + unit);
        } else if (currentLevel == 0 && previousLevel != 0) {
            events.addEvent(detector.getStation(), "RADIATION_NORMAL", detector.getName() + " returned to normal");
        }
        return saved;
    }

    private int level(BigDecimal value) {
        if (value == null) return 0;
        if (value.compareTo(alarmThreshold) >= 0) return 2;
        if (value.compareTo(warningThreshold) >= 0) return 1;
        return 0;
    }
}
