package com.example.demo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.time.Instant;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.jdbc.core.JdbcTemplate;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.datasource.url=jdbc:h2:mem:utc;MODE=PostgreSQL;NON_KEYWORDS=VALUE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password="
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UtcPersistenceTest {
    @Autowired StationRepository stations;
    @Autowired DetectorRepository detectors;
    @Autowired MeasurementRepository measurements;
    @Autowired JdbcTemplate jdbc;
    @Autowired EntityManager entityManager;

    @Test
    void instantIsStoredInExistingTimestampShapeAsUtc() {
        Station station = new Station();
        station.setName("Test");
        station.setLastSeen(Instant.parse("2026-09-23T12:00:00Z"));
        stations.saveAndFlush(station);
        LocalDateTime raw = jdbc.queryForObject("select last_seen from stations where id = ?",
                LocalDateTime.class, station.getId());
        assertEquals(LocalDateTime.parse("2026-09-23T12:00:00"), raw);
        entityManager.clear();
        assertEquals(Instant.parse("2026-09-23T12:00:00Z"), stations.findById(station.getId()).orElseThrow().getLastSeen());
    }

    @Test
    void measurementKeepsThreeDecimalPlaces() {
        Station station = new Station();
        station.setName("Test");
        stations.saveAndFlush(station);
        Detector detector = new Detector();
        detector.setName("Gamma");
        detector.setStation(station);
        detectors.saveAndFlush(detector);
        Measurement measurement = new Measurement();
        measurement.setDetector(detector);
        measurement.setValue(new BigDecimal("0.546"));
        measurements.saveAndFlush(measurement);
        entityManager.clear();
        assertEquals(0, measurements.findById(measurement.getId()).orElseThrow().getValue()
                .compareTo(new BigDecimal("0.546")));
    }
}
