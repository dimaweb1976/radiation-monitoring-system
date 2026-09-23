package com.example.demo;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface MeasurementRepository extends JpaRepository<Measurement, Long> {

    List<Measurement> findTop100ByOrderByIdDesc();

    List<Measurement> findTop100ByDetectorIdOrderByIdDesc(Long detectorId);

    List<Measurement> findTop500ByDetectorIdOrderByIdDesc(Long detectorId);
    Optional<Measurement> findByMessageId(String messageId);
    Optional<Measurement> findTopByDetectorIdOrderByIdDesc(Long detectorId);
}
