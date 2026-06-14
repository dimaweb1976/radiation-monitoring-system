package com.example.demo;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface StationHeartbeatRepository extends JpaRepository<StationHeartbeat, Long> {

    Optional<StationHeartbeat> findTopByStationIdOrderByCreatedAtDesc(Long stationId);

    List<StationHeartbeat> findAllByOrderByCreatedAtDesc();
}
