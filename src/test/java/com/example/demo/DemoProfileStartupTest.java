package com.example.demo;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:demo-profile;MODE=PostgreSQL;NON_KEYWORDS=VALUE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=false"
})
@ActiveProfiles("demo")
class DemoProfileStartupTest {
    @Autowired HeartbeatSimulator heartbeatSimulator;
    @Autowired SensorSimulator sensorSimulator;
    @Autowired StationRepository stations;
    @Autowired DetectorRepository detectors;
    @Autowired StationHeartbeatRepository heartbeats;
    @Autowired MeasurementRepository measurements;

    @Test
    void simulatorsProduceDataForExistingStationAndDetector() {
        Station station = new Station();
        station.setName("Demo station");
        station = stations.saveAndFlush(station);

        Detector detector = new Detector();
        detector.setName("Demo detector");
        detector.setStation(station);
        detectors.saveAndFlush(detector);

        heartbeatSimulator.generateHeartbeat();
        sensorSimulator.generate();

        assertNotNull(stations.findById(station.getId()).orElseThrow().getLastSeen());
        assertFalse(heartbeats.findTop100ByOrderByCreatedAtDesc().isEmpty());
        assertFalse(measurements.findTop100ByDetectorIdOrderByIdDesc(detector.getId()).isEmpty());
    }
}
